<?php

namespace App\Controller;

use Doctrine\DBAL\Connection;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

/**
 * Diagnostic rapide : si la base n’est pas joignable, toutes les routes Doctrine renvoient 500.
 */
final class HealthController
{
    #[Route('/api/health', name: 'api_health', methods: ['GET'])]
    public function __invoke(Connection $connection): JsonResponse
    {
        try {
            $connection->executeQuery('SELECT 1');
            if (!$connection->createSchemaManager()->tablesExist(['collaboration'])) {
                return new JsonResponse(
                    [
                        'ok' => false,
                        'database' => 'connected',
                        'schema' => 'missing_table',
                        'message' => 'Table « collaboration » absente : exécutez les migrations (docker-entrypoint ou bin/console doctrine:migrations:migrate).',
                        'hint' => 'Volume Postgres obsolète : docker compose down && docker volume rm …postgres-collab-data… puis up --build.',
                    ],
                    Response::HTTP_SERVICE_UNAVAILABLE
                );
            }

            return new JsonResponse(['ok' => true, 'database' => 'connected', 'schema' => 'ok']);
        } catch (\Throwable $e) {
            return new JsonResponse(
                [
                    'ok' => false,
                    'database' => 'error',
                    'message' => $e->getMessage(),
                    'hint' => 'Vérifiez DATABASE_URL (local 5432, ou postgres-collab Docker sur 5434 via .env.local).',
                ],
                Response::HTTP_SERVICE_UNAVAILABLE
            );
        }
    }
}
