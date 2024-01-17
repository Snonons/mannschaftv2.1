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

--  docker compose exec postgres bash
--  psql --dbname=mannschaft --username=mannschaft [--file=/sql/V1.1__Insert.sql]

-- COPY mit CSV-Dateien erfordert den Pfad src/main/resources/...
-- Dieser Pfad existiert aber nicht im Docker-Image
-- https://www.postgresql.org/docs/current/sql-copy.html

INSERT INTO login (id, username, password, rollen)
VALUES
    ('30000000-0000-0000-0000-000000000000','admin','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','ADMIN,MANNSCHAFT,ACTUATOR'),
    ('30000000-0000-0000-0000-000000000001','alpha','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g', 'MANNSCHAFT'),
    ('30000000-0000-0000-0000-000000000020','alpha2','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','MANNSCHAFT'),
    ('30000000-0000-0000-0000-000000000030','alpha3','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','MANNSCHAFT'),
    ('30000000-0000-0000-0000-000000000040','delta','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','MANNSCHAFT'),
    ('30000000-0000-0000-0000-000000000050','epsilon','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','MANNSCHAFT'),
    ('30000000-0000-0000-0000-000000000060','phi','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','MANNSCHAFT');

INSERT INTO trainer (id, vorname, nachname)
VALUES
    ('20000000-0000-0000-0000-000000000000', 'Max', 'Mustermann'),
    ('20000000-0000-0000-0000-000000000001', 'Max', 'Mustermann'),
    ('20000000-0000-0000-0000-000000000002', 'Max', 'Mustermann'),
    ('20000000-0000-0000-0000-000000000003', 'Max', 'Mustermann'),
    ('20000000-0000-0000-0000-000000000004', 'Anna', 'Schmidt'),
    ('20000000-0000-0000-0000-000000000005', 'Tom', 'Müller'),
    ('20000000-0000-0000-0000-000000000006', 'Lena', 'Wagner');

INSERT INTO mannschaft (id, name, gruendungsjahr, trainer_id, username, erzeugt, aktualisiert)
VALUES
    -- admin
    ('00000000-0000-0000-0000-000000000000','Admin','1894-05-12','20000000-0000-0000-0000-000000000000','admin','2022-01-31 00:00:00','2022-01-31 00:00:00'),
    -- HTTP GET
    ('00000000-0000-0000-0000-000000000001','Alpha','1905-09-28','20000000-0000-0000-0000-000000000001','alpha','2022-01-01 00:00:00','2022-01-01 00:00:00'),
    ('00000000-0000-0000-0000-000000000002','Beta','1922-03-03','20000000-0000-0000-0000-000000000002','alpha2','2022-01-02 00:00:00','2022-01-02 00:00:00'),
    -- HTTP PUT
    ('00000000-0000-0000-0000-000000000003','Gamma','1940-11-17','20000000-0000-0000-0000-000000000003','alpha3','2022-01-03 00:00:00','2022-01-03 00:00:00'),
    -- HTTP PATCH
    ('00000000-0000-0000-0000-000000000004','Delta','1965-07-21','20000000-0000-0000-0000-000000000004','delta','2022-01-04 00:00:00','2022-01-04 00:00:00'),
    -- HTTP DELETE
    ('00000000-0000-0000-0000-000000000005','FC Ballspielverein','1998-12-09','20000000-0000-0000-0000-000000000005','epsilon','2022-01-05 00:00:00','2022-01-05 00:00:00'),
    -- zur freien Verfuegung
    ('00000000-0000-0000-0000-000000000006','Karlsruher SC','1894-06-06','20000000-0000-0000-0000-000000000006','phi','2022-01-06 00:00:00','2022-01-06 00:00:00');

INSERT INTO spieler (id, vorname, nachname, mannschaft_id, idx)
VALUES
    ('10000000-0000-0000-0000-000000000000', 'Max', 'Mustermann','00000000-0000-0000-0000-000000000000',0),
    ('10000000-0000-0000-0000-000000000001', 'Max', 'Mustermann','00000000-0000-0000-0000-000000000001',0),
    ('10000000-0000-0000-0000-000000000002', 'Max', 'Mustermann','00000000-0000-0000-0000-000000000001',0),
    ('10000000-0000-0000-0000-000000000003', 'Max', 'Mustermann','00000000-0000-0000-0000-000000000001',0),
    ('10000000-0000-0000-0000-000000000011', 'Anna', 'Schmidt','00000000-0000-0000-0000-000000000002',1),
    ('10000000-0000-0000-0000-000000000012', 'Tom', 'Müller','00000000-0000-0000-0000-000000000002',2),
    ('10000000-0000-0000-0000-000000000013', 'Lena', 'Wagner','00000000-0000-0000-0000-000000000002',0),
    ('10000000-0000-0000-0000-000000000021', 'Felix', 'Becker','00000000-0000-0000-0000-000000000003',2),
    ('10000000-0000-0000-0000-000000000022', 'Sophie', 'Hoffmann','00000000-0000-0000-0000-000000000003',0),
    ('10000000-0000-0000-0000-000000000023', 'David', 'Klein','00000000-0000-0000-0000-000000000003',4),
    ('10000000-0000-0000-0000-000000000031', 'David', 'Klein','00000000-0000-0000-0000-000000000004',2),
    ('10000000-0000-0000-0000-000000000032', 'David', 'Klein','00000000-0000-0000-0000-000000000004',3),
    ('10000000-0000-0000-0000-000000000033', 'David', 'Klein','00000000-0000-0000-0000-000000000004',0),
    ('10000000-0000-0000-0000-000000000041', 'David', 'Klein','00000000-0000-0000-0000-000000000005',1),
    ('10000000-0000-0000-0000-000000000042', 'David', 'Klein','00000000-0000-0000-0000-000000000005',0),
    ('10000000-0000-0000-0000-000000000051', 'David', 'Klein','00000000-0000-0000-0000-000000000006',2),
    ('10000000-0000-0000-0000-000000000052', 'David', 'Klein','00000000-0000-0000-0000-000000000006',0);

