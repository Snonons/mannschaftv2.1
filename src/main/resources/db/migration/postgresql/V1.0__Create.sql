-- Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.

-- docker compose exec postgres bash
-- psql --dbname=mannschaft --username=mannschaft [--file=/sql/V1.0__Create.sql]

-- https://www.postgresql.org/docs/current/sql-createtable.html
-- https://www.postgresql.org/docs/current/datatype.html
-- BEACHTE: user ist ein Schluesselwort
CREATE TABLE IF NOT EXISTS login (
             -- https://www.postgresql.org/docs/current/datatype-uuid.html
             -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-PRIMARY-KEYS
             -- impliziter Index fuer Primary Key
    id       uuid PRIMARY KEY USING INDEX TABLESPACE mannschaftspace,
             -- generierte Sequenz "login_id_seq":
    -- id    integer GENERATED ALWAYS AS IDENTITY(START WITH 1000) PRIMARY KEY USING INDEX TABLESPACE mannschaftspace,
    username varchar(20) NOT NULL UNIQUE USING INDEX TABLESPACE mannschaftspace,
    password varchar(180) NOT NULL,
    rollen   varchar(32)
) TABLESPACE mannschaftspace;

CREATE TABLE IF NOT EXISTS trainer (
    id        uuid PRIMARY KEY USING INDEX TABLESPACE mannschaftspace,
    -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-CHECK-CONSTRAINTS
    vorname   varchar(40) NOT NULL,
    nachname  varchar(40) NOT NULL,
    idx       integer NOT NULL DEFAULT 0
    ) TABLESPACE mannschaftspace;
CREATE INDEX IF NOT EXISTS trainer_mannschaft_id_idx ON trainer(id) TABLESPACE mannschaftspace;

CREATE TABLE IF NOT EXISTS mannschaft (
    id            uuid PRIMARY KEY USING INDEX TABLESPACE mannschaftspace,
                  -- https://www.postgresql.org/docs/current/datatype-numeric.html#DATATYPE-INT
    name          varchar(40) NOT NULL,
                  -- impliziter Index als B-Baum durch UNIQUE
                  -- https://www.postgresql.org/docs/current/datatype-datetime.html
    gruendungsjahr date CHECK (gruendungsjahr < current_date),
    trainer_id    uuid NOT NULL UNIQUE USING INDEX TABLESPACE mannschaftspace REFERENCES trainer(id),
    username      varchar(20) NOT NULL UNIQUE USING INDEX TABLESPACE mannschaftspace REFERENCES login(username),
                  -- https://www.postgresql.org/docs/current/datatype-datetime.html
    erzeugt       timestamp NOT NULL,
    aktualisiert  timestamp NOT NULL
) TABLESPACE mannschaftspace;

-- default: btree
-- https://www.postgresql.org/docs/current/sql-createindex.html
CREATE INDEX IF NOT EXISTS mannschaft_name_idx ON mannschaft(name) TABLESPACE mannschaftspace;

CREATE TABLE IF NOT EXISTS spieler (
    id        uuid PRIMARY KEY USING INDEX TABLESPACE mannschaftspace,
    vorname   varchar(40) NOT NULL,
    nachname  varchar(40) NOT NULL,
    mannschaft_id  uuid REFERENCES mannschaft(id),
    idx       integer NOT NULL DEFAULT 0
    ) TABLESPACE mannschaftspace;
CREATE INDEX IF NOT EXISTS spieler_mannschaft_id_idx ON spieler(mannschaft_id) TABLESPACE mannschaftspace;
