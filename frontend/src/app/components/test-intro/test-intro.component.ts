import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AffectationTestService, RunCodeResult } from '../../services/affectation-test.service';

@Component({
  selector: 'app-test-intro',
  templateUrl: './test-intro.component.html',
  styleUrls: ['./test-intro.component.css']
})
export class TestIntroComponent implements OnInit {
  username = '';
  loading = true;
  canStartTest = true;
  message = '';
  /** 'success' = déjà réussi, 'warning' = max tentatives */
  messageType: 'success' | 'warning' | null = null;
  errorMessage = '';
  starting = false;
  /** Modal de confirmation des règles */
  showRulesModal = false;
  rulesAccepted = false;

  /** Sandbox CODING : langage choisi (Java, Python, JavaScript). */
  sandboxLanguage: 'java' | 'python' | 'javascript' = 'java';
  /** IDs des questions CODING par langage (Java 130, Python 139, JavaScript 140 en base). */
  readonly sandboxQuestionIds: Record<'java' | 'python' | 'javascript', number> = {
    java: 130,
    python: 139,
    javascript: 140
  };
  /** Descriptions et code de départ par langage pour le sandbox. */
  readonly sandboxConfig: Record<'java' | 'python' | 'javascript', { label: string; description: string; starterCode: string }> = {
    java: {
      label: 'JAVA',
      description: 'Example: write a sum(int a, int b) method that adds two integers read from standard input.',
      starterCode: `// Example: sum of two integers
import java.util.Scanner;
public class Main {
    public static int sum(int a, int b) {
        return a + b;
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println(sum(a, b));
    }
}`
    },
    python: {
      label: 'PYTHON',
      description: 'Example: read two integers from standard input and print their sum.',
      starterCode: `# Read two integers and print their sum
a = int(input())
b = int(input())
print(a + b)`
    },
    javascript: {
      label: 'JAVASCRIPT',
      description: 'Example: read two integers from standard input and print their sum.',
      starterCode: `// Read two integers (stdin) and print their sum
const input = require('fs').readFileSync(0, 'utf-8').trim().split(/\\s+/);
const a = parseInt(input[0], 10);
const b = parseInt(input[1], 10);
console.log(a + b);`
    }
  };
  sandboxCode = '';
  sandboxLoading = false;
  sandboxResult: RunCodeResult | null = null;

  constructor(
    private userService: UserService,
    private affectationService: AffectationTestService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.sandboxCode = this.sandboxConfig[this.sandboxLanguage].starterCode;
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.username = user.username || 'User';
        this.loading = false;
        if (!(user as any).profilePhoto && !(user as any).profilePicture) {
          this.canStartTest = false;
          this.errorMessage = 'You must upload a profile photo before starting the test. This photo is used for identity verification during the test.';
        } else {
          this.checkCanStartTest(user.id);
        }
      },
      error: () => {
        this.loading = false;
        this.canStartTest = false;
        this.errorMessage = 'Unable to load your profile.';
      }
    });
  }

  /** Change le langage du sandbox et met à jour l'éditeur avec le code de départ. */
  setSandboxLanguage(lang: 'java' | 'python' | 'javascript'): void {
    this.sandboxLanguage = lang;
    this.sandboxCode = this.sandboxConfig[lang].starterCode;
    this.sandboxResult = null;
  }

  private checkCanStartTest(userId: number): void {
    this.affectationService.getAffectationsByFreelancerId(userId).subscribe({
      next: (list) => {
        const hasValidated = list.some(a => a.isValidated);
        const failureCount = list.filter(a => !a.isValidated).length;
        if (hasValidated) {
          this.canStartTest = false;
          this.messageType = 'success';
          this.message = 'You have already passed the test. Congratulations! You cannot retake it.';
        } else if (failureCount >= 3) {
          this.canStartTest = false;
          this.messageType = 'warning';
          this.message = 'You have reached the maximum of 3 attempts. You can no longer retake the test.';
        }
      },
      error: () => {
        // On ignore l'erreur (ex. 404), le user peut tenter de démarrer
      }
    });
  }

  openRulesModal(): void {
    if (!this.canStartTest || this.starting) return;
    this.rulesAccepted = false;
    this.showRulesModal = true;
  }

  closeRulesModal(): void {
    this.showRulesModal = false;
  }

  confirmAndStartTest(): void {
    if (!this.rulesAccepted) return;
    this.showRulesModal = false;
    this.doStartTest();
  }

  startTest(): void {
    this.openRulesModal();
  }

  private doStartTest(): void {
    if (!this.canStartTest || this.starting) return;
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        if (!(user as any).profilePhoto && !(user as any).profilePicture) {
          this.starting = false;
          this.errorMessage = 'You must upload a profile photo before starting the test. This photo is used for identity verification during the test.';
          return;
        }
        this.starting = true;
        this.errorMessage = '';
        this.affectationService.startTest(user.id).subscribe({
          next: (affectation) => {
            this.router.navigate(['/test/pass'], { queryParams: { id: affectation.id } });
          },
          error: (err) => {
            this.starting = false;
            this.errorMessage = err?.error?.message || 'Unable to start the test. Please try again later.';
          }
        });
      },
      error: () => {
        this.errorMessage = 'Session expired. Please log in again.';
      }
    });
  }

  /** Exécute le code de la sandbox CODING (ne modifie pas les tentatives ni la note du test). */
  runSandbox(): void {
    const questionId = this.sandboxQuestionIds[this.sandboxLanguage];
    if (!questionId || this.sandboxLoading) return;
    this.sandboxLoading = true;
    this.sandboxResult = null;
    this.affectationService.runCode(questionId, this.sandboxCode).subscribe({
      next: (res) => {
        this.sandboxLoading = false;
        this.sandboxResult = res;
      },
      error: () => {
        this.sandboxLoading = false;
        this.sandboxResult = {
          success: false,
          score: 0,
          passedCount: 0,
          totalCount: 0,
          message: 'Erreur lors de l’exécution dans la sandbox.',
        };
      }
    });
  }
}
