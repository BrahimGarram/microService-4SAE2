# Guide de Test - Géolocalisation IP

## Problème : "Unknown" pour tous les utilisateurs

Si vous voyez "Unknown" pour tous les pays, c'est probablement parce que :

1. **Vous testez en localhost** : L'IP `127.0.0.1` est détectée comme privée et retourne "Unknown"
2. **Utilisateurs existants** : Les utilisateurs créés avant l'ajout du champ `country` n'ont pas d'IP stockée

## Solutions

### Solution 1 : Tester avec une vraie IP publique

Pour tester la géolocalisation, vous devez accéder à l'application depuis une IP publique :

1. **Déployer sur un serveur public** (même temporaire)
2. **Utiliser ngrok ou un tunnel similaire** pour exposer votre localhost avec une IP publique
3. **Tester depuis un autre réseau** (pas localhost)

### Solution 2 : Tester avec une IP publique manuelle

Vous pouvez utiliser l'endpoint admin pour mettre à jour les pays avec une IP publique de test :

```bash
# Obtenir votre IP publique
curl https://api.ipify.org

# Utiliser cette IP pour mettre à jour les pays
POST http://localhost:8081/api/users/update-countries
Headers: Authorization: Bearer <your-jwt-token>
```

**Note** : Cet endpoint utilisera l'IP de votre requête actuelle. Si vous êtes en localhost, cela ne fonctionnera pas.

### Solution 3 : Vérifier les logs

Vérifiez les logs du backend pour voir quelle IP est extraite :

```
Syncing user - Client IP: 127.0.0.1, Forwarded-For: null, Extracted IP: 127.0.0.1
IP address 127.0.0.1 is localhost or private, returning 'Unknown'
```

Si vous voyez `127.0.0.1`, c'est normal que ça retourne "Unknown".

## Comment ça fonctionne

1. **Nouveaux utilisateurs** : Lors de l'inscription ou de la synchronisation, l'IP est extraite de la requête HTTP
2. **Détection du pays** : L'IP est envoyée à l'API ip-api.com pour obtenir le pays
3. **IPs privées** : Les IPs locales (127.0.0.1, 192.168.x.x, etc.) retournent "Unknown"

## Pour tester en développement

### Option A : Utiliser ngrok

1. Installer ngrok : https://ngrok.com/
2. Exposer votre backend :
   ```bash
   ngrok http 8081
   ```
3. Utiliser l'URL ngrok (ex: `https://abc123.ngrok.io`) pour accéder à l'API
4. L'IP publique sera détectée correctement

### Option B : Tester depuis un serveur distant

1. Déployer temporairement sur un serveur (Heroku, Railway, etc.)
2. Tester l'inscription depuis ce serveur
3. Le pays sera détecté correctement

### Option C : Modifier temporairement le code (pour test uniquement)

Pour tester rapidement, vous pouvez temporairement commenter la vérification des IPs privées dans `IpGeolocationService.java` :

```java
// Temporairement désactiver pour test
// if (ipAddress.equals("127.0.0.1") || ...) {
//     return "Unknown";
// }
```

**⚠️ ATTENTION** : Ne faites cela que pour le test ! Remettez-le en production.

## Vérification

Pour vérifier que ça fonctionne :

1. **Créer un nouvel utilisateur** depuis une IP publique
2. **Vérifier dans MySQL** :
   ```sql
   SELECT id, username, email, country FROM users;
   ```
3. **Vérifier dans le dashboard admin** : Les statistiques par pays devraient s'afficher

## API utilisée

- **Service** : ip-api.com (gratuit, sans clé API)
- **Limite** : 45 requêtes/minute
- **URL** : `http://ip-api.com/json/{ip}`

## Notes importantes

- Les utilisateurs existants créés avant l'ajout du champ `country` resteront "Unknown" sauf si vous les mettez à jour manuellement
- En production, avec de vraies IPs publiques, la détection fonctionnera automatiquement
- Pour les utilisateurs en localhost (développement), c'est normal qu'ils aient "Unknown"
