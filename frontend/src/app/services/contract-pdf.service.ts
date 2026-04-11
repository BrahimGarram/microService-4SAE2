import { Injectable } from '@angular/core';
import jsPDF from 'jspdf';

export interface ContractPdfData {
  contractNumber: string;
  title: string;
  clientName: string;
  clientEmail: string;
  clientAddress?: string;
  freelancerName: string;
  freelancerEmail: string;
  freelancerAddress?: string;
  startDate: string;
  endDate?: string;
  description: string;
  totalAmount: number;
  currency?: string;
  paymentTerms?: string;
  deliverables?: string[];
  terms?: string;
  clientSignature?: string;
  freelancerSignature?: string;
  clientSignatureDate?: string;
  freelancerSignatureDate?: string;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class ContractPdfService {

  // Brand colors
  private readonly ORANGE   = [255, 107, 53]  as [number,number,number];
  private readonly DARK     = [26,  32,  44]   as [number,number,number];
  private readonly GRAY     = [113, 128, 150]  as [number,number,number];
  private readonly LIGHT    = [247, 250, 252]  as [number,number,number];
  private readonly WHITE    = [255, 255, 255]  as [number,number,number];
  private readonly GREEN    = [72,  187, 120]  as [number,number,number];
  private readonly BLUE     = [66,  153, 225]  as [number,number,number];
  private readonly BORDER   = [226, 232, 240]  as [number,number,number];

  generateContractPdf(data: ContractPdfData): void {
    // Load logo as base64 first, then generate PDF
    this.loadImageAsBase64('assets/logo.png').then(logoBase64 => {
      this.buildPdf(data, logoBase64);
    }).catch(() => {
      this.buildPdf(data, null);
    });
  }

  private loadImageAsBase64(url: string): Promise<string> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.crossOrigin = 'anonymous';
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = img.naturalWidth;
        canvas.height = img.naturalHeight;
        const ctx = canvas.getContext('2d')!;
        ctx.drawImage(img, 0, 0);
        resolve(canvas.toDataURL('image/png'));
      };
      img.onerror = reject;
      img.src = url + '?t=' + Date.now();
    });
  }

  private buildPdf(data: ContractPdfData, logoBase64: string | null): void {
    const doc = new jsPDF({ unit: 'mm', format: 'a4' });
    const W = doc.internal.pageSize.getWidth();
    const H = doc.internal.pageSize.getHeight();
    const M = 18;
    let y = 0;

    // ── HEADER BAR ──────────────────────────────────────────────────────────
    doc.setFillColor(...this.DARK);
    doc.rect(0, 0, W, 42, 'F');

    // Orange accent stripe
    doc.setFillColor(...this.ORANGE);
    doc.rect(0, 42, W, 3, 'F');

    // Logo — use the real Freelancify logo
    if (logoBase64) {
      // Logo has dark background — place it directly, it blends with the dark header
      doc.addImage(logoBase64, 'PNG', M, 7, 44, 26);
    } else {
      // Fallback: draw the F shape manually
      doc.setFillColor(220, 53, 69);
      doc.roundedRect(M, 9, 22, 22, 3, 3, 'F');
      doc.setTextColor(...this.WHITE);
      doc.setFontSize(16);
      doc.setFont('helvetica', 'bold');
      doc.text('F', M + 11, 24, { align: 'center' });
      // Brand name next to fallback icon
      doc.setTextColor(...this.WHITE);
      doc.setFontSize(22);
      doc.setFont('helvetica', 'bold');
      doc.text('Freelancify', M + 30, 20);
      doc.setFontSize(9);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(160, 174, 192);
      doc.text('Professional Freelance Platform', M + 30, 28);
    }

    // Contract label (right side)
    doc.setTextColor(...this.WHITE);
    doc.setFontSize(11);
    doc.setFont('helvetica', 'bold');
    doc.text('FREELANCE CONTRACT', W - M, 18, { align: 'right' });
    doc.setFontSize(8);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(160, 174, 192);
    doc.text(`#${data.contractNumber}`, W - M, 26, { align: 'right' });
    doc.text(`Issued: ${new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}`, W - M, 33, { align: 'right' });

    y = 54;

    // ── CONTRACT TITLE ───────────────────────────────────────────────────────
    doc.setTextColor(...this.DARK);
    doc.setFontSize(17);
    doc.setFont('helvetica', 'bold');
    const titleLines = doc.splitTextToSize(data.title, W - M * 2);
    doc.text(titleLines, M, y);
    y += titleLines.length * 7 + 2;

    // Orange underline
    doc.setDrawColor(...this.ORANGE);
    doc.setLineWidth(0.8);
    doc.line(M, y, M + 60, y);
    y += 8;

    // ── STATUS BADGE (if special) ────────────────────────────────────────────
    if (data.status && data.status !== 'ACTIVE' && data.status !== 'PENDING') {
      const badgeColors: Record<string, [number,number,number]> = {
        COMPLETED: this.GREEN, DISPUTED: [229,62,62], CANCELLED: this.GRAY,
        EXPIRED: this.GRAY, RENEWED: [128,90,213]
      };
      const bc = badgeColors[data.status] || this.GRAY;
      doc.setFillColor(...bc);
      doc.roundedRect(M, y - 4, 32, 8, 2, 2, 'F');
      doc.setTextColor(...this.WHITE);
      doc.setFontSize(7);
      doc.setFont('helvetica', 'bold');
      doc.text(data.status, M + 16, y + 1, { align: 'center' });
      y += 10;
    }

    // ── PARTIES SECTION ──────────────────────────────────────────────────────
    const colW = (W - M * 2 - 6) / 2;

    // Client card
    doc.setFillColor(...this.LIGHT);
    doc.roundedRect(M, y, colW, 32, 3, 3, 'F');
    doc.setDrawColor(...this.BORDER);
    doc.setLineWidth(0.3);
    doc.roundedRect(M, y, colW, 32, 3, 3, 'S');
    // Orange top border
    doc.setFillColor(...this.ORANGE);
    doc.roundedRect(M, y, colW, 4, 3, 3, 'F');
    doc.rect(M, y + 2, colW, 2, 'F');

    doc.setTextColor(...this.WHITE);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'bold');
    doc.text('CLIENT', M + 4, y + 3);

    doc.setTextColor(...this.DARK);
    doc.setFontSize(9);
    doc.setFont('helvetica', 'bold');
    doc.text(data.clientName, M + 4, y + 11);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(...this.GRAY);
    doc.setFontSize(8);
    doc.text(data.clientEmail, M + 4, y + 17);
    if (data.clientAddress) {
      const aLines = doc.splitTextToSize(data.clientAddress, colW - 8);
      doc.text(aLines[0], M + 4, y + 23);
    }

    // Freelancer card
    const fx = M + colW + 6;
    doc.setFillColor(...this.LIGHT);
    doc.roundedRect(fx, y, colW, 32, 3, 3, 'F');
    doc.setDrawColor(...this.BORDER);
    doc.roundedRect(fx, y, colW, 32, 3, 3, 'S');
    doc.setFillColor(...this.BLUE);
    doc.roundedRect(fx, y, colW, 4, 3, 3, 'F');
    doc.rect(fx, y + 2, colW, 2, 'F');

    doc.setTextColor(...this.WHITE);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'bold');
    doc.text('FREELANCER', fx + 4, y + 3);

    doc.setTextColor(...this.DARK);
    doc.setFontSize(9);
    doc.setFont('helvetica', 'bold');
    doc.text(data.freelancerName, fx + 4, y + 11);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(...this.GRAY);
    doc.setFontSize(8);
    doc.text(data.freelancerEmail, fx + 4, y + 17);
    if (data.freelancerAddress) {
      const aLines = doc.splitTextToSize(data.freelancerAddress, colW - 8);
      doc.text(aLines[0], fx + 4, y + 23);
    }

    y += 38;

    // ── CONTRACT DETAILS ROW ─────────────────────────────────────────────────
    doc.setFillColor(...this.DARK);
    doc.roundedRect(M, y, W - M * 2, 16, 3, 3, 'F');

    const detailCols = [
      { label: 'START DATE', value: data.startDate },
      { label: 'END DATE',   value: data.endDate || 'Open-ended' },
      { label: 'AMOUNT',     value: `${data.totalAmount} ${data.currency || 'USD'}` },
      { label: 'PAYMENT',    value: data.paymentTerms?.split('.')[0] || '30 days' }
    ];
    const dw = (W - M * 2) / detailCols.length;
    detailCols.forEach((d, i) => {
      const dx = M + i * dw + dw / 2;
      doc.setTextColor(160, 174, 192);
      doc.setFontSize(6);
      doc.setFont('helvetica', 'bold');
      doc.text(d.label, dx, y + 6, { align: 'center' });
      doc.setTextColor(...this.WHITE);
      doc.setFontSize(8);
      doc.setFont('helvetica', 'bold');
      const val = doc.splitTextToSize(d.value, dw - 4);
      doc.text(val[0], dx, y + 12, { align: 'center' });
      // divider
      if (i < detailCols.length - 1) {
        doc.setDrawColor(45, 55, 72);
        doc.line(M + (i + 1) * dw, y + 3, M + (i + 1) * dw, y + 13);
      }
    });
    y += 22;

    // ── SCOPE OF WORK ────────────────────────────────────────────────────────
    this.sectionTitle(doc, 'SCOPE OF WORK', M, y);
    y += 7;
    doc.setTextColor(...this.DARK);
    doc.setFontSize(8.5);
    doc.setFont('helvetica', 'normal');
    const descLines = doc.splitTextToSize(data.description || 'No description provided.', W - M * 2);
    const descSlice = descLines.slice(0, 4);
    doc.text(descSlice, M, y);
    y += descSlice.length * 5 + 6;

    // ── DELIVERABLES ─────────────────────────────────────────────────────────
    if (data.deliverables && data.deliverables.length > 0) {
      this.sectionTitle(doc, 'DELIVERABLES', M, y);
      y += 7;
      const maxD = Math.min(data.deliverables.length, 5);
      for (let i = 0; i < maxD; i++) {
        doc.setFillColor(...this.ORANGE);
        doc.circle(M + 2, y - 1, 1.2, 'F');
        doc.setTextColor(...this.DARK);
        doc.setFontSize(8.5);
        doc.setFont('helvetica', 'normal');
        const dl = doc.splitTextToSize(data.deliverables[i], W - M * 2 - 8);
        doc.text(dl[0], M + 6, y);
        y += 5;
      }
      if (data.deliverables.length > 5) {
        doc.setTextColor(...this.GRAY);
        doc.setFontSize(7.5);
        doc.text(`+ ${data.deliverables.length - 5} more deliverables`, M + 6, y);
        y += 5;
      }
      y += 3;
    }

    // ── TERMS ────────────────────────────────────────────────────────────────
    if (data.terms) {
      this.sectionTitle(doc, 'TERMS & CONDITIONS', M, y);
      y += 7;
      doc.setFillColor(255, 251, 235);
      doc.setDrawColor(246, 173, 85);
      doc.setLineWidth(0.3);
      const termsLines = doc.splitTextToSize(data.terms, W - M * 2 - 8);
      const termsSlice = termsLines.slice(0, 3);
      doc.roundedRect(M, y - 3, W - M * 2, termsSlice.length * 5 + 6, 2, 2, 'FD');
      doc.setTextColor(120, 53, 15);
      doc.setFontSize(8);
      doc.setFont('helvetica', 'normal');
      doc.text(termsSlice, M + 4, y + 2);
      y += termsSlice.length * 5 + 10;
    }

    // ── SIGNATURES ───────────────────────────────────────────────────────────
    const sigY = Math.max(y + 4, H - 52);
    const sigW = (W - M * 2 - 8) / 2;

    // Client signature box
    doc.setFillColor(...this.LIGHT);
    doc.setDrawColor(...this.BORDER);
    doc.setLineWidth(0.3);
    doc.roundedRect(M, sigY, sigW, 28, 3, 3, 'FD');
    doc.setFillColor(...this.ORANGE);
    doc.roundedRect(M, sigY, sigW, 5, 3, 3, 'F');
    doc.rect(M, sigY + 3, sigW, 2, 'F');
    doc.setTextColor(...this.WHITE);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'bold');
    doc.text('CLIENT SIGNATURE', M + 4, sigY + 4);

    if (data.clientSignature) {
      try { doc.addImage(data.clientSignature, 'PNG', M + 3, sigY + 7, sigW - 6, 12); } catch {}
    } else {
      doc.setDrawColor(...this.BORDER);
      doc.setLineWidth(0.5);
      doc.line(M + 4, sigY + 18, M + sigW - 4, sigY + 18);
    }
    doc.setTextColor(...this.GRAY);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'normal');
    doc.text(data.clientSignatureDate ? `Date: ${data.clientSignatureDate}` : 'Date: _______________', M + 4, sigY + 25);

    // Freelancer signature box
    const fsx = M + sigW + 8;
    doc.setFillColor(...this.LIGHT);
    doc.setDrawColor(...this.BORDER);
    doc.roundedRect(fsx, sigY, sigW, 28, 3, 3, 'FD');
    doc.setFillColor(...this.BLUE);
    doc.roundedRect(fsx, sigY, sigW, 5, 3, 3, 'F');
    doc.rect(fsx, sigY + 3, sigW, 2, 'F');
    doc.setTextColor(...this.WHITE);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'bold');
    doc.text('FREELANCER SIGNATURE', fsx + 4, sigY + 4);

    if (data.freelancerSignature) {
      try { doc.addImage(data.freelancerSignature, 'PNG', fsx + 3, sigY + 7, sigW - 6, 12); } catch {}
    } else {
      doc.setDrawColor(...this.BORDER);
      doc.setLineWidth(0.5);
      doc.line(fsx + 4, sigY + 18, fsx + sigW - 4, sigY + 18);
    }
    doc.setTextColor(...this.GRAY);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'normal');
    doc.text(data.freelancerSignatureDate ? `Date: ${data.freelancerSignatureDate}` : 'Date: _______________', fsx + 4, sigY + 25);

    // ── FOOTER ───────────────────────────────────────────────────────────────
    doc.setFillColor(...this.DARK);
    doc.rect(0, H - 14, W, 14, 'F');
    doc.setFillColor(...this.ORANGE);
    doc.rect(0, H - 14, W, 1.5, 'F');

    doc.setTextColor(160, 174, 192);
    doc.setFontSize(6.5);
    doc.setFont('helvetica', 'normal');
    doc.text('This is a legally binding contract. Please read carefully before signing.', W / 2, H - 8, { align: 'center' });
    doc.setFontSize(6);
    doc.setTextColor(74, 85, 104);
    doc.text(`Freelancify Platform  ·  Generated ${new Date().toLocaleString()}  ·  Contract #${data.contractNumber}`, W / 2, H - 4, { align: 'center' });

    // ── STATUS WATERMARK ─────────────────────────────────────────────────────
    if (data.status && ['DISPUTED','CANCELLED','EXPIRED','COMPLETED','RENEWED'].includes(data.status)) {
      const wColors: Record<string,[number,number,number]> = {
        DISPUTED: [229,62,62], CANCELLED: [113,128,150],
        EXPIRED: [113,128,150], COMPLETED: [72,187,120], RENEWED: [128,90,213]
      };
      const [r,g,b] = wColors[data.status] || [113,128,150];
      doc.setTextColor(r, g, b);
      doc.setFontSize(52);
      doc.setFont('helvetica', 'bold');
      doc.setGState(new (doc as any).GState({ opacity: 0.08 }));
      doc.text(data.status, W / 2, H / 2, { align: 'center', angle: 35 });
      doc.setGState(new (doc as any).GState({ opacity: 1 }));
    }

    const fileName = `Freelancify_Contract_${data.contractNumber}_${data.title.replace(/\s+/g,'_').substring(0,30)}.pdf`;
    doc.save(fileName);
  }

  private sectionTitle(doc: jsPDF, title: string, x: number, y: number): void {
    doc.setTextColor(...this.ORANGE);
    doc.setFontSize(8);
    doc.setFont('helvetica', 'bold');
    doc.text(title, x, y);
    doc.setDrawColor(...this.ORANGE);
    doc.setLineWidth(0.4);
    doc.line(x, y + 1.5, x + 40, y + 1.5);
  }

  previewContractPdf(data: ContractPdfData): Blob {
    const doc = new jsPDF();
    return doc.output('blob');
  }
}
