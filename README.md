# 🚀 Enterprise FullStack Architecture Showcase

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange?logo=openjdk)](https://adoptium.net)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-18-red?logo=angular)](https://angular.dev)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://www.docker.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org)
[![Redis](https://img.shields.io/badge/Redis-7%20Alpine-red?logo=redis)](https://redis.io)
[![Security](https://img.shields.io/badge/OWASP-Argon2id%20%2B%20JWT-success?logo=securityscorecard)](https://owasp.org)

Progetto di riferimento architetturale che dimostra come strutturare e orchestrare un'applicazione full-stack enterprise moderna, scalabile e sicura. 

Tutti i componenti sono isolati in **container Docker indipendenti**, pronti per il deploy e configurati secondo le best practice industriali.

---

## 🏛️ Architettura del Sistema

```
                              [ BROWSER UTENTE ]
                                      │
                                      ▼ (Porta 80)
                   ┌──────────────────────────────────────┐
                   │ CONTAINER 1: FRONTEND (Angular 18)   │
                   │ - Servito da Nginx                   │
                   │ - Single Page App (Signals, Standalone)
                   │ - Reverse Proxy interno per /api/    │
                   └──────────────────┬───────────────────┘
                                      │ Inoltro interno (Docker Network)
                                      │ Header: "Authorization: Bearer <JWT>"
                                      ▼ (Porta 8080)
                   ┌──────────────────────────────────────┐
                   │ CONTAINER 2: BACKEND (Spring Boot 3) │
                   │ - Security Filter Chain Stateless    │
                   │ - Hashing password con Argon2id      │
                   │ - Cache-Aside pattern con Redis      │
                   │ - Spring Data JPA & DTO Validation   │
                   └──────────┬───────────────────┬───────┘
                              │                   │
     (Persistenza Relazionale)│                   │(Cache in-memory ultra-rapida)
                              ▼                   ▼
                 ┌─────────────────┐ ┌─────────────────┐
                 │  CONTAINER 3:   │ │  CONTAINER 4:   │
                 │   PostgreSQL    │ │      Redis      │
                 │ (Volume su disk)│ │ (Volume su disk)│
                 └─────────────────┘ └─────────────────┘
```

---

## 💡 Scelte Architetturali e di Design (The "Why")

### 1. Sicurezza: Argon2id invece del tradizionale bcrypt
* **Il Motivo**: Le funzioni come bcrypt o PBKDF2 sono solo *CPU-bound*. Gli attaccanti moderni possono parallelizzare gli attacchi brute-force usando cluster di GPU o chip ASIC dedicati.
* **La Scelta**: È stato adottato **Argon2id** (vincitore della *Password Hashing Competition* e raccomandato da OWASP e RFC 9106). Essendo un algoritmo *Memory-Hard* (configurato con 64 MB di RAM per singolo hash), neutralizza i vantaggi hardware delle GPU rendendo gli attacchi impraticabili.

### 2. Autenticazione: Stateless JWT
* **Il Motivo**: In un'architettura a container scalabile (es. Kubernetes con più repliche del backend), memorizzare le sessioni utente nella memoria RAM del server richiederebbe *Sticky Sessions* o session clustering complessi.
* **La Scelta**: Autenticazione completamente **Stateless** con token **JSON Web Token (JWT)** firmati con HMAC-SHA256. Ogni richiesta contiene le credenziali crittografiche necessarie per autorizzare l'accesso in modo indipendente su qualsiasi istanza.

### 3. Caching: Pattern Cache-Aside con Invalidation
* **Il Motivo**: Il 90% del carico tipico di un'applicazione è costituito da query di lettura (`SELECT`). Eseguire costantemente query su database relazionali satura le risorse I/O del disco.
* **La Scelta**: Implementazione manuale ed esplicita del pattern **Cache-Aside**:
  * **Lettura**: Controlla prima Redis (RAM). In caso di **Cache Hit**, i dati vengono restituiti in ~2 ms. In caso di **Cache Miss**, interroga PostgreSQL (~45 ms) e popola la cache con un TTL (Time-To-Live).
  * **Scrittura**: Al salvataggio di un nuovo elemento, la cache viene immediatamente invalidata (*Cache Eviction*) per garantire consistenza e prevenire letture obsolete (*stale data*).

### 4. Frontend & Rete: Nginx come Reverse Proxy
* **Il Motivo**: Far chiamare al browser direttamente la porta 8080 del backend creerebbe complicazioni di configurazione CORS, esposizione non necessaria di porte interne e disallineamento con gli ambienti di produzione.
* **La Scelta**: **Nginx** serve i file statici compilati di Angular sulla porta 80 e funge da **Reverse Proxy**: intercetta tutte le chiamate `/api/` e le inoltra internamente al container del backend via DNS Docker (`http://backend:8080/api/`). Questo azzera i problemi di CORS e replica fedelmente un'infrastruttura di Ingress enterprise.

---

## 🔒 Nota di Sicurezza per la Produzione

> [!IMPORTANT]
> Le credenziali del database (`appuser` / `secretpassword`) e la chiave segreta JWT presenti nei file `docker-compose.yml` e `application.yml` sono fornite **esclusivamente per consentire l'avvio immediato e autonomo dell'ambiente di demo locale**.  
> In un ambiente di produzione reale, tali segreti non devono mai essere tracciati nel repository, ma iniettati tramite variabili d'ambiente protette, file `.env` (inclusi nel `.gitignore`) o gestori di segreti dedicati (come HashiCorp Vault, AWS Secrets Manager o Azure Key Vault).

---

## 📁 Struttura del Repository

```text
.
├── docker-compose.yml       # Orchestrazione multi-container (Network, Volumi, Healthchecks)
├── README.md                # Documentazione di progetto
├── .gitignore               # Esclusioni per Java, Angular, IDE e file di build
│
├── backend/                 # SERVIZIO BACKEND (Java 21 / Spring Boot 3.3)
│   ├── Dockerfile           # Multi-stage build (Maven -> Eclipse Temurin JRE Alpine)
│   ├── pom.xml              # Dipendenze Maven (Spring Web, Security, JPA, Redis, JJWT, Argon2)
│   └── src/
│       ├── main/
│       │   ├── java/com/example/demo/
│       │   │   ├── controller/  # Endpoint REST (Auth, Products, Cache)
│       │   │   ├── dto/         # Oggetti di trasferimento dati con validazione
│       │   │   ├── model/       # Entità JPA (User, Product, Role)
│       │   │   ├── repository/  # Spring Data JPA Repositories
│       │   │   ├── security/    # Filtro JWT, Argon2 PasswordService, SecurityConfig
│       │   │   └── service/     # Business logic e Cache-Aside Redis
│       │   └── resources/       # Configurazione applicativa (application.yml)
│       └── test/                # Test unitari con JUnit 5 e Mockito
│
└── frontend/                # SERVIZIO FRONTEND (Angular 18 + Nginx)
    ├── Dockerfile           # Multi-stage build (Node 20 build -> Nginx Alpine runtime)
    ├── nginx.conf           # Configurazione routing SPA e Reverse Proxy API
    ├── package.json         # Dipendenze Angular e tooling
    └── src/
        ├── app/
        │   ├── components/  # Componenti Standalone (Login, Products)
        │   ├── guards/      # AuthGuard per protezione rotte
        │   ├── interceptors/# HttpInterceptorFn per iniezione Bearer Token
        │   ├── models/      # Interfacce TypeScript
        │   └── services/    # Servizi reattivi (AuthService, ProductService)
        └── index.html       # Entry point HTML con Bootstrap 5
```

---

## 🧪 Testing Automatizzato

Il backend include una suite di test unitari implementata con **JUnit 5** e **Mockito**:

* **`PasswordServiceTest`**: Verifica che l'hashing generi sequenze `$argon2id$` valide, validi le credenziali corrette, rifiuti quelle errate e verifichi l'univocità del salt crittografico.
* **`JwtServiceTest`**: Verifica la corretta generazione dei token, l'estrazione dei claims (subject, role, nome) e il rifiuto di token scaduti o manomessi.
* **`ProductServiceTest`**: Testa l'isolamento del pattern Cache-Aside:
  * In caso di *Cache Hit*, verifica che il database relazionale non venga interpellato.
  * In caso di *Cache Miss*, verifica l'accesso al DB e il successivo salvataggio in cache con TTL.
  * All'inserimento di un prodotto, verifica la corretta invocazione dell'eviction della chiave di cache.

---

## 🚦 Guida Rapida all'Avvio

### Prerequisiti
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installato e avviato.

### 1. Clonazione ed Avvio
```bash
# Clona il repository
git clone https://github.com/NicolaForasacco/SpringFullStackTest.git
cd SpringFullStackTest

# Avvia tutti i container
docker compose up --build
```
*(Aggiungi il flag `-d` se desideri eseguire i container in background).*

### 2. Utilizzo dell'Applicazione
1. Apri il browser all'indirizzo: **`http://localhost`**
2. **Accesso Demo**:
   * Clicca su *"Inserisci: admin@example.com / Admin123!"* oppure crea un nuovo account tramite la schermata di registrazione.
3. **Verifica delle Prestazioni**:
   * Nella dashboard prodotti, osserva il badge di latenza:
     * Prima lettura: `POSTGRESQL QUERY (~45 ms)`
     * Ricarica lista: `REDIS CACHE HIT (~2 ms)`
   * Aggiungi un prodotto per testare l'invalidazione della cache.
   * Utilizza il pulsante *"Svuota Cache Redis"* per forzare una nuova lettura da database.

### 3. Arresto
```bash
docker compose down
```
*(I dati persistono nei volumi Docker `postgres_data` e `redis_data`).*

---

## 📄 Licenza
Rilasciato sotto licenza [MIT](LICENSE).
