import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from './services/auth.service';
import { TransactionService } from './services/transaction.service';

import { Transaction } from './models/transaction';

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  // =========================
  // AUTENTICAÇÃO
  // =========================

  isAuthenticated = false;

  currentUser: {
    id: string;
    name: string;
    email: string;
  } | null = null;

  loginData = {
    email: '',
    password: '',
  };

  loginLoading = false;
  loginError = '';

  // =========================
  // NAVEGAÇÃO
  // =========================

  activeSection: 'dashboard' | 'transactions' | 'assistant' = 'dashboard';

  // =========================
  // DASHBOARD
  // =========================

  groceriesTotal = 0;
  pharmaTotal = 0;
  autoTotal = 0;

  transactions: Transaction[] = [];

  // =========================
  // FILTROS
  // =========================

  searchTerm = '';
  selectedCategory = 'ALL';

  // =========================
  // NOVA TRANSAÇÃO
  // =========================

  showTransactionModal = false;

  newTransaction = {
    description: '',
    amount: 0,
    category: 'GROCERIES',
  };

  // =========================
  // ASSISTENTE IA
  // =========================

  showAiModal = false;

  aiMessage = '';
  aiResponse = '';

  aiLoading = false;

  // =========================
  // MICROFONE
  // =========================

  isRecording = false;

  private mediaRecorder?: MediaRecorder;
  private audioChunks: Blob[] = [];
  private mediaStream?: MediaStream;

  constructor(
    private authService: AuthService,
    private transactionService: TransactionService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.isAuthenticated = this.authService.isAuthenticated();

    if (this.isAuthenticated) {
      this.currentUser = this.authService.getUser();
      this.loadTransactions();
    }
  }

  // =========================
  // AUTENTICAÇÃO
  // =========================

  login(): void {
    const email = this.loginData.email.trim();
    const password = this.loginData.password;

    if (!email || !password) {
      this.loginError = 'Informe seu e-mail e sua senha.';
      return;
    }

    this.loginLoading = true;
    this.loginError = '';

    this.authService.login(email, password).subscribe({
      next: (response) => {
        this.isAuthenticated = true;
        this.currentUser = response.user;

        this.loginData = {
          email: '',
          password: '',
        };

        this.loginLoading = false;

        this.loadTransactions();

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao realizar login:', error);

        this.loginError = 'E-mail ou senha inválidos.';
        this.loginLoading = false;

        this.cdr.detectChanges();
      },
    });
  }

  logout(): void {
    if (this.isRecording) {
      this.stopRecording();
    }

    this.stopMicrophone();

    this.authService.logout();

    this.isAuthenticated = false;
    this.currentUser = null;

    this.activeSection = 'dashboard';

    this.transactions = [];

    this.groceriesTotal = 0;
    this.pharmaTotal = 0;
    this.autoTotal = 0;

    this.searchTerm = '';
    this.selectedCategory = 'ALL';

    this.showTransactionModal = false;
    this.showAiModal = false;

    this.aiMessage = '';
    this.aiResponse = '';

    this.cdr.detectChanges();
  }

  // =========================
  // NAVEGAÇÃO
  // =========================

  setSection(section: 'dashboard' | 'transactions' | 'assistant'): void {
    this.activeSection = section;

    if (section === 'assistant') {
      this.openAiModal();
    }
  }

  // =========================
  // CATEGORIAS
  // =========================

  getCategoryName(category: string): string {
    switch (category) {
      case 'GROCERIES':
        return 'Supermercado';

      case 'PHARMA':
        return 'Farmácia';

      case 'AUTO':
        return 'Automóvel';

      default:
        return category;
    }
  }

  // =========================
  // TRANSAÇÕES
  // =========================

  loadTransactions(): void {
    if (!this.isAuthenticated) {
      return;
    }

    this.transactions = [];

    this.groceriesTotal = 0;
    this.pharmaTotal = 0;
    this.autoTotal = 0;

    this.transactionService.getByCategory('GROCERIES').subscribe({
      next: (transactions) => {
        this.groceriesTotal = this.calculateTotal(transactions);

        this.transactions.push(...transactions);

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao carregar GROCERIES:', error);
      },
    });

    this.transactionService.getByCategory('PHARMA').subscribe({
      next: (transactions) => {
        this.pharmaTotal = this.calculateTotal(transactions);

        this.transactions.push(...transactions);

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao carregar PHARMA:', error);
      },
    });

    this.transactionService.getByCategory('AUTO').subscribe({
      next: (transactions) => {
        this.autoTotal = this.calculateTotal(transactions);

        this.transactions.push(...transactions);

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao carregar AUTO:', error);
      },
    });
  }

  private calculateTotal(transactions: Transaction[]): number {
    return transactions.reduce((total, transaction) => total + transaction.amount, 0);
  }

  get totalExpenses(): number {
    return this.groceriesTotal + this.pharmaTotal + this.autoTotal;
  }

  // =========================
  // FILTROS
  // =========================

  get filteredTransactions(): Transaction[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.transactions.filter((transaction) => {
      const matchesSearch = !search || transaction.description.toLowerCase().includes(search);

      const matchesCategory =
        this.selectedCategory === 'ALL' || transaction.category === this.selectedCategory;

      return matchesSearch && matchesCategory;
    });
  }

  get filteredTotal(): number {
    return this.filteredTransactions.reduce((total, transaction) => total + transaction.amount, 0);
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedCategory = 'ALL';
  }

  // =========================
  // NOVA TRANSAÇÃO
  // =========================

  openTransactionModal(): void {
    this.showTransactionModal = true;
  }

  closeTransactionModal(): void {
    this.showTransactionModal = false;

    this.newTransaction = {
      description: '',
      amount: 0,
      category: 'GROCERIES',
    };
  }

  createTransaction(): void {
    if (!this.newTransaction.description.trim() || this.newTransaction.amount <= 0) {
      return;
    }

    const transactionToSend = {
      description: this.newTransaction.description,

      amount: Math.round(this.newTransaction.amount * 100),

      category: this.newTransaction.category,
    };

    this.transactionService.create(transactionToSend).subscribe({
      next: () => {
        this.closeTransactionModal();

        this.loadTransactions();

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao cadastrar transação:', error);
      },
    });
  }

  // =========================
  // ASSISTENTE IA
  // =========================

  openAiModal(): void {
    this.showAiModal = true;
  }

  closeAiModal(): void {
    if (this.isRecording) {
      this.stopRecording();
    }

    this.showAiModal = false;

    this.aiMessage = '';
    this.aiResponse = '';

    if (this.activeSection === 'assistant') {
      this.activeSection = 'dashboard';
    }
  }

  sendAiMessage(): void {
    if (!this.aiMessage.trim()) {
      return;
    }

    this.aiLoading = true;
    this.aiResponse = '';

    this.transactionService.processWithAi(this.aiMessage).subscribe({
      next: (response) => {
        this.aiResponse = response;

        this.aiLoading = false;

        this.loadTransactions();

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao processar comando com IA:', error);

        this.aiResponse = 'Não foi possível processar sua solicitação.';

        this.aiLoading = false;

        this.cdr.detectChanges();
      },
    });
  }

  // =========================
  // MICROFONE
  // =========================

  async toggleRecording(): Promise<void> {
    if (this.isRecording) {
      this.stopRecording();
    } else {
      await this.startRecording();
    }
  }

  private async startRecording(): Promise<void> {
    try {
      this.aiResponse = '';

      this.mediaStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
      });

      this.audioChunks = [];

      this.mediaRecorder = new MediaRecorder(this.mediaStream);

      this.mediaRecorder.ondataavailable = (event: BlobEvent) => {
        if (event.data.size > 0) {
          this.audioChunks.push(event.data);
        }
      };

      this.mediaRecorder.onstop = () => {
        const mimeType = this.mediaRecorder?.mimeType || 'audio/webm';

        const audioBlob = new Blob(this.audioChunks, {
          type: mimeType,
        });

        this.stopMicrophone();

        this.sendAudio(audioBlob);
      };

      this.mediaRecorder.start();

      this.isRecording = true;

      this.cdr.detectChanges();
    } catch (error) {
      console.error('Erro ao acessar o microfone:', error);

      this.aiResponse = 'Não foi possível acessar o microfone. Verifique a permissão do navegador.';

      this.cdr.detectChanges();
    }
  }

  private stopRecording(): void {
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop();
    }

    this.isRecording = false;

    this.cdr.detectChanges();
  }

  private stopMicrophone(): void {
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach((track) => track.stop());

      this.mediaStream = undefined;
    }
  }

  private sendAudio(audioBlob: Blob): void {
    if (audioBlob.size === 0) {
      this.aiResponse = 'Nenhum áudio foi gravado.';

      this.cdr.detectChanges();

      return;
    }

    this.aiLoading = true;

    this.aiResponse = 'Processando comando de voz...';

    this.cdr.detectChanges();

    this.transactionService.processAudioWithAi(audioBlob).subscribe({
      next: (response) => {
        this.aiResponse = response;

        this.aiLoading = false;

        this.loadTransactions();

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao processar áudio:', error);

        this.aiResponse = 'Não foi possível processar o áudio.';

        this.aiLoading = false;

        this.cdr.detectChanges();
      },
    });
  }
}
