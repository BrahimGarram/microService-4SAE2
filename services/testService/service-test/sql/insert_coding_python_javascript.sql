-- Questions CODING Python et JavaScript pour le test et le sandbox (/test).
-- Exécuter dans MySQL après avoir vérifié que les IDs 131 et 132 sont libres
-- (sinon modifier les ID ou adapter sandboxQuestionIds dans test-intro.component.ts).

-- 1) Question CODING PYTHON : somme de deux entiers (stdin)
INSERT INTO question_test (
  id, question_text, domain, question_type, difficulty_level, duration,
  correct_answer, is_active, language, starter_code, test_cases_json,
  created_at, attempts_count, success_count, avg_time_seconds
) VALUES (
  131,
  'Écrire un programme Python qui lit deux entiers sur l''entrée standard et affiche leur somme.',
  'JAVA',
  'CODING',
  'EASY',
  300,
  'évalué par le serveur',
  1,
  'python',
  '# Lire deux entiers et afficher leur somme\na = int(input())\nb = int(input())\nprint(a + b)',
  '[{"input":"2\n3","expectedOutput":"5"},{"input":"0\n0","expectedOutput":"0"},{"input":"10\n20","expectedOutput":"30"}]',
  NOW(),
  0,
  0,
  0.0
);

-- 2) Question CODING JAVASCRIPT (Node) : somme de deux entiers (stdin)
INSERT INTO question_test (
  id, question_text, domain, question_type, difficulty_level, duration,
  correct_answer, is_active, language, starter_code, test_cases_json,
  created_at, attempts_count, success_count, avg_time_seconds
) VALUES (
  132,
  'Écrire un programme JavaScript (Node) qui lit deux entiers sur l''entrée standard et affiche leur somme.',
  'JAVA',
  'CODING',
  'EASY',
  300,
  'évalué par le serveur',
  1,
  'javascript',
  'const input = require(''fs'').readFileSync(0, ''utf-8'').trim().split(/\\s+/);\nconst a = parseInt(input[0], 10);\nconst b = parseInt(input[1], 10);\nconsole.log(a + b);',
  '[{"input":"2\n3","expectedOutput":"5"},{"input":"0\n0","expectedOutput":"0"},{"input":"10\n20","expectedOutput":"30"}]',
  NOW(),
  0,
  0,
  0.0
);

-- Si tes colonnes sont en camelCase (questionText au lieu de question_text), utilise plutôt :
-- questionText, domain, questionType, difficultyLevel, ...
