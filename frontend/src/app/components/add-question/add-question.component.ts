import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { QuestionTestService, QuestionTest } from '../../services/QuestionTestService';

export const DOMAINS = ['JAVA', 'SPRING_BOOT', 'ANGULAR', 'RESEAU', 'SQL', 'DEVOPS', 'DOCKER', 'IA'] as const;
export const QUESTION_TYPES = ['QCM_SIMPLE', 'CODING', 'VRAI_FAUX'] as const;
export const CODING_LANGUAGES = ['java', 'python', 'javascript'] as const;
export const DIFFICULTY_LEVELS = ['EASY', 'MEDIUM', 'HARD', 'EXPERT'] as const;

/** Validateur : pour un QCM, au moins 2 choix non vides sont requis */
function minChoicesQcmValidator(group: AbstractControl): ValidationErrors | null {
  const form = group as FormGroup;
  const questionType = form.get('questionType')?.value;
  const choices = form.get('choices') as FormArray;
  if (questionType !== 'QCM_SIMPLE' || !choices) return null;
  const filledCount = choices.controls.filter(
    c => c.value != null && String(c.value).trim().length > 0
  ).length;
  return filledCount >= 2 ? null : { minChoicesQcm: { required: 2, actual: filledCount } };
}

@Component({
  selector: 'app-add-question',
  templateUrl: './add-question.component.html',
  styleUrl: './add-question.component.css'
})
export class AddQuestionComponent implements OnInit {
  form: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  /** Affiché quand la soumission est bloquée par des erreurs de validation */
  showValidationErrors = false;
  /** Erreurs de validation renvoyées par le backend (contrôle de saisie serveur) */
  backendValidationErrors: { field: string; message: string }[] = [];

  readonly domains = DOMAINS;
  readonly questionTypes = QUESTION_TYPES;
  readonly codingLanguages = CODING_LANGUAGES;
  readonly difficultyLevels = DIFFICULTY_LEVELS;

  constructor(
    private fb: FormBuilder,
    private questionService: QuestionTestService,
    private router: Router
  ) {
    this.form = this.fb.group(
      {
        questionText: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
        domain: ['', Validators.required],
        questionType: ['', Validators.required],
        difficultyLevel: ['', Validators.required],
        duration: [60, [Validators.min(1)]],
        choices: this.fb.array([]),
        correctAnswer: ['', [Validators.required, Validators.minLength(1)]],
        language: ['java'],
        starterCode: [''],
        testCasesJson: ['']
      },
      { validators: minChoicesQcmValidator }
    );
  }

  ngOnInit(): void {
    this.form.get('questionType')?.valueChanges.subscribe(type => {
      const choicesArray = this.form.get('choices') as FormArray;
      const correctAnswerCtrl = this.form.get('correctAnswer');
      if (type === 'QCM_SIMPLE' && choicesArray.length === 0) {
        this.addChoice();
        this.addChoice();
      }
      if (type !== 'QCM_SIMPLE') {
        while (choicesArray.length) {
          choicesArray.removeAt(0);
        }
      }
      if (type === 'CODING') {
        correctAnswerCtrl?.clearValidators();
        correctAnswerCtrl?.setValue('');
      } else {
        correctAnswerCtrl?.setValidators([Validators.required, Validators.minLength(1)]);
      }
      correctAnswerCtrl?.updateValueAndValidity();
    });
  }

  get choices(): FormArray {
    return this.form.get('choices') as FormArray;
  }

  addChoice(): void {
    this.choices.push(this.fb.control('', Validators.required));
  }

  removeChoice(index: number): void {
    if (this.choices.length > 1) {
      this.choices.removeAt(index);
    }
  }

  get showChoices(): boolean {
    return this.form.get('questionType')?.value === 'QCM_SIMPLE';
  }

  get showCodingFields(): boolean {
    return this.form.get('questionType')?.value === 'CODING';
  }

  /** Erreur "minimum 2 choix" pour QCM */
  get choicesMinError(): boolean {
    return !!this.form.errors?.['minChoicesQcm'];
  }

  private scrollToFirstError(): void {
    const el = document.querySelector('.form-card-modern .error-msg, .form-card-modern .validation-alert');
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }

  onSubmit(): void {
    this.showValidationErrors = false;
    this.errorMessage = '';
    this.backendValidationErrors = [];
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.showValidationErrors = true;
      this.scrollToFirstError();
      return;
    }
    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const raw = this.form.getRawValue();
    const payload: QuestionTest = {
      questionText: raw.questionText?.trim() ?? '',
      domain: raw.domain,
      questionType: raw.questionType,
      difficultyLevel: raw.difficultyLevel || 'MEDIUM',
      duration: raw.duration ? Number(raw.duration) : 60,
      choices: Array.isArray(raw.choices) ? raw.choices.filter((c: string) => c && String(c).trim()) : [],
      correctAnswer: (raw.correctAnswer && String(raw.correctAnswer).trim()) || '',
      isActive: true,
      createdAt: new Date()
    };
    if (raw.questionType === 'CODING') {
      payload.language = (raw.language || 'java').toLowerCase();
      payload.starterCode = raw.starterCode != null ? String(raw.starterCode) : '';
      payload.testCasesJson = raw.testCasesJson != null ? String(raw.testCasesJson).trim() : '';
    }

    this.questionService.createQuestion(payload).subscribe({
      next: () => {
        this.successMessage = 'Question added successfully.';
        this.isSubmitting = false;
        this.backendValidationErrors = [];
        setTimeout(() => {
          this.router.navigate(['/admin/test']);
        }, 1500);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.backendValidationErrors = Array.isArray(err?.error?.errors)
          ? err.error.errors.map((e: { field?: string; message?: string }) => ({
              field: e.field ?? '',
              message: e.message ?? 'Invalid value'
            }))
          : [];
        if (this.backendValidationErrors.length > 0) {
          this.errorMessage = 'Please fix the errors shown below.';
        } else {
          this.errorMessage = err?.error?.message ?? 'Error adding the question.';
        }
        this.scrollToFirstError();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/test']);
  }
}
