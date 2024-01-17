/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.mannschaft.dev;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import static com.acme.mannschaft.dev.DevConfig.DEV;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.ResponseEntity.ok;

/**
 * Eine Controller-Klasse, um beim Enwickeln, die (Test-) DB neu zu laden.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Controller
@RequestMapping("/dev")
@RequiredArgsConstructor
@Slf4j
@Profile(DEV)
public class DbPopulateController {
    private final Flyway flyway;

    /**
     * Die (Test-) DB wird bei einem POST-Request neu geladen.
     *
     * @return Response mit Statuscode 200 und Body "ok", falls keine Exception aufgetreten ist.
     */
    @PostMapping(value = "db_populate", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> dbPopulate() {
        log.warn("Die DB wird neu geladen");
        flyway.clean();
        flyway.migrate();
        log.warn("Die DB wurde neu geladen");
        return ok("ok");
    }
}
