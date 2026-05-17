-- Runs once on first Postgres startup (empty data volume).
-- The "ecommerce" database is created via POSTGRES_DB; create the rest here
-- so every microservice owns an isolated database.
CREATE DATABASE userdb;
CREATE DATABASE orderdb;
CREATE DATABASE paymentdb;
