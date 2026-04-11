import { Component, OnInit, ViewChild, ElementRef, Output, EventEmitter, Input } from '@angular/core';
import SignaturePad from 'signature_pad';

@Component({
  selector: 'app-signature-modal',
  templateUrl: './signature-modal.component.html',
  styleUrls: ['./signature-modal.component.css']
})
export class SignatureModalComponent implements OnInit {
  @ViewChild('signatureCanvas', { static: false }) signatureCanvas!: ElementRef<HTMLCanvasElement>;
  @Output() signatureSaved = new EventEmitter<string>();
  @Output() modalClosed = new EventEmitter<void>();
  @Input() signerName: string = '';
  @Input() signerRole: string = 'Client';
  @Input() showModal = true;
  
  signaturePad!: SignaturePad;

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    if (this.signatureCanvas && this.showModal) {
      setTimeout(() => {
        this.initSignaturePad();
      }, 100);
    }
  }

  closeModal(): void {
    this.modalClosed.emit();
  }

  private initSignaturePad(): void {
    if (!this.signatureCanvas) return;
    
    const canvas = this.signatureCanvas.nativeElement;
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;
    
    this.signaturePad = new SignaturePad(canvas, {
      backgroundColor: 'rgb(255, 255, 255)',
      penColor: 'rgb(0, 0, 0)',
      minWidth: 1,
      maxWidth: 3
    });
  }

  clearSignature(): void {
    if (this.signaturePad) {
      this.signaturePad.clear();
    }
  }

  saveSignature(): void {
    if (this.signaturePad && !this.signaturePad.isEmpty()) {
      const signatureDataUrl = this.signaturePad.toDataURL('image/png');
      this.signatureSaved.emit(signatureDataUrl);
      this.closeModal();
    } else {
      alert('Please provide a signature first');
    }
  }

  isSignatureEmpty(): boolean {
    return this.signaturePad ? this.signaturePad.isEmpty() : true;
  }
}
