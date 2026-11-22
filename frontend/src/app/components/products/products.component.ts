import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Product, ProductListResponse } from '../../models/product.model';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html'
})
export class ProductsComponent implements OnInit {
  productsData = signal<ProductListResponse | null>(null);
  loading = signal(false);
  message = signal<string | null>(null);

  // Form nuovo prodotto
  newName = '';
  newPrice: number | null = null;
  newDescription = '';
  submitting = signal(false);

  constructor(
    private productService: ProductService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.message.set(null);

    this.productService.getProducts().subscribe({
      next: (data) => {
        this.productsData.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.message.set('Errore nel caricamento prodotti: ' + (err.error?.error || err.message));
      }
    });
  }

  addProduct(): void {
    if (!this.newName || this.newPrice === null || this.newPrice < 0) {
      alert('Inserisci nome e prezzo valido');
      return;
    }

    this.submitting.set(true);
    const newProd: Product = {
      name: this.newName,
      price: this.newPrice,
      description: this.newDescription
    };

    this.productService.createProduct(newProd).subscribe({
      next: () => {
        this.submitting.set(false);
        this.newName = '';
        this.newPrice = null;
        this.newDescription = '';
        this.message.set('Prodotto aggiunto con successo! La cache Redis è stata invalidata.');
        // Ricarica la lista: poiché la cache è stata invalidata, questa chiamata leggerà da Postgres!
        this.loadProducts();
      },
      error: (err) => {
        this.submitting.set(false);
        alert('Errore salvataggio: ' + err.message);
      }
    });
  }

  clearRedisCache(): void {
    this.productService.clearCache().subscribe({
      next: (res) => {
        this.message.set(res.message + ' - Clicca "Ricarica Lista" per misurare il tempo da Postgres!');
      },
      error: (err) => alert('Errore svuotamento cache: ' + err.message)
    });
  }
}
