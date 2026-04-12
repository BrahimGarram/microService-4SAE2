-- Migration: rename table "tasks" to "kanbanlist" (project-service)
-- Run this once if you already have a "tasks" table and want to use the new name.

RENAME TABLE tasks TO kanbanlist;
