import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  isRegister = signal(false);

  // Campi form
  email = '';
  password = '';
  fullName = '';

  errorMessage = signal<string | null>(null);
  loading = signal(false);

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/products']);
    }
  }

  toggleMode(): void {
    this.isRegister.update(v => !v);
    this.errorMessage.set(null);
  }

  onSubmit(): void {
    this.errorMessage.set(null);
    this.loading.set(true);

    if (this.isRegister()) {
      this.authService.register({
        email: this.email,
        password: this.password,
        fullName: this.fullName
      }).subscribe({
        next: () => this.router.navigate(['/products']),
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(err.error?.error || 'Errore durante la registrazione');
        }
      });
    } else {
      this.authService.login({
        email: this.email,
        password: this.password
      }).subscribe({
        next: () => this.router.navigate(['/products']),
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(err.error?.error || 'Email o password errati');
        }
      });
    }
  }

  fillAdminCredentials(): void {
    this.email = 'admin@example.com';
    this.password = 'Admin123!';
    this.isRegister.set(false);
  }
}
