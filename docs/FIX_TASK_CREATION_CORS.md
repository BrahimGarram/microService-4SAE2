# Fix Task Creation CORS Error

## Problème
L'erreur CORS se produit car le backend `project-service` bloque les requêtes depuis le frontend Angular.

## Solution

### 1. Vérifier que le backend est démarré

Le backend `project-service` doit être démarré sur le port **8082**.

**Pour démarrer le service :**
```bash
cd project-service
mvn spring-boot:run
```

OU utilisez le fichier `run.bat` :
```bash
cd project-service
.\run.bat
```

### 2. Vérifier que la table `tasks` existe

Exécutez le script SQL dans MySQL :
```sql
-- Voir: project-service/database/create_tasks_table.sql
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TO_DO',
    project_id BIGINT NOT NULL,
    assigned_to BIGINT,
    created_by BIGINT NOT NULL,
    due_date DATETIME,
    completed_at DATETIME,
    priority INT DEFAULT 0,
    order_index INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_id (project_id),
    INDEX idx_assigned_to (assigned_to),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. Configuration CORS

La configuration CORS a été mise à jour dans :
- `WebConfig.java` - Configuration globale CORS
- `TaskController.java` - Annotation `@CrossOrigin` mise à jour

### 4. Redémarrer le backend

Après avoir modifié la configuration CORS, **redémarrer le backend** est nécessaire :

1. Arrêter le service (Ctrl+C dans le terminal)
2. Redémarrer avec `mvn spring-boot:run` ou `.\run.bat`

### 5. Vérifier que le service répond

Testez avec curl ou dans le navigateur :
```
http://localhost:8082/api/projects
```

Si vous obtenez une réponse JSON, le service est actif.

### 6. Vérifier les logs

Regardez les logs du backend pour voir les erreurs potentielles :
- Erreurs de connexion à la base de données
- Erreurs de validation
- Erreurs CORS

## Vérifications supplémentaires

1. **Port 8082 disponible** : Vérifiez qu'aucun autre service n'utilise le port 8082
2. **MySQL actif** : Vérifiez que MySQL est démarré (XAMPP)
3. **Base de données** : Vérifiez que la base `freelance_db` existe
4. **Table tasks** : Vérifiez que la table `tasks` existe dans la base

## Après correction

Une fois le backend redémarré avec la nouvelle configuration CORS, essayez à nouveau de créer une tâche. Les logs dans la console du navigateur vous indiqueront si le problème persiste.
