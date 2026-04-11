import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export type FaceCheckResult = 'match' | 'no_match' | 'no_face' | 'multiple_faces' | 'no_profile_photo' | 'error';

import { environment } from '../../environments/environment';
const FACE_VERIFY_API = `${environment.apiGatewayUrl}/service-test/api/face/verify`;

export interface FaceVerifyApiResponse {
  match: boolean;
  similarity: number;
  message?: string;
  noFaceDetected?: boolean;
  multipleFacesDetected?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class FaceVerificationService {
  constructor(private http: HttpClient) {}

  /**
   * Compare le visage visible dans la vidéo avec la photo de profil.
   * Accepte une data URL (base64) ou une URL HTTP (photo hébergée sur le backend user).
   * Utilise uniquement le backend Spring Boot (CompreFace). Aucun fallback face-api.js.
   */
  async compareWithProfile(
    video: HTMLVideoElement,
    profilePhotoDataUrl: string | null | undefined
  ): Promise<FaceCheckResult> {
    if (!profilePhotoDataUrl || !profilePhotoDataUrl.trim()) {
      return 'no_profile_photo';
    }
    // Si c'est une URL (backend1 renvoie profilePicture en URL), la convertir en base64
    let sourceBase64: string = profilePhotoDataUrl.trim();
    if (sourceBase64.startsWith('http://') || sourceBase64.startsWith('https://')) {
      const dataUrl = await this.fetchImageAsDataUrl(sourceBase64);
      if (!dataUrl) return 'no_profile_photo';
      sourceBase64 = dataUrl;
    }
    if (!sourceBase64.startsWith('data:image')) {
      return 'no_profile_photo';
    }
    if (video.readyState < 2 || video.videoWidth === 0 || video.videoHeight === 0) {
      return 'error';
    }
    // Éviter d'envoyer des images trop petites (cadre pas encore prêt)
    if (video.videoWidth < 100 || video.videoHeight < 100) {
      return 'error';
    }

    // Une seule capture : en paysage (DroidCam) on redresse d'emblée pour n'envoyer que l'image correcte
    const forceRotate = video.videoWidth > video.videoHeight;
    const targetBase64 = this.captureVideoFrameAsBase64(video, forceRotate);
    if (!targetBase64) return 'error';

    try {
      const apiResult = await this.verifyViaBackend(sourceBase64, targetBase64);
      if (apiResult !== null) {
        if (apiResult.noFaceDetected) return 'no_face';
        if (apiResult.multipleFacesDetected) return 'multiple_faces';
        console.log(
          `[FaceVerification] similarity=${apiResult.similarity.toFixed(3)} | ${apiResult.match ? '✓ match' : '✗ no_match'}`
        );
        return apiResult.match ? 'match' : 'no_match';
      }
    } catch (e) {
      console.warn('[FaceVerification] Erreur appel backend:', e);
    }
    return 'error';
  }

  private async verifyViaBackend(
    sourceImageBase64: string,
    targetImageBase64: string
  ): Promise<FaceVerifyApiResponse | null> {
    try {
      const res = await firstValueFrom(
        this.http.post<FaceVerifyApiResponse>(FACE_VERIFY_API, {
          sourceImageBase64: sourceImageBase64,
          targetImageBase64: targetImageBase64
        })
      );
      return res;
    } catch {
      return null;
    }
  }

  /** Résolution minimale pour la capture (CompreFace détecte mal les visages trop petits). */
  private static readonly MIN_CAPTURE_WIDTH = 640;

  /**
   * Capture une frame vidéo et optionnellement la redresse (+90°).
   * DroidCam envoie l'image tournée 90° CCW (tête à droite) : on applique +90° pour avoir le visage à l'endroit.
   */
  private captureVideoFrameAsBase64(video: HTMLVideoElement, forceRotate: boolean): string | null {
    try {
      const vW = video.videoWidth;
      const vH = video.videoHeight;
      if (vW <= 0 || vH <= 0) return null;

      let canvas = document.createElement('canvas');
      let ctx = canvas.getContext('2d');
      if (!ctx) return null;

      const applyRotation = forceRotate || vH > vW;
      if (applyRotation) {
        // +90° (sens horaire) pour redresser : DroidCam envoie l'image tournée 90° CCW (tête à droite)
        canvas.width = vH;
        canvas.height = vW;
        ctx.translate(canvas.width, 0);
        ctx.rotate((90 * Math.PI) / 180);
        ctx.drawImage(video, 0, 0, vW, vH, 0, 0, vH, vW);
      } else {
        canvas.width = vW;
        canvas.height = vH;
        ctx.drawImage(video, 0, 0, vW, vH, 0, 0, vW, vH);
      }

      let w = canvas.width;
      let h = canvas.height;
      if (w < FaceVerificationService.MIN_CAPTURE_WIDTH) {
        const scale = FaceVerificationService.MIN_CAPTURE_WIDTH / w;
        const scaledW = Math.round(w * scale);
        const scaledH = Math.round(h * scale);
        const canvas2 = document.createElement('canvas');
        canvas2.width = scaledW;
        canvas2.height = scaledH;
        const ctx2 = canvas2.getContext('2d');
        if (ctx2) {
          ctx2.drawImage(canvas, 0, 0, w, h, 0, 0, scaledW, scaledH);
          canvas = canvas2;
        }
      }
      return canvas.toDataURL('image/jpeg', 0.92);
    } catch {
      return null;
    }
  }

  /**
   * Charge une image depuis une URL et la convertit en data URL (base64).
   * Nécessaire quand le backend user renvoie profilePicture en URL (ex. backend1).
   */
  private async fetchImageAsDataUrl(imageUrl: string): Promise<string | null> {
    try {
      const response = await fetch(imageUrl, { mode: 'cors' });
      if (!response.ok) return null;
      const blob = await response.blob();
      return await new Promise<string | null>((resolve) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result as string);
        reader.onerror = () => resolve(null);
        reader.readAsDataURL(blob);
      });
    } catch {
      return null;
    }
  }

  /** Ne fait plus rien (plus de face-api.js). Conservé pour compatibilité avec le log chaque seconde. */
  async getFaceScore(_video: HTMLVideoElement): Promise<number | null> {
    return null;
  }
}
