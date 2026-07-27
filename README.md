# Gestion des ressources — Marsa Maroc (Opération Manutention)

Application de gestion des ressources humaines et matérielles pour l'opération Manutention,
développée dans le cadre du stage DDIT. Backend Java Spring Boot / PostgreSQL, frontend Angular,
données de démonstration issues d'un mock simulant le TOS (Terminal Operating System).

## Périmètre

Conforme au SFD "Gestion des ressources" et au rapport de cadrage du stage, limité à
l'opération **Manutention** :

- **Paramétrage** : Mode de travail, Période shift, Équipes, Plan de roulement, Main théorique,
  Norme de productivité.
- **Exploitation** : Escales (interface Trafic/TOS), Commandes, Affectation prévisionnelle,
  Affectation réelle, Suivi des absences et arrêts, Validation, Lier commande à une escale,
  Clôturer une escale.
- **Administration** : Profils/droits, Utilisateurs, Authentification (JWT).
- **Reporting** : Dashboard (écarts prévu/réalisé, absentéisme, arrêts cumulés).

## Démarrage rapide avec Docker (Recommandé)

Lancez toute l'application (Base de données PostgreSQL, Backend Spring Boot, et Frontend Angular) en une seule commande sans aucune installation préalable (à part Docker) :

```bash
docker compose up --build
```

L'application sera directement accessible :
- **Frontend Web** : `http://localhost` (ou `http://localhost:4200`)
- **Backend API** : `http://localhost:8080/api`
- **PostgreSQL** : `localhost:5432`

Compte d'accès administrateur par défaut :
- **Matricule** : `ADMIN001`
- **Mot de passe** : `MarsaMaroc2026!`

Pour arrêter l'application :
```bash
docker compose down
```

---

## Démarrage manuel (Développement)

### 1. Base de données uniquement

```bash
docker compose up -d postgres
```

Démarre PostgreSQL (`gestion_ressources`, user/password `marsa`/`marsa`) sur `localhost:5432`.

### 2. Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

Au premier démarrage, le `DataSeeder` charge automatiquement le mock TOS
(`backend/src/main/resources/mock/mock-tos-data-marsa-maroc.json` — escales, commandes,
personnel, équipes, équipements, arrêts, absences) ainsi que des données de paramétrage de
référence (fonctions, mains théoriques, normes, mode de travail, profils/droits). Un compte
administrateur est créé :

- **Matricule** : `ADMIN001`
- **Mot de passe** : `MarsaMaroc2026!`

L'API démarre sur `http://localhost:8080/api`.

### 3. Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

L'application démarre sur `http://localhost:4200`.

## Visualiser la base de données

Recommandé : extension **SQLTools** + **SQLTools PostgreSQL/Cockroach Driver** dans VS Code.

### Paramètres de connexion

| Champ | Valeur |
|---|---|
| Connection name | (libre, ex. `Marsa - Gestion des ressources`) |
| Connect using | Server and Port |
| Server Address | `localhost` |
| Port | `5432` |
| Database | `gestion_ressources` |
| Username | `marsa` |
| Password | `marsa` |

### Requêtes utiles pour explorer la base

```sql
-- Lister toutes les tables
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;

-- Données de référence
SELECT * FROM ref_personnel;
SELECT * FROM ref_equipement;

-- Paramétrage
SELECT * FROM param_equipe;
SELECT * FROM param_main_theorique;

-- Exploitation (le cœur de l'app)
SELECT * FROM expl_escale;
SELECT * FROM expl_commande;
SELECT * FROM expl_arret;
SELECT * FROM expl_absence;

-- Administration
SELECT * FROM admin_utilisateur;
SELECT * FROM admin_profil;
```

Alternative sans installer d'extension : `docker exec -it marsa-gestion-ressources-db psql -U marsa -d gestion_ressources`
puis `\dt` pour lister les tables ou coller directement les requêtes ci-dessus.

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 25, Spring Boot 3.5, Spring Data JPA, Spring Security + JWT |
| Base de données | PostgreSQL (Docker) |
| Frontend | Angular 22 (standalone components, signals), CSS maison (charte Marsa Maroc) |

**Note sur l'UI** : PrimeNG a été évalué mais écarté — au moment du développement, seule une
version release-candidate de PrimeNG est compatible avec Angular 22, avec des dépendances de
thème elles-mêmes en rc/alpha. Pour un livrable stable, l'interface a été réalisée avec des
composants standalone et une feuille de style maison (`frontend/src/styles.css`) reprenant la
charte Marsa Maroc (rouge/noir/blanc).

## Simplifications assumées (par rapport au SFD complet)

- Les popups « Ajouter équipe/équipement/accessoire externe » du SFD sont remplacées par une
  sélection libre inline (checkbox « appel externe » côté modèle) plutôt qu'une fenêtre modale
  dédiée.
- Le référentiel `Shift` (calendrier des shifts avec dates) du mock n'est pas répliqué tel quel
  en base ; les shifts sont représentés par l'énumération SHIFT_1/2/3 combinée à une date sur
  chaque commande/affectation.
- Les équipes du mock ne portent pas de terminal explicite : elles sont rattachées au premier
  terminal du référentiel lors du seed.

## Dépannage

- **Docker Desktop non démarré** : lancer Docker Desktop puis relancer `docker compose up -d`.
- **Réinitialiser les données** : `docker compose down -v && docker compose up -d` puis
  redémarrer le backend (le DataSeeder ne s'exécute que si la base est vide).
