# MicroService-4SAE2

## 📌 Description

**MicroService-4SAE2** is an academic project based on a **microservices architecture** for a **freelance and project matching web platform**.

The objective of this project is to design and implement a modern, scalable, and modular system where each service handles a specific business responsibility.

The backend is developed using **Spring Boot** and **Symfony (PHP)**, while the frontend is built with **Angular**. The project uses multiple database technologies (**H2, MySQL, MongoDB**) following the principle of **polyglot persistence**.

---

## 🏗️ Architecture

The project follows a **microservices architecture**, where:

* Each microservice is independent
* Services communicate via REST APIs
* Each service can have its own database

### Main components:

* **Frontend**: Angular Web Application
* **Backend Services**:

  * Spring Boot Microservices
  * Symfony (PHP) Microservices
* **Databases**:

  * H2 (development & testing)
  * MySQL (relational data)
  * MongoDB (NoSQL data)

---

## 🛠️ Technologies Used

### Frontend

* Angular
* TypeScript
* HTML / CSS

### Backend

* Java – Spring Boot
* PHP – Symfony
* RESTful APIs

### Databases

* H2 Database
* MySQL
* MongoDB

### Tools & Concepts

* Microservices Architecture
* REST API
* MVC Pattern
* Git & GitHub

---

## 📂 Project Structure (Example)

```
MicroService-4SAE2/
│
├── frontend-angular/
│   └── angular-app/
│
├── backend-springboot/
│   ├── user-service/
│   ├── project-service/
│   └── authentication-service/
│
├── backend-symfony/
│   ├── payment-service/
│   └── management-service/
│
├── databases/
│   ├── mysql/
│   └── mongodb/
│
└── README.md
```

---

## 🎯 Features

* User management (freelancers & clients)
* Project and job posting
* Freelance–project matching
* Service-based backend (microservices)
* Multiple database support

---

## 🚀 How to Run the Project (General Steps)

### Backend – Spring Boot

1. Open the project in an IDE (IntelliJ / Eclipse)
2. Configure `application.yml` or `application.properties`
3. Run the Spring Boot application

### Backend – Symfony

1. Install dependencies:

   ```bash
   composer install
   ```
2. Configure `.env`
3. Start the Symfony server

### Frontend – Angular

1. Install dependencies:

   ```bash
   npm install
   ```
2. Run the application:

   ```bash
   ng serve
   ```

---

## 🎓 Academic Context

This project was developed as part of the **4SAE2 academic program**, with the goal of applying:

* Microservices concepts
* Full-stack development
* Multi-database integration
* Modern web technologies

---

## 👨‍💻 Author

* **Student**: 4SAE2
* **Project Type**: Academic / Educational

---

## 📄 License

This project is for **educational purposes only**.
