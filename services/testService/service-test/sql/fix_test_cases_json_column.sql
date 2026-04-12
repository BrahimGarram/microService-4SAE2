-- Erreur "Cas de test invalides ou tronqués" : la colonne test_cases_json est en TINYTEXT (255 octets)
-- ou les données ont été tronquées. Exécuter les 3 commandes ci-dessous dans l'ordre.

-- 1) Passer la colonne en LONGTEXT
ALTER TABLE question_test MODIFY COLUMN test_cases_json LONGTEXT;

-- 2) Réécrire le JSON pour la question Python (id 139)
-- Important : en MySQL utiliser \\n (double backslash) pour stocker \n dans le JSON, sinon \n devient un vrai saut de ligne et le JSON est invalide.
UPDATE question_test
SET test_cases_json = '[{"input":"2\\n3","expectedOutput":"5"},{"input":"0\\n0","expectedOutput":"0"},{"input":"10\\n20","expectedOutput":"30"}]'
WHERE id = 139;

-- 3) Réécrire le JSON pour la question JavaScript (id 140)
UPDATE question_test
SET test_cases_json = '[{"input":"2\\n3","expectedOutput":"5"},{"input":"0\\n0","expectedOutput":"0"},{"input":"10\\n20","expectedOutput":"30"}]'
WHERE id = 140;
