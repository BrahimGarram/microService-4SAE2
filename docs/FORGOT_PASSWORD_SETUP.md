# Guide de Configuration - Réinitialisation de Mot de Passe

## 📋 Vue d'ensemble

Ce guide explique comment configurer et tester la fonctionnalité de réinitialisation de mot de passe par email.

## 🔧 Configuration Keycloak (SMTP)

### Étape 1: Configurer le serveur SMTP dans Keycloak

Pour que les emails de réinitialisation soient envoyés, Keycloak doit être configuré avec un serveur SMTP.

1. **Accéder à l'Admin Console de Keycloak**
   - URL: `http://localhost:8080`
   - Se connecter avec: `admin` / `admin`

2. **Sélectionner le Realm**
   - Cliquer sur le realm `projetpidev` dans le menu déroulant en haut à gauche

3. **Configurer l'Email**
   - Aller dans **Realm Settings** (Paramètres du Realm)
   - Cliquer sur l'onglet **Email** dans le menu de gauche
   - Remplir les champs suivants:

   **Pour Gmail (exemple):**
   ```
   Host: smtp.gmail.com
   Port: 587
   From: votre-email@gmail.com
   From Display Name: Freelancify
   Reply to: (laisser vide ou mettre votre email)
   Enable SSL: OFF (désactivé)
   Enable StartTLS: ON (activé)
   Authentication: ON (ACTIVER le toggle - important!)
   Username: votre-email@gmail.com (apparaît après activation)
   Password: [Mot de passe d'application Gmail] (apparaît après activation)
   ```
   
   **⚠️ IMPORTANT:** 
   - Vous devez **ACTIVER le toggle "Authentication"** pour voir les champs Username et Password
   - Le mot de passe d'application Gmail doit être mis dans le champ "Password" (pas dans "Reply to")
   - Pour obtenir un mot de passe d'application Gmail:
     1. Aller sur https://myaccount.google.com/apppasswords
     2. Sélectionner "Mail" et votre appareil
     3. Copier le mot de passe généré (16 caractères)

   **Pour un serveur SMTP local (MailHog - pour développement):**
   ```
   Host: localhost
   Port: 1025
   From: noreply@freelancify.com
   From Display Name: Freelancify
   Enable StartTLS: OFF
   Enable Authentication: OFF
   ```

4. **Tester la configuration**
   - Cliquer sur **Save** (Enregistrer)
   - Cliquer sur **Test connection** pour vérifier que la connexion SMTP fonctionne
   - Cliquer sur **Send test email** pour envoyer un email de test

### Étape 2: Vérifier les paramètres d'email du Realm

1. Dans **Realm Settings** → **Email**
2. Vérifier que **Verify Email** est activé (si vous voulez que les emails soient vérifiés)
3. Vérifier que **Forgot Password** est activé

## 🚀 Étapes de Test

### Étape 1: Démarrer les services

1. **Démarrer Keycloak**
   ```bash
   # Si Keycloak est installé localement
   # Naviguer vers le dossier Keycloak et exécuter:
   bin/standalone.bat  # Windows
   # ou
   bin/standalone.sh  # Linux/Mac
   ```

2. **Démarrer MySQL (XAMPP)**
   - Démarrer XAMPP Control Panel
   - Démarrer MySQL

3. **Démarrer le Backend (Spring Boot)**
   ```bash
   cd backend
   mvn spring-boot:run
   # ou utiliser l'IDE pour lancer UserServiceApplication
   ```
   - Le backend devrait démarrer sur `http://localhost:8081`

4. **Démarrer le Frontend (Angular)**
   ```bash
   cd frontend
   npm start
   # ou
   ng serve
   ```
   - Le frontend devrait démarrer sur `http://localhost:4200`

### Étape 2: Tester la fonctionnalité

1. **Accéder à la page de login**
   - Ouvrir `http://localhost:4200/login`

2. **Cliquer sur "Forgot password?"**
   - Le lien se trouve sous le champ "Password"
   - Vous serez redirigé vers `/forgot-password`

3. **Saisir un email**
   - Entrer l'email d'un utilisateur existant dans Keycloak
   - Exemple: `user1@example.com`

4. **Soumettre le formulaire**
   - Cliquer sur "Send Reset Link"
   - Vous devriez voir un message de succès

5. **Vérifier l'email**
   - Ouvrir la boîte de réception de l'email saisi
   - Si SMTP est configuré, vous devriez recevoir un email de Keycloak
   - L'email contient un lien pour réinitialiser le mot de passe

6. **Réinitialiser le mot de passe**
   - Cliquer sur le lien dans l'email
   - Vous serez redirigé vers Keycloak
   - Saisir un nouveau mot de passe
   - Confirmer le nouveau mot de passe
   - Cliquer sur "Submit"

7. **Se connecter avec le nouveau mot de passe**
   - Retourner sur `http://localhost:4200/login`
   - Se connecter avec l'email et le nouveau mot de passe

## 🧪 Test sans SMTP (Développement)

Si vous n'avez pas configuré SMTP, vous pouvez quand même tester le flux:

1. **Vérifier les logs du backend**
   - Le backend devrait loguer: `Password reset email sent successfully to: [email]`
   - Même si l'email n'est pas envoyé, l'endpoint retourne un succès

2. **Vérifier les logs de Keycloak**
   - Les logs de Keycloak montreront si l'email a été envoyé ou s'il y a eu une erreur

3. **Utiliser MailHog pour le développement local**
   - Installer MailHog: https://github.com/mailhog/MailHog
   - Démarrer MailHog
   - Configurer Keycloak avec:
     - Host: `localhost`
     - Port: `1025`
   - Accéder à `http://localhost:8025` pour voir les emails capturés

## 🔍 Dépannage

### Problème: L'email n'est pas envoyé

**Solutions:**
1. Vérifier que SMTP est configuré dans Keycloak
2. Vérifier les logs de Keycloak pour les erreurs SMTP
3. Vérifier que le port SMTP n'est pas bloqué par le firewall
4. Pour Gmail, utiliser un "App Password" au lieu du mot de passe normal

### Problème: "User not found" dans les logs

**Solution:**
- Vérifier que l'utilisateur existe dans Keycloak avec cet email
- Vérifier que l'email est correctement saisi

### Problème: Le lien de réinitialisation ne fonctionne pas

**Solutions:**
1. Vérifier que le lien n'a pas expiré (les liens expirent généralement après 1 heure)
2. Vérifier que le realm dans l'URL correspond à `projetpidev`
3. Vérifier que Keycloak est accessible depuis le navigateur

### Problème: CORS errors

**Solution:**
- Vérifier que CORS est configuré dans `SecurityConfig.java`
- Vérifier que l'origine `http://localhost:4200` est autorisée

## 📝 Notes importantes

1. **Sécurité**: Le backend retourne toujours un message de succès, même si l'utilisateur n'existe pas. Cela empêche l'énumération d'emails.

2. **Expiration**: Les liens de réinitialisation expirent généralement après 1 heure (configurable dans Keycloak).

3. **Email requis**: L'utilisateur doit avoir un email valide et vérifié dans Keycloak pour recevoir l'email de réinitialisation.

4. **Configuration SMTP**: Pour la production, utilisez un service SMTP fiable (SendGrid, Mailgun, AWS SES, etc.).

## 🎯 Résumé des endpoints

- **Frontend**: `POST /api/users/forgot-password`
- **Backend**: `POST http://localhost:8081/api/users/forgot-password`
- **Body**: `{ "email": "user@example.com" }`
- **Response**: `{ "message": "If an account with that email exists, a password reset link has been sent." }`

## ✅ Checklist de configuration

- [ ] Keycloak démarré et accessible
- [ ] Realm `projetpidev` créé
- [ ] SMTP configuré dans Keycloak (ou MailHog pour développement)
- [ ] Test de connexion SMTP réussi
- [ ] Backend démarré sur port 8081
- [ ] Frontend démarré sur port 4200
- [ ] Utilisateur de test créé dans Keycloak avec email valide
- [ ] Test de réinitialisation de mot de passe effectué
