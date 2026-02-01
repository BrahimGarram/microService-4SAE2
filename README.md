# MicroService-4SAE2

## 📌 Description

**MicroService-4SAE2** est un projet académique basé sur une **architecture microservices** moderne, conçue pour être **scalable**, **sécurisée** et **faiblement couplée**.

L’objectif principal est de mettre en place un système distribué où chaque microservice gère une responsabilité métier bien définie, tout en communiquant via des mécanismes standards et centralisés.

Le backend est développé avec **Spring Boot** et **Symfony (PHP)**, le frontend avec **Angular**, et la sécurité est assurée par **Keycloak**.

---

## 🏗️ Architecture globale

Le projet repose sur une **architecture microservices complète**, incluant les composants suivants :

### 🔹 Composants principaux

- **Frontend** : Application web Angular  
- **API Gateway** : Point d’entrée unique pour toutes les requêtes du frontend  
- **Eureka Server** : Service de découverte des microservices  
- **Config Server** : Centralisation de la configuration des services  
- **Keycloak** : Authentification et gestion des utilisateurs  
- **Microservices Backend** :
  - Microservices Spring Boot
  - Microservice avancé en Symfony (PHP)
- **Bases de données** : Une base par microservice  
- **Docker** : Conteneurisation des services  

---

## 🧩 Microservices

### Spring Boot Microservices

Chaque microservice Spring Boot est indépendant et dispose de sa propre base de données :

- **Test Management Service** – H2  
- **Project Management Service** – MySQL  
- **Reclamation Management Service** – MySQL  
- **Payment Management Service** – H2  
- **Post Management Service** – MySQL  

### Symfony Microservice

- **Collaboration Management Service**
  - Framework : Symfony (PHP)
  - Base de données : PostgreSQL
  - Utilisé pour les fonctionnalités avancées

---

## 🛠️ Technologies utilisées

### Frontend
- Angular  
- TypeScript  
- HTML / CSS  

### Backend
- Java – Spring Boot  
- PHP – Symfony  
- RESTful APIs  

### Sécurité
- Keycloak  
- OAuth2 / JWT  

### Infrastructure
- Spring Cloud Eureka  
- Spring Cloud Config  
- API Gateway  
- Docker  

### Bases de données
- H2  
- MySQL  
- PostgreSQL  

---


