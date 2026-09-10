import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Transaction } from '../models/transaction';

@Injectable({
  providedIn: 'root',
})
export class TransactionService {
  private readonly apiUrl = 'http://localhost:8080/transactions';

  constructor(private http: HttpClient) {}

  getByCategory(categoryId: string): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/${categoryId}`);
  }

  create(transaction: {
    description: string;
    amount: number;
    categoryId: string;
    type: 'INCOME' | 'EXPENSE';
    date: string;
    status: 'PENDING' | 'COMPLETED';
  }): Observable<Transaction> {
    return this.http.post<Transaction>(this.apiUrl, transaction);
  }

  update(
    transactionId: string,
    transaction: {
      description: string;
      amount: number;
      categoryId: string;
      type: 'INCOME' | 'EXPENSE';
      date: string;
      status: 'PENDING' | 'COMPLETED';
    },
  ): Observable<Transaction> {
    return this.http.put<Transaction>(`${this.apiUrl}/${transactionId}`, transaction);
  }

  delete(transactionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${transactionId}`);
  }

  processWithAi(message: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/ai`, message, {
      headers: {
        'Content-Type': 'text/plain',
      },
      responseType: 'text',
    });
  }

  processAudioWithAi(audioBlob: Blob): Observable<string> {
    const formData = new FormData();

    formData.append('file', audioBlob, 'audio.webm');

    return this.http.post(`${this.apiUrl}/ai/audio`, formData, {
      responseType: 'text',
    });
  }
}
