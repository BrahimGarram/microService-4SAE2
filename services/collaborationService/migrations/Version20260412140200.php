<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Bases déjà migrées avec une ancienne Version20260412140000 sans user_name.
 */
final class Version20260412140200 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'collaboration.user_name si colonne absente';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE collaboration ADD COLUMN IF NOT EXISTS user_name VARCHAR(255) NOT NULL DEFAULT \'\'');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE collaboration DROP COLUMN IF EXISTS user_name');
    }
}
