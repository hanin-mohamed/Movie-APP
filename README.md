#  Movie API (Spring Boot)

A clean REST API for a Movie Dashboard. Supports **JWT auth (access + refresh)**, **admin OMDb import**, **user movie browsing**, and **per-user ratings**. Built with **Spring Boot**, **Spring Security**, **JPA/Hibernate**, and **PostgreSQL**.

**Frontend:** 👉 [Movie Dashboard (Angular)](https://github.com/hanin-mohamed/Movie-APP)

---

##  Features

- **Authentication & Roles**
  - JWT access & refresh tokens, token revocation on logout.
  - Roles: `ADMIN` and `USER`.  
  - Access token includes `role` and `userId` claims.

- **Admin**
  - Search OMDb (by query, paginated).
  - Import selected movies by `imdbId` (single or batch).
  - Delete movie (single) or batch delete by `imdbId`.

- **Users**
  - Browse movies stored in DB (search + pagination).
  - View full movie details.
  - Rate a movie (1–5), update or clear rating.
  - See rating summary (average & votes).

- **Pagination**
  - Consistent, 1-based `page` param with `size`.

- **DX**
  - Standard response wrapper (`AppResponse<T>`).
  - OpenAPI/Swagger UI.

---

## Tech Stack

- **Backend:** Spring Boot (3.x), Spring Security (JWT), Spring Data JPA
- **DB:** PostgreSQL (easily switchable to MySQL if preferred)
- **Integrations:** OMDb API (movie data by IMDb ID)
- **Docs:** Swagger / OpenAPI

---

##  Quick Start

### 1) Prerequisites
- Java 17+
- Maven
- PostgreSQL 14+
- OMDb API Key

### 2) Database
Using Docker:
```bash
docker run --name moviesdb \
  -e POSTGRES_USER=movies -e POSTGRES_PASSWORD=movies -e POSTGRES_DB=moviesdb \
  -p 5433:5432 -d postgres:14
```

Local connection used by default:
```
jdbc:postgresql://localhost:5433/moviesdb
username: movies
password: movies
```

### 3) Run
```bash
export OMDB_API_KEY=YOUR_OMDB_KEY
mvn clean install
mvn spring-boot:run
```
Server: `http://localhost:8080`  
CORS is allowed for `http://localhost:4200` (Angular dev server).

---


##  Response Shape

All endpoints return a consistent wrapper:

```json
{
  "message": "Movies fetched",
  "data": { ... }
}
```

Paginated responses (`PageResponse<T>`) include:
```json
{
  "content": [ /* items */ ],
  "page": 1,
  "size": 15,
  "totalPages": 3,
  "totalElements": 45
}
```

---

##  API Endpoints

Base URLs from config (shown resolved below).

### Auth
| Method | Path            | Body                     | Access | Notes |
|-------:|-----------------|--------------------------|:------:|-------|
| POST   | `/auth/login`   | `{ "email","password" }` | Public | Returns `{accessToken, refreshToken}` |
| POST   | `/auth/refresh` | `{ "refreshToken" }`     | Public | Rotate tokens |
| POST   | `/auth/logout`  | —                        | Auth   | Revokes refresh tokens |

### Movies (User)
| Method | Path             | Query                          | Access | Description |
|-------:|------------------|--------------------------------|:------:|-------------|
| GET    | `/movies`        | `page=1&size=15&search?=text` | Auth   | List movies from DB (paginated, 1-based page) |
| GET    | `/movies/{id}`   | —                              | Auth   | Movie details by `imdbId` (from DB) |

### OMDb (Admin)
| Method | Path                     | Query / Body                                        | Access | Description |
|-------:|--------------------------|-----------------------------------------------------|:------:|-------------|
| GET    | `/movies/omdb/search`    | `query=batman&page=1..100`                          | ADMIN  | Search OMDb (10 results/page) |
| POST   | `/movies/omdb/import`    | `{ "imdbIds": ["tt0468569","tt1375666", ...] }`     | ADMIN  | Import one or many titles |
| DELETE | `/movies/{imdbId}`       | —                                                   | ADMIN  | Delete single movie by `imdbId` |
| DELETE | `/movies/batch`          | `ids=tt0111161,tt0133093,...`                       | ADMIN  | Batch delete by comma-separated `imdbId`s |

### Ratings (User)
Base: `/movies/rating`
| Method | Path                     | Body                | Access | Description |
|-------:|--------------------------|---------------------|:------:|-------------|
| PUT    | `/{imdbId}`              | `{ "score": 1..5 }` | Auth   | Create/update my rating |
| GET    | `/{imdbId}`              | —                   | Auth   | My rating + summary |
| GET    | `/{imdbId}/summary`      | —                   | Auth   | Summary (avg, count) |
| DELETE | `/{imdbId}`              | —                   | Auth   | Delete my rating |

---

##  Architecture & Structure

- **Controllers**: Auth, Movies (Admin & User), Ratings
- **Services**: `AuthService`, `MovieUserService`, `RatingService`
- **Security**: `JwtAuthFilter`, `SecurityConfig` (stateless, CORS)
- **Integration**: `OmdbClient` (search + detail with robust error handling)
- **Persistence**: Entities (`Movie`, `MovieUser`, `Rating`) + Repositories
- **DTO & Mappers**: Clean mapping for API responses
- **Responses**: Unified `AppResponse<T>` and `PageResponse<T>`

**Entities (high level):**
- `Movie(id, imdbId*, title, year, type, poster, plot, genre, runtime, director, actors, language, country, awards, rated, released)`
- `MovieUser(id, email*, password(bcrypt), username*, role[ADMIN|USER])`
- `Rating(id, score 1..5, user_id -> MovieUser, movie_id -> Movie, created_at, updated_at)`

---

##  Swagger / OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui.html`  
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---


![🎥 Your popcorn and enjoy watching movie_app😉 ](assets/my_movie.gif)

---

