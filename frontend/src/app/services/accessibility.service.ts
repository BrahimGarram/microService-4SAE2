import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export type FontSize = 'normal' | 'large';
export type AppLanguage = 'en' | 'fr';

const STORAGE_LANG = 'app_lang';
const STORAGE_FONT = 'app_font_size';
const STORAGE_CONTRAST = 'app_high_contrast';

@Injectable({
  providedIn: 'root'
})
export class AccessibilityService {

  constructor(private translate: TranslateService) {
    this.applyStoredPreferences();
  }

  get currentLang(): AppLanguage {
    const lang = this.translate.currentLang || this.translate.defaultLang;
    return (lang === 'fr' ? 'fr' : 'en') as AppLanguage;
  }

  get isHighContrast(): boolean {
    return localStorage.getItem(STORAGE_CONTRAST) === 'true';
  }

  get fontSize(): FontSize {
    return (localStorage.getItem(STORAGE_FONT) as FontSize) || 'normal';
  }

  setLanguage(lang: AppLanguage): void {
    this.translate.use(lang);
    localStorage.setItem(STORAGE_LANG, lang);
    document.documentElement.lang = lang;
  }

  setFontSize(size: FontSize): void {
    localStorage.setItem(STORAGE_FONT, size);
    this.applyFontSize(size);
  }

  setHighContrast(enabled: boolean): void {
    localStorage.setItem(STORAGE_CONTRAST, String(enabled));
    this.applyHighContrast(enabled);
  }

  private applyStoredPreferences(): void {
    const lang = (localStorage.getItem(STORAGE_LANG) as AppLanguage) || this.getBrowserLang();
    this.translate.use(lang);
    document.documentElement.lang = lang;
    this.applyFontSize(this.fontSize);
    this.applyHighContrast(this.isHighContrast);
  }

  private getBrowserLang(): AppLanguage {
    const browser = navigator.language || (navigator as any).userLanguage;
    if (browser && browser.startsWith('fr')) return 'fr';
    return 'en';
  }

  private applyFontSize(size: FontSize): void {
    document.documentElement.classList.remove('font-normal', 'font-large');
    document.documentElement.classList.add(size === 'large' ? 'font-large' : 'font-normal');
  }

  private applyHighContrast(enabled: boolean): void {
    if (enabled) {
      document.documentElement.classList.add('high-contrast');
    } else {
      document.documentElement.classList.remove('high-contrast');
    }
  }
}
