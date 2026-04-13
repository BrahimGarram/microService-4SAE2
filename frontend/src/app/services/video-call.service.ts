import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { KeycloakService } from 'keycloak-angular';
import { environment } from '../../environments/environment';

export interface IncomingCall {
  fromUserId: number;
}

function websocketHttpBase(): string {
  const g = environment.apiGatewayUrl?.trim();
  if (g?.startsWith('/')) {
    if (typeof window !== 'undefined' && window.location?.origin) {
      return window.location.origin + g;
    }
    return `http://localhost:4200${g}`;
  }
  if (g) return g;
  if (typeof window !== 'undefined' && window.location?.origin) {
    return window.location.origin + '/gateway';
  }
  return 'http://localhost:4200/gateway';
}

@Injectable({
  providedIn: 'root'
})
export class VideoCallService {
  private readonly wsBaseUrl = websocketHttpBase();
  private ws: WebSocket | null = null;
  private peer: RTCPeerConnection | null = null;
  private localStream: MediaStream | null = null;
  private currentRemoteUserId: number | null = null;
  private pendingOfferFromUserId: number | null = null;
  private pendingOfferData: RTCSessionDescriptionInit | null = null;

  private localStreamSubject = new Subject<MediaStream | null>();
  private remoteStreamSubject = new Subject<MediaStream | null>();
  private incomingCallSubject = new Subject<IncomingCall>();
  private callEndedSubject = new Subject<void>();
  private errorSubject = new Subject<string>();
  private connectionStateSubject = new Subject<'connecting' | 'connected' | 'disconnected'>();

  localStream$ = this.localStreamSubject.asObservable();
  remoteStream$ = this.remoteStreamSubject.asObservable();
  incomingCall$ = this.incomingCallSubject.asObservable();
  callEnded$ = this.callEndedSubject.asObservable();
  error$ = this.errorSubject.asObservable();
  connectionState$ = this.connectionStateSubject.asObservable();

  constructor(private keycloakService: KeycloakService) {}

  async connect(): Promise<void> {
    if (this.ws?.readyState === WebSocket.OPEN) return;
    this.connectionStateSubject.next('connecting');
    const token = await this.getToken();
    if (!token) {
      this.errorSubject.next('Not authenticated');
      this.connectionStateSubject.next('disconnected');
      return;
    }
    const encodedToken = encodeURIComponent(token);
    const url = `${this.wsBaseUrl.replace(/^http/, 'ws')}/ws/signal?token=${encodedToken}`;
    return new Promise((resolve, reject) => {
      this.ws = new WebSocket(url);
      this.ws.onopen = () => {
        this.connectionStateSubject.next('connected');
        resolve();
      };
      this.ws.onerror = () => {
        this.errorSubject.next('WebSocket connection failed');
        this.connectionStateSubject.next('disconnected');
        reject(new Error('WebSocket failed'));
      };
      this.ws.onclose = () => this.connectionStateSubject.next('disconnected');
      this.ws.onmessage = (event) => this.handleSignalingMessage(JSON.parse(event.data));
    });
  }

  disconnect(): void {
    this.endCall();
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.connectionStateSubject.next('disconnected');
  }

  async startCall(remoteUserId: number): Promise<void> {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      await this.connect();
    }
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      this.errorSubject.next('Cannot connect to signaling server');
      return;
    }
    this.currentRemoteUserId = remoteUserId;
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
      this.localStreamSubject.next(this.localStream);
    } catch (e) {
      this.errorSubject.next('Camera or microphone access denied');
      return;
    }
    this.peer = new RTCPeerConnection({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
    });
    this.localStream.getTracks().forEach(track => this.peer!.addTrack(track, this.localStream!));
    this.peer.ontrack = (e) => this.remoteStreamSubject.next(e.streams[0]);
    this.peer.onicecandidate = (e) => {
      if (e.candidate)
        this.sendSignal(remoteUserId, { type: 'ice', data: e.candidate });
    };
    const offer = await this.peer.createOffer();
    await this.peer.setLocalDescription(offer);
    this.sendSignal(remoteUserId, { type: 'offer', data: offer });
  }

  async acceptCall(fromUserId: number): Promise<void> {
    if (!this.pendingOfferData || this.pendingOfferFromUserId !== fromUserId) {
      this.errorSubject.next('No pending offer');
      return;
    }
    this.pendingOfferFromUserId = null;
    const offer = this.pendingOfferData;
    this.pendingOfferData = null;
    this.currentRemoteUserId = fromUserId;
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
      this.localStreamSubject.next(this.localStream);
    } catch (e) {
      this.errorSubject.next('Camera or microphone access denied');
      return;
    }
    this.peer = new RTCPeerConnection({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
    });
    this.localStream.getTracks().forEach(track => this.peer!.addTrack(track, this.localStream!));
    this.peer.ontrack = (e) => this.remoteStreamSubject.next(e.streams[0]);
    this.peer.onicecandidate = (e) => {
      if (e.candidate)
        this.sendSignal(fromUserId, { type: 'ice', data: e.candidate });
    };
    await this.peer.setRemoteDescription(new RTCSessionDescription(offer));
    const answer = await this.peer.createAnswer();
    await this.peer.setLocalDescription(answer);
    this.sendSignal(fromUserId, { type: 'answer', data: answer });
  }

  declineCall(): void {
    this.pendingOfferFromUserId = null;
    this.pendingOfferData = null;
  }

  endCall(): void {
    if (this.localStream) {
      this.localStream.getTracks().forEach(t => t.stop());
      this.localStream = null;
    }
    this.localStreamSubject.next(null);
    this.remoteStreamSubject.next(null);
    if (this.peer) {
      this.peer.close();
      this.peer = null;
    }
    this.currentRemoteUserId = null;
    this.callEndedSubject.next();
  }

  isInCall(): boolean {
    return this.peer !== null && this.currentRemoteUserId !== null;
  }

  getCurrentRemoteUserId(): number | null {
    return this.currentRemoteUserId;
  }

  private async getToken(): Promise<string | null> {
    try {
      const fromKeycloak = await this.keycloakService.getToken();
      return fromKeycloak ?? localStorage.getItem('kc-access-token');
    } catch {
      return localStorage.getItem('kc-access-token');
    }
  }

  private sendSignal(toUserId: number, signal: { type: string; data: object }): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
    this.ws.send(JSON.stringify({ type: 'signal', toUserId, signal }));
  }

  private handleSignalingMessage(msg: any): void {
    if (msg.type === 'error') {
      this.errorSubject.next(msg.message || 'Signaling error');
      return;
    }
    if (msg.type === 'signal' && msg.fromUserId != null && msg.signal) {
      const { type, data } = msg.signal;
      const fromUserId = msg.fromUserId as number;
      if (type === 'offer') {
        this.pendingOfferFromUserId = fromUserId;
        this.pendingOfferData = data;
        this.incomingCallSubject.next({ fromUserId });
      } else if (type === 'answer' && this.peer && this.currentRemoteUserId === fromUserId) {
        this.peer.setRemoteDescription(new RTCSessionDescription(data));
      } else if (type === 'ice' && this.peer && this.currentRemoteUserId === fromUserId) {
        this.peer.addIceCandidate(new RTCIceCandidate(data));
      }
    }
  }
}
