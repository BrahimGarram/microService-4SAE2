import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { Router } from '@angular/router';
import { SubscriptionService, SubscriptionPlan, Subscription } from '../../services/subscription.service';
import { ToastService } from '../../services/toast.service';

interface ChatMessage {
  text: string;
  isUser: boolean;
  time: string;
}

@Component({
  selector: 'app-subscription-plans',
  templateUrl: './subscription-plans.component.html',
  styleUrls: ['./subscription-plans.component.css']
})
export class SubscriptionPlansComponent implements OnInit, AfterViewChecked {
  @ViewChild('chatMessages') private chatMessagesContainer!: ElementRef;
  
  plans: SubscriptionPlan[] = [];
  currentSubscription: Subscription | null = null;
  loading = true;
  subscribing = false;
  userId: number = 0;
  billingCycle: 'MONTHLY' | 'YEARLY' = 'MONTHLY';
  isYearly = false;

  // Chat properties
  isChatOpen = false;
  userMessage = '';
  chatHistory: ChatMessage[] = [];
  isTyping = false;
  showSuggestions = true;
  suggestions = [
    'What plan is best for me?',
    'Compare PRO vs ENTERPRISE',
    'Tell me about features',
    'How does billing work?'
  ];
  private shouldScrollToBottom = false;

  constructor(
    public subscriptionService: SubscriptionService,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Get user ID from localStorage or your auth service
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      this.userId = user.id;
      this.loadCurrentSubscription();
    }
    this.loadPlans();
    this.initializeChat();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  initializeChat(): void {
    this.chatHistory = [
      {
        text: 'Hi! 👋 I\'m your AI assistant. I can help you choose the perfect subscription plan. What would you like to know?',
        isUser: false,
        time: this.getCurrentTime()
      }
    ];
  }

  toggleChat(): void {
    this.isChatOpen = !this.isChatOpen;
    if (this.isChatOpen) {
      this.shouldScrollToBottom = true;
    }
  }

  sendMessage(): void {
    if (!this.userMessage.trim() || this.isTyping) return;

    const message = this.userMessage.trim();
    this.chatHistory.push({
      text: message,
      isUser: true,
      time: this.getCurrentTime()
    });

    this.userMessage = '';
    this.showSuggestions = false;
    this.shouldScrollToBottom = true;

    // Simulate AI typing
    this.isTyping = true;
    setTimeout(() => {
      const response = this.getAIResponse(message);
      this.chatHistory.push({
        text: response,
        isUser: false,
        time: this.getCurrentTime()
      });
      this.isTyping = false;
      this.shouldScrollToBottom = true;
    }, 1500);
  }

  sendSuggestion(suggestion: string): void {
    this.userMessage = suggestion;
    this.sendMessage();
  }

  getAIResponse(message: string): string {
    const lowerMessage = message.toLowerCase();

    // Plan recommendations
    if (lowerMessage.includes('best') || lowerMessage.includes('recommend') || lowerMessage.includes('which plan')) {
      return 'Great question! 🎯 For most freelancers starting out, I recommend the FREE plan to get familiar with the platform. If you\'re actively bidding on projects, the PRO plan offers unlimited bids and priority support. For agencies or teams, ENTERPRISE provides custom solutions. What\'s your main use case?';
    }

    // Compare plans
    if (lowerMessage.includes('compare') || lowerMessage.includes('difference') || lowerMessage.includes('vs')) {
      return 'Let me break down the key differences! 📊\n\nPRO: Unlimited bids, 50 active projects, 8% commission, analytics + priority support.\n\nENTERPRISE: Everything in PRO plus unlimited projects, 5% commission, featured listings, and dedicated account executive.\n\nThe main difference is scale and support level. Need help deciding?';
    }

    // Features
    if (lowerMessage.includes('feature') || lowerMessage.includes('include') || lowerMessage.includes('what do')) {
      return 'All plans include core features like project management and messaging. 💼 Higher tiers add:\n\n✨ Analytics dashboard\n🚀 Profile boost\n⭐ Priority support\n🎯 Featured listings\n\nPlus lower commission rates! Which feature interests you most?';
    }

    // Billing
    if (lowerMessage.includes('billing') || lowerMessage.includes('payment') || lowerMessage.includes('charge')) {
      return 'Our billing is simple and transparent! 💳\n\n• Monthly or yearly billing (save 20% annually)\n• 14-day free trial on paid plans\n• Cancel anytime, no questions asked\n• Secure payment processing\n\nYou can upgrade or downgrade anytime. Want to know more about a specific plan?';
    }

    // FREE plan
    if (lowerMessage.includes('free')) {
      return 'The FREE plan is perfect for getting started! 🎁\n\n• 10 bids per month\n• 5 active projects\n• 15% commission\n• Basic features\n\nIt\'s great for testing the platform. Ready to explore paid plans for more features?';
    }

    // PRO plan
    if (lowerMessage.includes('pro')) {
      return 'The PRO plan is our most popular choice! 🚀\n\n• Unlimited bids\n• 50 active projects\n• 8% commission (almost half of FREE!)\n• Analytics dashboard\n• Profile boost\n• Priority support\n\nPerfect for active freelancers. Want to subscribe?';
    }

    // ENTERPRISE plan
    if (lowerMessage.includes('enterprise')) {
      return 'ENTERPRISE is designed for serious professionals! 👑\n\n• Everything in PRO\n• Unlimited projects\n• 5% commission (lowest rate!)\n• Featured listings\n• Dedicated account executive\n• White-glove onboarding\n\nIdeal for agencies and high-volume freelancers. Interested in learning more?';
    }

    // Commission
    if (lowerMessage.includes('commission') || lowerMessage.includes('fee')) {
      return 'Commission rates vary by plan:\n\n💰 FREE: 15%\n💰 BASIC: 12%\n💰 PRO: 8%\n💰 ENTERPRISE: 5%\n\nHigher plans = lower fees = more money in your pocket! Calculate your potential savings?';
    }

    // Trial
    if (lowerMessage.includes('trial')) {
      return 'Yes! All paid plans come with a 14-day free trial. 🎉 No credit card required upfront. Try all the premium features risk-free. If you\'re not satisfied, cancel anytime during the trial. Which plan would you like to try?';
    }

    // Upgrade/Downgrade
    if (lowerMessage.includes('upgrade') || lowerMessage.includes('downgrade') || lowerMessage.includes('change')) {
      return 'You can change plans anytime! 🔄\n\n• Upgrades take effect immediately\n• Downgrades apply at next billing cycle\n• Pro-rated billing for fairness\n• No penalties for switching\n\nFlexibility is key! Need help choosing a new plan?';
    }

    // Default response
    return 'I\'m here to help! 😊 I can answer questions about:\n\n• Plan features and pricing\n• Billing and payments\n• Comparing different plans\n• Recommendations based on your needs\n\nWhat would you like to know?';
  }

  getCurrentTime(): string {
    const now = new Date();
    return now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }

  scrollToBottom(): void {
    try {
      if (this.chatMessagesContainer) {
        this.chatMessagesContainer.nativeElement.scrollTop = 
          this.chatMessagesContainer.nativeElement.scrollHeight;
      }
    } catch (err) {
      console.error('Scroll error:', err);
    }
  }

  loadPlans(): void {
    this.subscriptionService.getAllPlans().subscribe({
      next: (plans) => {
        this.plans = plans.sort((a, b) => a.price - b.price);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading plans:', error);
        this.toastService.error('Failed to load subscription plans');
        this.loading = false;
      }
    });
  }

  loadCurrentSubscription(): void {
    this.subscriptionService.getActiveSubscription(this.userId).subscribe({
      next: (subscription) => {
        this.currentSubscription = subscription;
      },
      error: () => {
        // No active subscription
        this.currentSubscription = null;
      }
    });
  }

  subscribe(plan: SubscriptionPlan): void {
    if (!this.userId) {
      this.toastService.warning('Please login to subscribe');
      this.router.navigate(['/login']);
      return;
    }

    if (this.currentSubscription && this.currentSubscription.plan.id === plan.id) {
      this.toastService.info('You are already subscribed to this plan');
      return;
    }

    this.subscribing = true;

    if (this.currentSubscription) {
      // Upgrade/Downgrade
      this.changePlan(plan);
    } else {
      // New subscription
      this.createNewSubscription(plan);
    }
  }

  createNewSubscription(plan: SubscriptionPlan): void {
    this.subscriptionService.createSubscription(this.userId, plan.id).subscribe({
      next: (response) => {
        this.toastService.success('Subscription created! Redirecting to payment...');
        
        // Simulate payment for FREE plan
        if (plan.price === 0) {
          this.simulatePayment(response.subscription.id, plan.price);
        } else {
          // In production, redirect to actual payment gateway
          console.log('Payment URL:', response.paymentUrl);
          // window.location.href = response.paymentUrl;
          
          // For demo, simulate payment after 2 seconds
          setTimeout(() => {
            this.simulatePayment(response.subscription.id, plan.price);
          }, 2000);
        }
      },
      error: (error) => {
        console.error('Error creating subscription:', error);
        this.toastService.error(error.error?.error || 'Failed to create subscription');
        this.subscribing = false;
      }
    });
  }

  changePlan(newPlan: SubscriptionPlan): void {
    const isUpgrade = newPlan.price > (this.currentSubscription?.plan.price || 0);
    const action = isUpgrade ? 'Upgrading' : 'Downgrading';

    this.subscriptionService.changePlan(this.userId, newPlan.id, true).subscribe({
      next: (response) => {
        this.toastService.success(`${action} to ${newPlan.planName} successful!`);
        this.currentSubscription = response.subscription;
        this.subscribing = false;
        this.router.navigate(['/my-subscription']);
      },
      error: (error) => {
        console.error('Error changing plan:', error);
        this.toastService.error(error.error?.error || 'Failed to change plan');
        this.subscribing = false;
      }
    });
  }

  simulatePayment(subscriptionId: number, amount: number): void {
    this.subscriptionService.simulatePaymentWebhook(subscriptionId, 'SUCCESS', amount).subscribe({
      next: () => {
        this.toastService.success('Payment successful! Subscription activated.');
        this.subscribing = false;
        this.loadCurrentSubscription();
        this.router.navigate(['/my-subscription']);
      },
      error: (error) => {
        console.error('Error processing payment:', error);
        this.toastService.error('Payment processing failed');
        this.subscribing = false;
      }
    });
  }

  isCurrentPlan(plan: SubscriptionPlan): boolean {
    return this.currentSubscription?.plan.id === plan.id;
  }

  canUpgrade(plan: SubscriptionPlan): boolean {
    if (!this.currentSubscription) return true;
    return plan.price > this.currentSubscription.plan.price;
  }

  canDowngrade(plan: SubscriptionPlan): boolean {
    if (!this.currentSubscription) return false;
    return plan.price < this.currentSubscription.plan.price;
  }

  getButtonText(plan: SubscriptionPlan): string {
    if (this.isCurrentPlan(plan)) return 'Current Plan';
    if (!this.currentSubscription) return 'Subscribe';
    if (this.canUpgrade(plan)) return 'Upgrade';
    if (this.canDowngrade(plan)) return 'Downgrade';
    return 'Subscribe';
  }

  getButtonClass(plan: SubscriptionPlan): string {
    if (this.isCurrentPlan(plan)) return 'btn-secondary';
    if (this.canUpgrade(plan)) return 'btn-success';
    if (this.canDowngrade(plan)) return 'btn-warning';
    return 'btn-primary';
  }

  getPlanIcon(planName: string): string {
    const icons: { [key: string]: string } = {
      'FREE': 'fa-gift',
      'BASIC': 'fa-star',
      'PRO': 'fa-rocket',
      'ENTERPRISE': 'fa-crown'
    };
    return icons[planName] || 'fa-box';
  }

  getButtonIcon(plan: SubscriptionPlan): string {
    if (this.isCurrentPlan(plan)) return 'fa-check';
    if (this.canUpgrade(plan)) return 'fa-arrow-up';
    if (this.canDowngrade(plan)) return 'fa-arrow-down';
    return 'fa-lightning-bolt';
  }

  getDefaultDescription(planName: string): string {
    const descriptions: { [key: string]: string } = {
      'FREE': 'Everything you need to get started',
      'BASIC': 'Everything in the Free plan plus',
      'PRO': 'Everything in the Basic plan plus',
      'ENTERPRISE': 'Custom needs. Everything in the Business plan plus'
    };
    return descriptions[planName] || 'Choose this plan';
  }

  toggleBillingCycle(): void {
    this.billingCycle = this.isYearly ? 'YEARLY' : 'MONTHLY';
  }

  getDisplayPrice(plan: SubscriptionPlan): string {
    if (plan.price === 0) return 'Free';
    
    if (this.billingCycle === 'YEARLY') {
      // Calculate yearly price with 20% discount
      const yearlyPrice = plan.price * 12 * 0.8;
      const monthlyEquivalent = yearlyPrice / 12;
      return monthlyEquivalent.toFixed(0);
    }
    
    return plan.price.toFixed(0);
  }

  getPlanDescription(planName: string): string {
    const descriptions: { [key: string]: string } = {
      'FREE': 'Everything you need to get started',
      'BASIC': 'Everything in the Startup plan plus',
      'PRO': 'Everything in the Business plan plus',
      'ENTERPRISE': 'Custom needs. Everything in the Business plan plus'
    };
    return descriptions[planName] || 'Choose this plan';
  }

  getBillingCycleText(cycle: string): string {
    return cycle.charAt(0) + cycle.slice(1).toLowerCase();
  }
}
