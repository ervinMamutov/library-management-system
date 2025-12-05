# Library Management System

A RESTful API for managing a library system with JWT authentication, role-based access control, and full audit trails.

## Features

- **User Management** - Register, login, update users with role-based permissions
- **Book Management** - CRUD operations for books with bulk import
- **Member Management** - Track library members and their information
- **Loan System** - Borrow and return books with due date tracking
- **Audit Trail** - Track who created and modified books with timestamps
- **Role-Based Access Control** - Three roles: ADMIN, LIBRARIAN, MEMBER
- **JWT Authentication** - Secure token-based authentication
- **Overdue Tracking** - Automatically track overdue loans

## Tech Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** - JWT authentication and authorization
- **Spring Data JPA** - ORM and database access
- **PostgreSQL** - Primary database
- **Maven** - Dependency management
- **BCrypt** - Password hashing

## Database Schema

### Tables
- `users` - System users (ADMIN, LIBRARIAN, MEMBER)
- `book` - Book catalog with audit fields
- `member` - Library members
- `loan` - Borrowing records

### Relationships
- Member (1) → (many) Loan
- Book (1) → (many) Loan

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 12+
- Maven 3.6+

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd library-management-system
```

2. **Setup PostgreSQL database**
```sql
CREATE DATABASE library;
```

3. **Configure application properties**
Create `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/library
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=your-256-bit-secret-key-here-make-it-long-and-random
jwt.expiration=3600000
```

4. **Build the project**
```bash
./mvnw clean install
```

5. **Run the application**
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Create First Admin User

Since registration defaults to MEMBER role, create the first ADMIN manually:

```sql
INSERT INTO users (username, hashed_password, role)
VALUES ('admin', '$2a$10$...bcrypt_hash...', 'ADMIN');
```

Or use a password encoder:
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("admin123");
```

## API Documentation

### Base URL
```
http://localhost:8080
```

### Authentication

#### Register (Public)
```http
POST /auth/register
Content-Type: application/json

{
  "username": "john",
  "password": "password123",
  "role": "MEMBER"
}
```
**Note:** Only ADMINs can create users with LIBRARIAN or ADMIN roles.

#### Login (Public)
```http
POST /auth/login
Content-Type: application/json

{
  "username": "john",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john",
  "role": "MEMBER"
}
```

### Book Endpoints

#### Get All Books (Authenticated)
```http
GET /api/books
Authorization: Bearer <token>
```

#### Get Book by ID (Authenticated)
```http
GET /api/books/{id}
Authorization: Bearer <token>
```

#### Create Book (ADMIN/LIBRARIAN)
```http
POST /api/books
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "1984",
  "author": "George Orwell",
  "isbn": "978-0451524935",
  "genre": "Dystopian",
  "publicationYear": 1949,
  "copiesAvailable": 5
}
```

**Response includes audit fields:**
```json
{
  "id": 1,
  "title": "1984",
  "author": "George Orwell",
  "isbn": "978-0451524935",
  "genre": "Dystopian",
  "publicationYear": 1949,
  "copiesAvailable": 5,
  "createdAt": "2024-12-04T10:30:00",
  "createdBy": "admin",
  "updatedAt": "2024-12-04T10:30:00",
  "updatedBy": "admin"
}
```

#### Update Book (ADMIN/LIBRARIAN)
```http
PUT /api/books/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "1984",
  "author": "George Orwell",
  "genre": "Dystopian Fiction",
  "publicationYear": 1949,
  "copiesAvailable": 10
}
```

#### Delete Book (ADMIN/LIBRARIAN)
```http
DELETE /api/books/{id}
Authorization: Bearer <token>
```

#### Bulk Import Books (ADMIN/LIBRARIAN)
```http
POST /api/books/import
Authorization: Bearer <token>
Content-Type: application/json

[
  {
    "title": "To Kill a Mockingbird",
    "author": "Harper Lee",
    "isbn": "978-0061120084",
    "genre": "Fiction",
    "publicationYear": 1960,
    "copies": 3
  }
]
```

### Member Endpoints

#### Get All Members (ADMIN/LIBRARIAN)
```http
GET /api/members
Authorization: Bearer <token>
```

#### Create Member (ADMIN/LIBRARIAN)
```http
POST /api/members
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890"
}
```

#### Get Member by ID (ADMIN/LIBRARIAN)
```http
GET /api/members/{id}
Authorization: Bearer <token>
```

#### Delete Member (ADMIN/LIBRARIAN)
```http
DELETE /api/members/{id}
Authorization: Bearer <token>
```

#### Get Member Loan History (ADMIN/LIBRARIAN/MEMBER)
```http
GET /api/members/{id}/loans
Authorization: Bearer <token>
```

### Loan Endpoints

#### Borrow Book (ADMIN/LIBRARIAN)
```http
POST /api/loans
Authorization: Bearer <token>
Content-Type: application/json

{
  "memberId": 1,
  "bookId": 2
}
```
**Note:** `dueDate` is optional and defaults to 14 days from borrow date.

With custom due date:
```json
{
  "memberId": 1,
  "bookId": 2,
  "dueDate": "2024-12-31T23:59:59"
}
```

#### Return Book (ADMIN/LIBRARIAN)
```http
PATCH /api/loans/{id}/return
Authorization: Bearer <token>
Content-Type: application/json

{
  "returnDate": "2024-12-10T14:30:00"
}
```
**Note:** `returnDate` is optional and defaults to current time.

#### Get Active Loans (ADMIN/LIBRARIAN)
```http
GET /api/loans/active
Authorization: Bearer <token>
```

#### Get Overdue Loans (ADMIN/LIBRARIAN)
```http
GET /api/loans/overdue
Authorization: Bearer <token>
```

### User Management Endpoints

#### Get All Users (ADMIN)
```http
GET /api/users
Authorization: Bearer <admin-token>
```

#### Get User by ID (ADMIN)
```http
GET /api/users/{id}
Authorization: Bearer <admin-token>
```

#### Update User (ADMIN)
```http
PUT /api/users/{id}
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "username": "newusername",
  "role": "LIBRARIAN"
}
```

#### Update Role Only (ADMIN)
```http
PATCH /api/users/{id}/role
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "role": "ADMIN"
}
```

#### Change Password
Users can change their own password, ADMINs can change anyone's:
```http
PATCH /api/users/{id}/password
Authorization: Bearer <token>
Content-Type: application/json

{
  "newPassword": "newpassword123"
}
```

#### Delete User (ADMIN)
```http
DELETE /api/users/{id}
Authorization: Bearer <admin-token>
```

## Role Permissions

### ADMIN
- Full system access
- Manage users (create, update, delete, change roles)
- All LIBRARIAN permissions

### LIBRARIAN
- Manage books (create, update, delete, import)
- Manage members (create, view, delete)
- Manage loans (borrow, return, view active/overdue)
- View all books

### MEMBER
- View all books
- View their own loan history (read-only)

## Business Rules

1. **Book Availability** - `copiesAvailable` is decremented when borrowed, incremented when returned
2. **No Duplicate Loans** - A member cannot borrow the same book twice simultaneously
3. **ISBN Uniqueness** - Each book must have a unique ISBN
4. **Overdue Detection** - Loans with `returnDate = null` and `dueDate < now` are overdue
5. **Default Loan Period** - 14 days if no due date specified
6. **Role Assignment** - Only ADMINs can create LIBRARIAN/ADMIN users

## Audit Trail

All books track:
- `createdBy` - Username who added the book
- `createdAt` - When the book was added
- `updatedBy` - Username who last modified the book
- `updatedAt` - When the book was last modified

Audit information is automatically captured from JWT authentication and visible in all API responses.

## Project Structure

```
src/main/java/com/example/library_management_system/
├── controller/          # REST endpoints
│   ├── AuthController.java
│   ├── BookController.java
│   ├── MemberController.java
│   ├── LoanController.java
│   └── UserController.java
├── service/             # Business logic
│   ├── AuthService.java
│   ├── BookService.java
│   ├── MemberService.java
│   ├── LoanService.java
│   └── UserService.java
├── repository/          # JPA repositories
│   ├── UserRepository.java
│   ├── BookRepository.java
│   ├── MemberRepository.java
│   └── LoanRepository.java
├── model/               # JPA entities
│   ├── User.java
│   ├── Book.java
│   ├── Member.java
│   ├── Loan.java
│   └── Role.java
├── dto/                 # Data Transfer Objects
│   ├── auth/
│   ├── book/
│   ├── member/
│   ├── loan/
│   └── user/
├── mapper/              # Entity <-> DTO mapping
│   ├── BookMapper.java
│   ├── MemberMapper.java
│   └── LoanMapper.java
├── security/            # Security configuration
│   ├── SecurityConfig.java
│   ├── JwtUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityAuditorAware.java
├── exception/           # Custom exceptions
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── UnauthorizedException.java
│   ├── BookUnavailableException.java
│   └── InvalidLoanOperationException.java
└── LibraryManagementSystemApplication.java
```

## Error Handling

The API returns consistent error responses:

**404 Not Found:**
```json
{
  "status": 404,
  "message": "Book not found with id: 123",
  "timestamp": 1701691234567
}
```

**400 Bad Request:**
```json
{
  "status": 400,
  "message": "Book is not available. No copies left.",
  "timestamp": 1701691234567
}
```

**401 Unauthorized:**
```json
{
  "status": 401,
  "message": "Invalid username or password",
  "timestamp": 1701691234567
}
```

**403 Forbidden:**
```json
{
  "status": 403,
  "message": "Access Denied",
  "timestamp": 1701691234567
}
```

## Testing with Postman

1. **Register a user** (defaults to MEMBER)
2. **Login as ADMIN** (created manually in DB)
3. **Promote user to LIBRARIAN** using PATCH /api/users/{id}/role
4. **Login as LIBRARIAN**
5. **Create books** using POST /api/books
6. **Create members** using POST /api/members
7. **Borrow books** using POST /api/loans
8. **View active loans** using GET /api/loans/active
9. **Return books** using PATCH /api/loans/{id}/return

## Development

### Code Conventions

- Constructor injection (no @Autowired on fields)
- DTOs for all request/response bodies
- Validation annotations on request DTOs
- Service layer handles business logic
- Controllers stay thin
- Custom exceptions with @ControllerAdvice handler
- Audit fields auto-populated via Spring Data JPA

### Running Tests

```bash
./mvnw test
```

## License

This project is licensed under the MIT License.

## Author

Developed as a final project for HackYourFuture.
