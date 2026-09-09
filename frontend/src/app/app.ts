import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from './services/auth.service';
import { TransactionService } from './services/transaction.service';
import { CategoryService } from './services/category.service';

import { Category } from './models/category';
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

  activeSection: 'dashboard' | 'transactions' | 'categories' | 'assistant' = 'dashboard';

  // =========================
  // DASHBOARD
  // =========================

  categories: Category[] = [];
  transactions: Transaction[] = [];

  categoryTotals: Record<string, number> = {};

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
    categoryId: '',
    type: 'EXPENSE' as 'INCOME' | 'EXPENSE',
    date: this.getTodayDate(),
    status: 'COMPLETED' as 'PENDING' | 'COMPLETED',
  };

  // =========================
  // CATEGORIAS
  // =========================

  showCategoryModal = false;

  newCategoryName = '';

  categoryLoading = false;
  categoryError = '';

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
    private categoryService: CategoryService,
  ) {}

  ngOnInit(): void {
    this.isAuthenticated = this.authService.isAuthenticated();

    if (this.isAuthenticated) {
      this.currentUser = this.authService.getUser();

      this.loadCategories();
    }
  }

  // =========================
  // UTILITÁRIOS
  // =========================

  private getTodayDate(): string {
    const today = new Date();

    const year = today.getFullYear();

    const month = String(today.getMonth() + 1).padStart(2, '0');

    const day = String(today.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  get transactionCompleted(): boolean {
    return this.newTransaction.status === 'COMPLETED';
  }

  set transactionCompleted(completed: boolean) {
    this.newTransaction.status = completed ? 'COMPLETED' : 'PENDING';
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

        this.loadCategories();

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

    this.categories = [];
    this.categoryTotals = {};

    this.searchTerm = '';
    this.selectedCategory = 'ALL';

    this.showTransactionModal = false;

    this.showCategoryModal = false;
    this.newCategoryName = '';
    this.categoryError = '';
    this.categoryLoading = false;

    this.showAiModal = false;

    this.aiMessage = '';
    this.aiResponse = '';

    this.cdr.detectChanges();
  }

  // =========================
  // NAVEGAÇÃO
  // =========================

  setSection(section: 'dashboard' | 'transactions' | 'categories' | 'assistant'): void {
    this.activeSection = section;

    if (section === 'assistant') {
      this.openAiModal();
    }
  }

  // =========================
  // CATEGORIAS
  // =========================

  loadCategories(): void {
    if (!this.isAuthenticated) {
      return;
    }

    this.categoryService.findAll().subscribe({
      next: (categories) => {
        this.categories = categories;

        const firstSelectableCategory = this.selectableCategories[0];

        if (!this.newTransaction.categoryId && firstSelectableCategory) {
          this.newTransaction.categoryId = firstSelectableCategory.id;
        }

        this.loadTransactions();

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao carregar categorias:', error);
      },
    });
  }

  get visibleCategories(): Category[] {
    return this.categories.filter((category) => {
      if (category.name.toLowerCase() !== 'sem categoria') {
        return true;
      }

      return this.transactions.some(
        (transaction) => transaction.category.toLowerCase() === 'sem categoria',
      );
    });
  }

  get selectableCategories(): Category[] {
    return this.categories.filter((category) => category.name.toLowerCase() !== 'sem categoria');
  }

  openCategoryModal(): void {
    this.newCategoryName = '';
    this.categoryError = '';
    this.showCategoryModal = true;
  }

  closeCategoryModal(): void {
    this.showCategoryModal = false;
    this.newCategoryName = '';
    this.categoryError = '';
  }

  createCategory(): void {
    const name = this.newCategoryName.trim();

    if (!name) {
      this.categoryError = 'Informe o nome da categoria.';

      return;
    }

    this.categoryLoading = true;
    this.categoryError = '';

    this.categoryService.create(name).subscribe({
      next: () => {
        this.categoryLoading = false;

        this.closeCategoryModal();

        this.loadCategories();

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao criar categoria:', error);

        this.categoryError = error.error?.message || 'Não foi possível criar a categoria.';

        this.categoryLoading = false;

        this.cdr.detectChanges();
      },
    });
  }

  deleteCategory(category: Category): void {
    if (category.name.toLowerCase() === 'sem categoria') {
      return;
    }

    const confirmed = window.confirm(
      `Deseja realmente excluir a categoria "${category.name}"?\n\n` +
        `As transações dessa categoria serão movidas para "Sem categoria".`,
    );

    if (!confirmed) {
      return;
    }

    this.categoryLoading = true;
    this.categoryError = '';

    this.categoryService.delete(category.id).subscribe({
      next: () => {
        this.categoryLoading = false;

        this.loadCategories();

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao excluir categoria:', error);

        this.categoryError = error.error?.message || 'Não foi possível excluir a categoria.';

        this.categoryLoading = false;

        this.cdr.detectChanges();
      },
    });
  }

  // =========================
  // TRANSAÇÕES
  // =========================

  loadTransactions(): void {
    if (!this.isAuthenticated) {
      return;
    }

    this.transactions = [];
    this.categoryTotals = {};

    for (const category of this.categories) {
      this.transactionService.getByCategory(category.id).subscribe({
        next: (transactions) => {
          this.categoryTotals[category.id] = this.calculateExpenseTotal(transactions);

          this.transactions.push(...transactions);

          this.cdr.detectChanges();
        },

        error: (error) => {
          console.error(`Erro ao carregar categoria ${category.name}:`, error);
        },
      });
    }
  }

  private calculateExpenseTotal(transactions: Transaction[]): number {
    return transactions
      .filter((transaction) => transaction.type === 'EXPENSE')
      .reduce((total, transaction) => total + transaction.amount, 0);
  }

  // =========================
  // RESUMO FINANCEIRO
  // =========================

  get totalIncome(): number {
    return this.transactions
      .filter((transaction) => transaction.type === 'INCOME')
      .reduce((total, transaction) => total + transaction.amount, 0);
  }

  get totalExpenses(): number {
    return this.transactions
      .filter((transaction) => transaction.type === 'EXPENSE')
      .reduce((total, transaction) => total + transaction.amount, 0);
  }

  get balance(): number {
    return this.totalIncome - this.totalExpenses;
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
    return this.filteredTransactions.reduce((total, transaction) => {
      if (transaction.type === 'INCOME') {
        return total + transaction.amount;
      }

      return total - transaction.amount;
    }, 0);
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedCategory = 'ALL';
  }

  // =========================
  // NOVA TRANSAÇÃO
  // =========================

  openTransactionModal(): void {
    if (!this.newTransaction.categoryId && this.selectableCategories.length > 0) {
      this.newTransaction.categoryId = this.selectableCategories[0].id;
    }

    if (!this.newTransaction.date) {
      this.newTransaction.date = this.getTodayDate();
    }

    this.showTransactionModal = true;
  }

  closeTransactionModal(): void {
    this.showTransactionModal = false;

    this.newTransaction = {
      description: '',
      amount: 0,

      categoryId: this.selectableCategories.length > 0 ? this.selectableCategories[0].id : '',

      type: 'EXPENSE',

      date: this.getTodayDate(),

      status: 'COMPLETED',
    };
  }

  createTransaction(): void {
    if (
      !this.newTransaction.description.trim() ||
      this.newTransaction.amount <= 0 ||
      !this.newTransaction.categoryId ||
      !this.newTransaction.date
    ) {
      return;
    }

    const transactionToSend = {
      description: this.newTransaction.description.trim(),

      amount: Math.round(this.newTransaction.amount * 100),

      categoryId: this.newTransaction.categoryId,

      type: this.newTransaction.type,

      date: this.newTransaction.date,

      status: this.newTransaction.status,
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
