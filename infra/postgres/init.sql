-- Mini Maya database schema bootstrap
-- Flyway handles migrations; this creates the DB-level setup only.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Ensure the minimaya schema exists
CREATE SCHEMA IF NOT EXISTS minimaya;
SET search_path TO minimaya, public;
