# Guide de Configuration - Authentification Sociale (Google & GitHub)

## 📋 Vue d'ensemble

Ce guide explique comment configurer l'authentification Google et GitHub via Keycloak.

## 🔧 Configuration Keycloak

### Étape 1: Créer les applications OAuth

#### Google OAuth

1. **Aller sur Google Cloud Console**
   - URL: https://console.cloud.google.com/
   - Créer un nouveau projet ou sélectionner un projet existant

2. **Configurer l'écran de consentement OAuth**
   - Aller dans **APIs & Services** → **OAuth consent screen**
   - Choisir **External** (pour le développement)
   - Remplir les informations :
     - App name: `Freelancify`
     - User support email: votre email
     - Developer contact information: votre email
   - Cliquer sur **Save and Continue**
   - Ajouter les scopes (email, profile, openid)
   - Cliquer sur **Save and Continue**
   - Ajouter des test users si nécessaire
   - Cliquer sur **Save and Continue**

3. **Créer les credentials OAuth 2.0**
   - Aller dans **APIs & Services** → **Credentials**
   - Cliquer sur **Create Credentials** → **OAuth client ID**
   - Application type: **Web application**
   - Name: `Freelancify Keycloak`
   - **Authorized redirect URIs**: 
     ```
     http://localhost:8080/realms/projetpidev/broker/google/endpoint
     ```
   - Cliquer sur **Create**
   - **Copier le Client ID et Client Secret** (vous en aurez besoin)

#### GitHub OAuth

1. **Aller sur GitHub Developer Settings**
   - URL: https://github.com/settings/developers
   - Se connecter avec votre compte GitHub

2. **Créer une nouvelle OAuth App**
   - Cliquer sur **New OAuth App** (ou **OAuth Apps** → **New OAuth App**)
   - **IMPORTANT**: Assurez-vous de créer une **OAuth App**, pas une **GitHub App**
   - Remplir les informations :
     - **Application name**: `Freelancify Keycloak`
     - **Homepage URL**: `http://localhost:4200`
     - **Authorization callback URL**: 
       ```
       http://localhost:8080/realms/projetpidev/broker/github/endpoint
       ```
       ⚠️ **ATTENTION**: Cette URL doit correspondre exactement à l'alias configuré dans Keycloak. Si l'alias est différent de `github`, remplacez `github` dans l'URL par votre alias.
   - Cliquer sur **Register application**
   - **Copier le Client ID** (affiché immédiatement)
   - **Générer un Client Secret** : Cliquer sur **Generate a new client secret**
   - **Copier le Client Secret** (affiché une seule fois - sauvegardez-le immédiatement)

### Étape 2: Configurer les Identity Providers dans Keycloak

#### Configurer Google

1. **Accéder à Keycloak Admin Console**
   - URL: `http://localhost:8080/admin`
   - Se connecter avec: `admin` / `admin`

2. **Sélectionner le Realm**
   - Cliquer sur le realm `projetpidev` dans le menu déroulant en haut à gauche

3. **Ajouter Google Identity Provider**
   - Aller dans **Identity Providers** (dans le menu de gauche)
   - Cliquer sur **Add provider**
   - Sélectionner **Google**
   - Remplir les champs :
     - **Alias**: `google` (ou laisser par défaut)
     - **Display name**: `Google`
     - **Client ID**: [Votre Google Client ID]
     - **Client Secret**: [Votre Google Client Secret]
   - Cliquer sur **Add**

4. **Configurer les paramètres**
   - Dans la section **Settings** :
     - **Trust Email**: ON (pour faire confiance aux emails de Google)
     - **Account Linking Only**: OFF
   - Dans la section **Advanced settings** :
     - **First login flow**: Sélectionner **"first broker login"** (par défaut)
     - **Sync mode**: Sélectionner **"Import"** (pour créer automatiquement l'utilisateur)
   - Cliquer sur **Save**

5. **Configurer le First Login Flow (IMPORTANT - pour éviter la page Keycloak "Update Account Information" et "Account already exists")**
   
   **⚠️ IMPORTANT: Ne supprimez PAS complètement "Review Profile", mais configurez-le correctement**
   
   **Étapes détaillées:**
   - Aller dans **Authentication** (menu de gauche)
   - Cliquer sur **Flows** (sous Authentication)
   - Trouver le flow **"First Broker Login"**
   - Cliquer sur **"Copy"** pour créer une copie (nom: `First Broker Login - Auto`)
   - Dans le nouveau flow, trouver **"Review Profile"** dans la liste
   - Cliquer sur l'icône **"Config"** (engrenage) à droite de "Review Profile"
   - Dans la configuration qui s'ouvre :
     - **"Update profile on first login"**: OFF (désactivé)
     - **"Update profile on first login (required)"**: OFF (désactivé)
   - Cliquer sur **Save**
   
   **Configurer l'auto-linking pour les comptes existants:**
   - Dans le même flow `First Broker Login - Auto`, trouver **"Handle Existing Account"** (c'est l'étape 4 dans le flow par défaut)
   - **IMPORTANT**: "Handle Existing Account" est un **sub-flow** (flow parent), donc il n'a pas d'icône Config directe
   - **SOLUTION - Configurer via le sub-flow:**
     1. Cliquer sur la **flèche déroulante** (▼) à gauche de "Handle Existing Account" pour l'expanser
     2. Vous verrez les sous-étapes: "Confirm link existing account", "Account verification options", etc.
     3. Cliquer sur le **nom "Handle Existing Account"** lui-même (le texte bleu/liens) - cela ouvrira la page de configuration du sub-flow
     4. OU aller dans **Authentication** → **Flows** → chercher un flow nommé **"Handle Existing Account"** dans la liste des flows
     5. Cliquer sur ce flow pour l'ouvrir
     6. Dans la configuration du flow, chercher l'option **"Account linking strategy"** ou **"Linking strategy"**
     7. Changer de **"Ask user"** à **"Link account automatically"**
     8. Cliquer sur **Save**
   
   **ALTERNATIVE - Si vous ne trouvez pas cette option:**
   - Dans le flow `First Broker Login - Auto`, trouver **"Confirm link existing account"** (étape 5, sous "Handle Existing Account")
   - Cliquer sur l'icône **Config** (⚙️) à côté de "Confirm link existing account" (cette icône devrait être visible)
   - Dans la configuration, chercher une option pour désactiver la confirmation ou activer le linking automatique
   - OU simplement désactiver "Confirm link existing account" en mettant son Requirement sur "Disabled"
   
   **IMPORTANT - Simplifier le flow pour éviter les étapes de confirmation:**
   - Dans le flow `First Broker Login - Auto`, trouver **"Confirm link existing account"** (étape 5)
   - Dans la colonne **"Requirement"**, changer de **"Required"** à **"Disabled"**
   - Cela évite d'afficher une page de confirmation et permet la liaison automatique
   - **Note**: Si vous désactivez cette étape, vous pouvez aussi simplifier les étapes de vérification (étapes 6-9) en les mettant sur "Disabled" si vous voulez une liaison complètement automatique
   - Retourner dans **Identity Providers** → **Google** → **Settings**
   - Dans **Advanced settings**, changer **First login flow** vers `First Broker Login - Auto`
   - Cliquer sur **Save**
   
   **Note**: Gardez "Review Profile" dans le flow mais avec la configuration désactivée. Ne le supprimez pas complètement car cela peut causer des erreurs "Page has expired".

5. **Créer les Mappers (IMPORTANT)**
   - Cliquer sur l'onglet **Mappers** (en haut de la page)
   - Si aucun mapper n'existe, cliquer sur **Add mapper**
   
   **Mapper 1: Username**
   - **Name**: `google-username`
   - **Mapper type**: `Username Template Importer`
   - **Template**: `${CLAIM.preferred_username}`
   - Cliquer sur **Save**
   
   **Mapper 2: Email**
   - Cliquer sur **Add mapper** à nouveau
   - **Name**: `google-email`
   - **Mapper type**: `Attribute Importer`
   - **Social Profile JSON Field Path**: `email`
   - **User Attribute Name**: `email`
   - Cliquer sur **Save**
   
   **Mapper 3: First Name**
   - Cliquer sur **Add mapper** à nouveau
   - **Name**: `google-firstname`
   - **Mapper type**: `Attribute Importer`
   - **Social Profile JSON Field Path**: `given_name`
   - **User Attribute Name**: `  e`
   - Cliquer sur **Save**
   
   **Mapper 4: Last Name**
   - Cliquer sur **Add mapper** à nouveau
   - **Name**: `google-lastname`
   - **Mapper type**: `Attribute Importer`
   - **Social Profile JSON Field Path**: `family_name`
   - **User Attribute Name**: `lastName`
   - Cliquer sur **Save**

#### Configurer GitHub

1. **Ajouter GitHub Identity Provider**
   - Dans **Identity Providers**, cliquer sur **Add provider**
   - Sélectionner **GitHub**
   - Remplir les champs :
     - **Alias**: `github` (⚠️ IMPORTANT: doit correspondre exactement à l'URL de callback dans GitHub - si vous changez l'alias, mettez à jour l'URL dans GitHub)
     - **Display name**: `GitHub`
     - **Client ID**: [Votre GitHub Client ID] - Copier depuis GitHub sans espaces ni retours à la ligne
     - **Client Secret**: [Votre GitHub Client Secret] - Copier depuis GitHub sans espaces ni retours à la ligne
   - ⚠️ **VÉRIFICATION**: Vérifiez que l'**Alias** est exactement `github` (ou notez-le si différent)
   - Cliquer sur **Add**

2. **Configurer les paramètres**
   - Dans la section **Settings** :
     - **Trust Email**: ON
     - **Account Linking Only**: OFF
   - Dans la section **Advanced settings** :
     - **First login flow**: Utiliser le même flow personnalisé créé pour Google (`First Broker Login - Auto`)
     - **Sync mode**: Sélectionner **"Import"** (pour créer automatiquement l'utilisateur)
   - Cliquer sur **Save**

3. **Créer les Mappers (IMPORTANT)**
   - Cliquer sur l'onglet **Mappers** (en haut de la page)
   - Si aucun mapper n'existe, cliquer sur **Add mapper**
   
   **Mapper 1: Username**
   - **Name**: `github-username`
   - **Mapper type**: `Username Template Importer`
   - **Template**: `${CLAIM.preferred_username}`
   - Cliquer sur **Save**
   
   **Mapper 2: Email**
   - Cliquer sur **Add mapper** à nouveau
   - **Name**: `  `
   - **Mapper type**: `Attribute Importer`
   - **Social Profile JSON Field Path**: `email`
   - **User Attribute Name**: `email`
   - Cliquer sur **Save**
   
   **Mapper 3: First Name (optionnel)**
   - Cliquer sur **Add mapper** à nouveau
   - **Name**: `github-name`
   - **Mapper type**: `Attribute Importer`
   - **Social Profile JSON Field Path**: `name`
   - **User Attribute Name**: `fullName`
   - Cliquer sur **Save**

### Étape 3: Configurer le Client pour accepter les Identity Providers

1. **Aller dans Clients**
   - Sélectionner le client `freelance-client`

2. **Vérifier les Redirect URIs**
   - Aller dans l'onglet **Settings**
   - Vérifier que les **Valid redirect URIs** incluent :
     ```
     http://localhost:4200/*
     http://localhost:4200
     ```

3. **Vérifier les Web Origins**
   - Vérifier que **Web origins** inclut :
     ```
     http://localhost:4200
     ```

## 🎨 Frontend - Ajouter les boutons de connexion sociale

Les boutons Google et GitHub seront ajoutés dans le composant de login pour rediriger vers Keycloak avec le provider approprié.

### URLs de redirection Keycloak

- **Google**: `http://localhost:8080/realms/projetpidev/protocol/openid-connect/auth?client_id=freelance-client&redirect_uri=http://localhost:4200&response_type=code&scope=openid&kc_idp_hint=google`

- **GitHub**: `http://localhost:8080/realms/projetpidev/protocol/openid-connect/auth?client_id=freelance-client&redirect_uri=http://localhost:4200&response_type=code&scope=openid&kc_idp_hint=github`

## ✅ Test

1. **Démarrer tous les services**
   - Keycloak
   - Backend
   - Frontend

2. **Tester Google**
   - Aller sur `http://localhost:4200/login`
   - Cliquer sur "Sign in with Google"
   - Se connecter avec un compte Google
   - Vérifier la redirection vers l'application

3. **Tester GitHub**
   - Aller sur `http://localhost:4200/login`
   - Cliquer sur "Sign in with GitHub"
   - Autoriser l'application GitHub
   - Vérifier la redirection vers l'application

## 🔍 Dépannage

### Problème: "Invalid redirect URI"

**Solution**: Vérifier que les redirect URIs dans Google/GitHub correspondent exactement à ceux configurés dans Keycloak.

### Problème: "Client ID not found"

**Solution**: Vérifier que le Client ID et Client Secret sont correctement copiés dans Keycloak.

### Problème: L'utilisateur n'est pas créé dans la base de données

**Solution**: Après la première connexion sociale, appeler `/api/users/sync` pour créer l'utilisateur dans MySQL.

### Problème: La page "Update Account Information" apparaît toujours

**Solution**: 
- Vérifier que le **First login flow** dans l'Identity Provider est configuré avec le flow personnalisé
- Vérifier que **"Update profile on first login"** est désactivé dans la configuration de "Review Profile"
- Vérifier que **Sync mode** est sur **"Import"** dans les Advanced settings de l'Identity Provider

### Problème: "Page has expired" après connexion sociale

**Solution**: 
- **NE PAS supprimer complètement "Review Profile"** du flow - cela cause cette erreur
- Garder "Review Profile" dans le flow mais désactiver **"Update profile on first login"** dans sa configuration
- Vérifier que tous les steps nécessaires sont présents dans le flow :
  - "Review Profile" (configuré avec "Update profile on first login" = OFF)
  - "User creation or linking"
  - "Create User If Unique"
  - "Handle Existing Account" (avec "Link account automatically")
- Vérifier que les **Redirect URIs** dans le client Keycloak incluent exactement `http://localhost:4200/*`
- Vérifier que les **Redirect URIs** dans Google/GitHub correspondent exactement à ceux de Keycloak
- Redémarrer Keycloak après avoir modifié les flows

### Problème: "Account already exists" - Dialog apparaît au lieu de rediriger vers la page d'accueil

**Solution**: 
- Configurer **"Handle Existing Account"** dans le flow "First Broker Login - Auto"
- Dans la configuration de "Handle Existing Account", définir **"Account linking strategy"** sur **"Link account automatically"**
- Cela permettra de lier automatiquement le compte social au compte existant sans afficher le dialog
- L'utilisateur sera automatiquement redirigé vers la page d'accueil après la liaison

### Problème: GitHub 404 "Page not found" sur `github.com/login/oauth/authorize`

**Solution**: Cette erreur indique que l'application OAuth GitHub n'est pas correctement configurée.

**Étapes de vérification :**

1. **Vérifier que l'application OAuth GitHub existe :**
   - Aller sur https://github.com/settings/developers
   - Vérifier que votre application OAuth est listée
   - Si elle n'existe pas, créer une nouvelle **OAuth App** (pas GitHub App)

2. **Vérifier le Client ID et Secret dans Keycloak :**
   - Dans Keycloak Admin Console → **Identity Providers** → **GitHub**
   - Onglet **Settings**
   - Vérifier que le **Client ID** correspond exactement à celui de GitHub (sans espaces, sans retours à la ligne)
   - Vérifier que le **Client Secret** correspond exactement (sans espaces, sans retours à la ligne)
   - **Action** : Recopier depuis GitHub et coller dans Keycloak (utiliser Ctrl+A, Ctrl+C, Ctrl+V)

3. **Vérifier l'Authorization callback URL dans GitHub :**
   - Dans GitHub → Votre application OAuth → **Authorization callback URL**
   - Doit être exactement : `http://localhost:8080/realms/projetpidev/broker/github/endpoint`
   - **IMPORTANT** : L'alias dans Keycloak doit correspondre (si l'alias est `github`, l'URL doit contenir `/broker/github/`)
   - Pour vérifier l'alias : Keycloak → **Identity Providers** → **GitHub** → **Settings** → **Alias**

4. **Vérifier que l'application GitHub est de type "OAuth App" :**
   - Dans GitHub → Settings → Developer settings → OAuth Apps
   - L'application doit être une **OAuth App**, pas une **GitHub App**
   - Si c'est une GitHub App, créer une nouvelle OAuth App

5. **Redémarrer Keycloak** après toute modification

6. **Si le problème persiste - Recréer l'Identity Provider :**
   - Dans Keycloak → **Identity Providers** → **GitHub** → **Delete**
   - Dans GitHub → Supprimer l'ancienne application OAuth
   - Créer une nouvelle application OAuth dans GitHub avec :
     - **Application name** : `Freelancify Keycloak`
     - **Homepage URL** : `http://localhost:4200`
     - **Authorization callback URL** : `http://localhost:8080/realms/projetpidev/broker/github/endpoint`
   - Copier le **Client ID** et générer un nouveau **Client Secret**
   - Recréer l'Identity Provider dans Keycloak avec les nouveaux credentials

## 📝 Notes importantes

1. **Première connexion**: Après la première connexion avec Google/GitHub, l'utilisateur doit être synchronisé avec la base de données via `/api/users/sync`.

2. **Email**: Assurez-vous que "Trust Email" est activé dans les Identity Providers pour éviter la vérification d'email.

3. **Production**: Pour la production, utilisez les URLs de production au lieu de `localhost`.
