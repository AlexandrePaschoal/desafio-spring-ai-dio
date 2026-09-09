export interface Transaction {
  id: string;
  category: string;
  description: string;
  type: 'INCOME' | 'EXPENSE';
  date: string;
  status: 'PENDING' | 'COMPLETED';
  amount: number;
}
