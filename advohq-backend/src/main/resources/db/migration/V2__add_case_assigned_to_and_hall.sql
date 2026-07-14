-- The Library UI tracks who a case is assigned to and its court hall number;
-- V1 didn't have columns for either.
ALTER TABLE cases ADD COLUMN assigned_to VARCHAR(120);
ALTER TABLE cases ADD COLUMN hall VARCHAR(120);
