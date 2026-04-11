-- Autoriser le statut FRAUD (et autres valeurs du enum Status).
-- Si la colonne est en ENUM, la passer en VARCHAR.

ALTER TABLE affectation MODIFY COLUMN status VARCHAR(50) NOT NULL;
