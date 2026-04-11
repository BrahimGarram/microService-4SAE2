# Troubleshooting - Système de Tâches

## Problèmes courants et solutions

### 1. Les tâches ne s'affichent pas dans le modal de détails

**Vérifications :**
- ✅ Le backend `project-service` est démarré sur le port 8082
- ✅ La table `tasks` existe dans la base de données MySQL
- ✅ Aucune erreur dans la console du navigateur (F12)

**Solution :**
1. Exécuter le script SQL pour créer la table :
   ```sql
   -- Voir: project-service/database/create_tasks_table.sql
   ```

2. Redémarrer le `project-service`

3. Vérifier la console du navigateur pour les erreurs

### 2. Erreur CORS ou 404 lors du chargement des tâches

**Vérifications :**
- Le `project-service` écoute sur `http://localhost:8082`
- Le `TaskService` utilise la bonne URL : `http://localhost:8082/api/tasks`

**Solution :**
- Vérifier que le `project-service` est démarré
- Vérifier les logs du backend pour les erreurs

### 3. Le bouton "New Task" ne s'affiche pas

**Vérifications :**
- L'utilisateur est le propriétaire du projet OU est admin
- La méthode `canManageTasks()` retourne `true`

**Solution :**
- Seuls le propriétaire du projet et les admins peuvent créer des tâches

### 4. Erreur lors de la création d'une tâche

**Vérifications :**
- Le `projectId` est valide
- Le `createdBy` (currentUserId) est défini
- Le titre de la tâche fait au moins 3 caractères

**Solution :**
- Vérifier que l'utilisateur est bien connecté
- Vérifier que le projet existe

## Commandes utiles

### Démarrer le project-service
```bash
cd project-service
mvn spring-boot:run
# OU
.\run.bat
```

### Vérifier si le service est actif
```bash
curl http://localhost:8082/api/projects
```

### Vérifier les tâches d'un projet
```bash
curl http://localhost:8082/api/tasks/project/{projectId}
```

## Structure de la base de données

La table `tasks` doit avoir les colonnes suivantes :
- `id` (BIGINT, AUTO_INCREMENT, PRIMARY KEY)
- `title` (VARCHAR(200), NOT NULL)
- `description` (TEXT)
- `status` (VARCHAR(20), NOT NULL, DEFAULT 'TO_DO')
- `project_id` (BIGINT, NOT NULL)
- `assigned_to` (BIGINT)
- `created_by` (BIGINT, NOT NULL)
- `due_date` (DATETIME)
- `completed_at` (DATETIME)
- `priority` (INT, DEFAULT 0)
- `order_index` (INT, DEFAULT 0)
- `created_at` (DATETIME, NOT NULL)
- `updated_at` (DATETIME)
