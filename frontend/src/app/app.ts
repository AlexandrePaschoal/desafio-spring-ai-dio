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

  // =========================
  // LOGIN
  // =========================

  loginData = {
    email: '',
    password: '',
  };

  loginLoading = false;
  loginError = '';

  // =========================
  // CADASTRO
  // =========================

  isRegisterMode = false;

  registerData = {
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  };

  registerLoading = false;
  registerError = '';
  registerSuccess = '';

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
  // TRANSAÇÃO
  // =========================

  showTransactionModal = false;

  editingTransactionId: string | null = null;

  transactionLoading = false;
  transactionError = '';

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

  selectedCategoryView: Category | null = null;

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

  get isEditingTransaction(): boolean {
    return this.editingTransactionId !== null;
  }

  // =========================
  // AUTENTICAÇÃO
  // =========================

  showRegister(): void {
    this.isRegisterMode = true;

    this.loginError = '';
    this.registerError = '';
    this.registerSuccess = '';
  }

  showLogin(): void {
    this.isRegisterMode = false;

    this.loginError = '';
    this.registerError = '';

    this.registerData = {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
    };
  }

  register(): void {
    const name = this.registerData.name.trim();
    const email = this.registerData.email.trim();
    const password = this.registerData.password;
    const confirmPassword = this.registerData.confirmPassword;

    this.registerError = '';
    this.registerSuccess = '';

    if (!name || !email || !password || !confirmPassword) {
      this.registerError = 'Preencha todos os campos.';

      return;
    }

    if (password !== confirmPassword) {
      this.registerError = 'As senhas não coincidem.';

      return;
    }

    if (password.length < 6) {
      this.registerError = 'A senha deve possuir pelo menos 6 caracteres.';

      return;
    }

    this.registerLoading = true;

    this.authService.register(name, email, password).subscribe({
      next: () => {
        this.registerLoading = false;

        this.registerSuccess = 'Conta criada com sucesso. Agora você pode entrar.';

        this.registerData = {
          name: '',
          email: '',
          password: '',
          confirmPassword: '',
        };

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao cadastrar usuário:', error);

        this.registerError = error.error?.message || 'Não foi possível criar sua conta.';

        this.registerLoading = false;

        this.cdr.detectChanges();
      },
    });
  }

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

        this.isRegisterMode = false;

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

    this.isRegisterMode = false;

    this.loginData = {
      email: '',
      password: '',
    };

    this.registerData = {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
    };

    this.loginError = '';
    this.registerError = '';
    this.registerSuccess = '';
    this.loginLoading = false;
    this.registerLoading = false;

    this.activeSection = 'dashboard';

    this.transactions = [];

    this.categories = [];
    this.categoryTotals = {};

    this.searchTerm = '';
    this.selectedCategory = 'ALL';

    this.showTransactionModal = false;
    this.editingTransactionId = null;
    this.transactionLoading = false;
    this.transactionError = '';

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

  openCategoryView(category: Category): void {
    this.selectedCategoryView = category;
  }

  closeCategoryView(): void {
    this.selectedCategoryView = null;
  }

  getTransactionsByCategory(categoryName: string): Transaction[] {
    return this.transactions
      .filter((transaction) => transaction.category === categoryName)
      .sort((a, b) => b.date.localeCompare(a.date));
  }

  getCategoryTransactionCount(categoryName: string): number {
    return this.getTransactionsByCategory(categoryName).length;
  }

  getCategoryIncome(categoryName: string): number {
    return this.getTransactionsByCategory(categoryName)
      .filter((transaction) => transaction.type === 'INCOME')
      .reduce((total, transaction) => total + transaction.amount, 0);
  }

  getCategoryExpenses(categoryName: string): number {
    return this.getTransactionsByCategory(categoryName)
      .filter((transaction) => transaction.type === 'EXPENSE')
      .reduce((total, transaction) => total + transaction.amount, 0);
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

  // RECEITAS TOTAIS
  get totalIncome(): number {
    return this.transactions
      .filter((transaction) => transaction.type === 'INCOME')
      .reduce((total, transaction) => total + transaction.amount, 0);
  }

  // DESPESAS TOTAIS
  get totalExpenses(): number {
    return this.transactions
      .filter((transaction) => transaction.type === 'EXPENSE')
      .reduce((total, transaction) => total + transaction.amount, 0);
  }

  // RECEITAS JÁ RECEBIDAS
  get receivedIncome(): number {
    return this.transactions
      .filter((transaction) => transaction.type === 'INCOME' && transaction.status === 'COMPLETED')
      .reduce((total, transaction) => total + transaction.amount, 0);
  }

  // DESPESAS JÁ PAGAS
  get paidExpenses(): number {
    return this.transactions
      .filter((transaction) => transaction.type === 'EXPENSE' && transaction.status === 'COMPLETED')
      .reduce((total, transaction) => total + transaction.amount, 0);
  }

  // SALDO ATUAL
  get currentBalance(): number {
    return this.receivedIncome - this.paidExpenses;
  }

  // SALDO PROJETADO
  get projectedBalance(): number {
    return this.totalIncome - this.totalExpenses;
  }

  get balance(): number {
    return this.currentBalance;
  }

  // =========================
  // FILTROS
  // =========================

  get filteredTransactions(): Transaction[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.transactions
      .filter((transaction) => {
        const matchesSearch = !search || transaction.description.toLowerCase().includes(search);

        const matchesCategory =
          this.selectedCategory === 'ALL' || transaction.category === this.selectedCategory;

        return matchesSearch && matchesCategory;
      })
      .sort((a, b) => {
        const dateComparison = b.date.localeCompare(a.date);

        if (dateComparison !== 0) {
          return dateComparison;
        }

        return a.description.localeCompare(b.description, 'pt-BR');
      });
  }

  get recentTransactions(): Transaction[] {
    return [...this.transactions]
      .sort((a, b) => {
        const dateComparison = b.date.localeCompare(a.date);

        if (dateComparison !== 0) {
          return dateComparison;
        }

        return a.description.localeCompare(b.description, 'pt-BR');
      })
      .slice(0, 5);
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
    this.editingTransactionId = null;
    this.transactionError = '';

    this.newTransaction = {
      description: '',
      amount: 0,

      categoryId: this.selectableCategories.length > 0 ? this.selectableCategories[0].id : '',

      type: 'EXPENSE',

      date: this.getTodayDate(),

      status: 'COMPLETED',
    };

    this.showTransactionModal = true;
  }

  // =========================
  // EDITAR TRANSAÇÃO
  // =========================

  openEditTransaction(transaction: Transaction): void {
    const category = this.categories.find(
      (item) => item.name.toLowerCase() === transaction.category.toLowerCase(),
    );

    if (!category) {
      this.transactionError = 'A categoria da transação não foi encontrada.';

      return;
    }

    this.editingTransactionId = transaction.id;

    this.transactionError = '';

    this.newTransaction = {
      description: transaction.description,
      amount: transaction.amount,
      categoryId: category.id,
      type: transaction.type,
      date: transaction.date,
      status: transaction.status,
    };

    this.showTransactionModal = true;

    this.cdr.detectChanges();
  }

  closeTransactionModal(): void {
    this.showTransactionModal = false;

    this.editingTransactionId = null;
    this.transactionLoading = false;
    this.transactionError = '';

    this.newTransaction = {
      description: '',
      amount: 0,

      categoryId: this.selectableCategories.length > 0 ? this.selectableCategories[0].id : '',

      type: 'EXPENSE',

      date: this.getTodayDate(),

      status: 'COMPLETED',
    };
  }

  // =========================
  // SALVAR TRANSAÇÃO
  // =========================

  createTransaction(): void {
    if (
      !this.newTransaction.description.trim() ||
      this.newTransaction.amount <= 0 ||
      !this.newTransaction.categoryId ||
      !this.newTransaction.date
    ) {
      this.transactionError = 'Preencha todos os campos obrigatórios.';

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

    this.transactionLoading = true;
    this.transactionError = '';

    // =========================
    // EDIÇÃO
    // =========================

    if (this.editingTransactionId) {
      this.transactionService.update(this.editingTransactionId, transactionToSend).subscribe({
        next: () => {
          this.transactionLoading = false;

          this.closeTransactionModal();

          this.loadTransactions();

          this.cdr.detectChanges();
        },

        error: (error) => {
          console.error('Erro ao editar transação:', error);

          this.transactionError = error.error?.message || 'Não foi possível editar a transação.';

          this.transactionLoading = false;

          this.cdr.detectChanges();
        },
      });

      return;
    }

    // =========================
    // CRIAÇÃO
    // =========================

    this.transactionService.create(transactionToSend).subscribe({
      next: () => {
        this.transactionLoading = false;

        this.closeTransactionModal();

        this.loadTransactions();

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao cadastrar transação:', error);

        this.transactionError = error.error?.message || 'Não foi possível cadastrar a transação.';

        this.transactionLoading = false;

        this.cdr.detectChanges();
      },
    });
  }

  // =========================
  // EXCLUIR TRANSAÇÃO
  // =========================

  deleteTransaction(transaction: Transaction): void {
    const confirmed = window.confirm(
      `Deseja realmente excluir a transação "${transaction.description}"?`,
    );

    if (!confirmed) {
      return;
    }

    this.transactionService.delete(transaction.id).subscribe({
      next: () => {
        this.loadTransactions();

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao excluir transação:', error);

        window.alert('Não foi possível excluir a transação.');
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
