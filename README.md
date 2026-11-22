# FullStack Enterprise Demo: Java (Spring Boot 3) + Angular 18 + PostgreSQL + Redis + Docker

Applicazione completa multi-container progettata secondo le best practice architetturali enterprise.

---

## 🧱 I 4 Mattoncini (Container)

1. **Frontend (`frontend`)**:
   - Single Page Application in **Angular 18** (Standalone Components, Signals, Reactive HTTP).
   - Servito tramite **Nginx** come web server e reverse proxy (invia le chiamate `/api/` al backend).
2. **Backend (`backend`)**:
   - **Java 21** e **Spring Boot 3.3**.
   - **Spring Security** con filtro Stateless JWT.
   - Hashing delle password con **Argon2id** (standard OWASP).
   - Gestione cache intelligente con **Redis** (Cache-Aside pattern).
3. **Database (`postgres`)**:
   - **PostgreSQL 16** con volume persistente (`postgres_data`).
4. **Cache (`redis`)**:
   - **Redis 7 (Alpine)** con volume persistente (`redis_data`).

---

## 🚀 Come avviare l'intero stack

Assicurati che **Docker Desktop** sia aperto e funzionante sul tuo PC.

Apri un terminale nella cartella del progetto:
```bash
git clone https://github.com/NicolaForasacco/SpringFullStackTest.git
cd SpringFullStackTest
```

Avvia tutti i container con un solo comando:
```bash
docker compose up --build
```
*(Se preferisci eseguirlo in background, aggiungi il flag `-d`: `docker compose up --build -d`)*

Docker scaricherà le immagini, compilerà sia il backend Java che il frontend Angular all'interno dei container (senza che tu debba installare Maven o Node.js sul tuo PC!) e avvierà tutto.

---

## 🌐 Come usare l'applicazione

Una volta avviato:
1. Apri il browser all'indirizzo: **[http://localhost](http://localhost)**
2. Ti troverai nella schermata di **Login**.
3. Puoi:
   - Cliccare sul pulsante rapido per inserire le credenziali di test:
     - **Email**: `admin@example.com`
     - **Password**: `Admin123!`
   - Oppure cliccare su *"Registrati"* e creare un nuovo account con la tua email (la password verrà cifrata sul server con **Argon2id**).
4. Entrato nella dashboard dei **Prodotti**, osserva il badge in alto a destra:
   - **Prima chiamata**: `POSTGRESQL QUERY (~45 ms)` (Cache Miss: legge dal disco del DB e popola Redis).
   - **Clicca "Ricarica Lista"**: `REDIS CACHE HIT (~2 ms)` (Cache Hit: legge istantaneamente dalla RAM di Redis!).
   - **Aggiungi un nuovo prodotto**: il backend salverà su Postgres e invaliderà automaticamente la cache Redis, dimostrando l'invalidation pattern.
   - **Clicca "Svuota Cache Redis"**: per testare manualmente il ritorno alla query PostgreSQL.

---

## 🛑 Come spegnere tutto

Nel terminale, premi `Ctrl + C` (oppure lancia `docker compose down` da un altro terminale).
I container si spegneranno all'istante liberando la memoria RAM, mentre i dati del database rimarranno al sicuro nei volumi Docker.
