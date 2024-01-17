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
package com.acme.mannschaft.service;

import com.acme.mannschaft.entity.Mannschaft;
import com.acme.mannschaft.repository.MannschaftRepository;
import com.acme.mannschaft.security.CustomUserDetailsService;
import com.acme.mannschaft.security.PasswordInvalidException;
import com.acme.mannschaft.security.UsernameExistsException;
import jakarta.validation.Validator;
import java.util.UUID;

import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anwendungslogik für Mannschaften auch mit Bean Validation.
 * <img src="../../../../../asciidoc/MannschaftWriteService.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MannschaftWriteService {
    private final MannschaftRepository repo;
    // https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation-beanvalidation
    private final Validator validator;
    private final CustomUserDetailsService userService;

    /**
     * Einen neuen Mannschaften anlegen.
     *
     * @param mannschaft Das Objekt des neu anzulegenden Mannschaften.
     * @param user Die Benutzerdaten für den neuen Mannschaften.
     * @return Der neu angelegte Mannschaften mit generierter ID
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws EmailExistsException Es gibt bereits einen Mannschaften mit der Emailadresse.
     * @throws PasswordInvalidException falls das Passwort ungültig ist
     * @throws UsernameExistsException falls der Benutzername bereits existiert
     */
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions
    @Transactional
    @SuppressWarnings("TrailingComment")
    public Mannschaft create(final Mannschaft mannschaft, final UserDetails user) {
        log.debug("create: mannschaft={}", mannschaft);
        log.debug("create: adresse={}", mannschaft.getTrainer());
        log.debug("create: umsaetze={}", mannschaft.getSpielerList());
        log.debug("create: user={}", user);

        final var violations = validator.validate(mannschaft, Default.class, Mannschaft.NeuValidation.class);
        if (!violations.isEmpty()) {
            log.debug("create: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }

        final var login = userService.save(user);
        log.trace("create: login={}", login);
        final var mannschaftDB = repo.save(mannschaft);

        log.trace("create: Thread-ID={}", Thread.currentThread().threadId());

        log.debug("create: mannschaftDB={}", mannschaftDB);
        return mannschaftDB;
    }

    /**
     * Einen vorhandenen Mannschaften aktualisieren.
     *
     * @param mannschaft Das Objekt mit den neuen Daten (ohne ID)
     * @param id ID des zu aktualisierenden Mannschaften
     * @param version Die erforderliche Version
     * @return Aktualisierter Mannschaft mit erhöhter Versionsnummer
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws NotFoundException Kein Mannschaft zur ID vorhanden.
     * @throws VersionOutdatedException Die Versionsnummer ist veraltet und nicht aktuell.
     * @throws EmailExistsException Es gibt bereits einen Mannschaften mit der Emailadresse.
     */
    @Transactional
    public Mannschaft update(final Mannschaft mannschaft, final UUID id, final int version) {
        log.debug("update: mannschaft={}", mannschaft);
        log.debug("update: id={}, version={}", id, version);

        final var violations = validator.validate(mannschaft);
        if (!violations.isEmpty()) {
            log.debug("update: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }
        log.trace("update: Keine Constraints verletzt");

        final var mannschaftDbOptional = repo.findById(id);
        if (mannschaftDbOptional.isEmpty()) {
            throw new NotFoundException(id);
        }

        var mannschaftDb = mannschaftDbOptional.get();
        log.trace("update: version={}, mannschaftDb={}", version, mannschaftDb);
        if (version != mannschaftDb.getVersion()) {
            throw new VersionOutdatedException(version);
        }

        // Zu ueberschreibende Werte uebernehmen
        mannschaftDb.set(mannschaft);
        mannschaftDb = repo.save(mannschaftDb);

        log.debug("update: {}", mannschaftDb);
        return mannschaftDb;
    }
}
