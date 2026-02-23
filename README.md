# 🚀 Freelance Management Platform (Microservices Architecture)

## 📌 Overview

This project is a **Freelance Management Platform** designed using a modern **Microservices Architecture**.

It allows clients and freelancers to interact through multiple independent services such as:

- Posts management  
- Tests management  
- Payments management  
- Complaints management  
- Projects management  

The system is built with:

- **Spring Boot** for backend microservices  
- **Angular** for the frontend  
- **API Gateway** as the single entry point  
- **Keycloak** for user authentication and authorization  
- A centralized database for freelance-related data  

---

## 🏗️ Architecture

The application follows this architecture:

Client/User → Angular Frontend → API Gateway → Spring Boot Microservices → Database

### Microservices Included:

| Service Name | Description |
|------------|-------------|
| Post Service | Manages posts published by clients/freelancers |
| Test Service | Handles technical tests and evaluations |
| Payment Service | Manages payments and transactions |
| Reclamation Service | Handles user complaints and support requests |
| Project Service | Manages freelance projects and assignments |
| User Service | User management secured with Keycloak |

---

## 🔐 User Management with Keycloak

Authentication and authorization are handled using **Keycloak**, providing:

- Secure login and registration  
- Role-based access control (Admin, Client, Freelancer)  
- Token-based authentication (JWT)  
- Centralized identity management  

Keycloak is integrated with the API Gateway to protect all backend services.

---

## ⚙️ Technologies Used

### Backend:
- Java 17+
- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka (Service Discovery)
- Spring Security
- Keycloak Integration

### Frontend:
- Angular
- TypeScript
- Bootstrap / Material UI

### Database:
- MySQL 


