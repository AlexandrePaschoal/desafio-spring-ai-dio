import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

interface UserData {
  id: string;
  name: string;
  email: string;
  createdAt: string;
  initialBalance: number;
  initialBalanceDate: string;
}

interface LoginResponse {
  token: string;
  user: UserData;
}

interface RegisterResponse {
  id: string;
  name: string;
  email: string;
  createdAt: string;
  initialBalance: number;
  initialBalanceDate: string;
}

interface InitialBalanceResponse {
  initialBalance: number;
  initialBalanceDate: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/auth';
  private readonly usersApiUrl = 'http://localhost:8080/users';

  constructor(private http: HttpClient) {}

  // =========================
  // LOGIN
  // =========================

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, {
        email,
        password,
      })
      .pipe(
        tap((response) => {
          sessionStorage.setItem('token', response.token);
          sessionStorage.setItem('user', JSON.stringify(response.user));
        }),
      );
  }

  // =========================
  // CADASTRO
  // =========================

  register(name: string, email: string, password: string): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, {
      name,
      email,
      password,
    });
  }

  // =========================
  // SALDO INICIAL
  // =========================

  updateInitialBalance(
    initialBalance: number,
    initialBalanceDate: string,
  ): Observable<InitialBalanceResponse> {
    return this.http
      .patch<InitialBalanceResponse>(`${this.usersApiUrl}/me/initial-balance`, {
        initialBalance,
        initialBalanceDate,
      })
      .pipe(
        tap((response) => {
          const user = this.getUser();

          if (!user) {
            return;
          }

          const updatedUser: UserData = {
            ...user,
            initialBalance: response.initialBalance,
            initialBalanceDate: response.initialBalanceDate,
          };

          sessionStorage.setItem('user', JSON.stringify(updatedUser));
        }),
      );
  }

  // =========================
  // LOGOUT
  // =========================

  logout(): void {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
  }

  // =========================
  // TOKEN
  // =========================

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  // =========================
  // USUÁRIO ATUAL
  // =========================

  getUser(): UserData | null {
    const user = sessionStorage.getItem('user');

    if (!user) {
      return null;
    }

    return JSON.parse(user) as UserData;
  }
}
