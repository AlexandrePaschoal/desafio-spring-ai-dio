export interface Transaction {
  id: string;
  category: string;
  description: string;
  type: 'INCOME' | 'EXPENSE';
  amount: number;
}
