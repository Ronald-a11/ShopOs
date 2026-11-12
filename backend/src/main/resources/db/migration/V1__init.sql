-- V1: baseline. The PRD core DDL lands here in the first schema PR,
-- reviewed by the technical lead. Migrations are forward-only: never edit
-- a migration that has been merged to main; add a new V<n>__ file instead.

CREATE EXTENSION IF NOT EXISTS pgcrypto;
