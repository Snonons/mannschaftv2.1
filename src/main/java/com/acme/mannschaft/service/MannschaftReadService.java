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
import com.acme.mannschaft.repository.SpecificationBuilder;
import com.acme.mannschaft.security.Rolle;
import io.micrometer.observation.annotation.Observed;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.acme.mannschaft.security.Rolle.ADMIN;

/**
 * Anwendungslogik für Mannschaften.
 * <img src="../../../../../asciidoc/MannschaftReadService.svg" alt="Klassendiagramm">
 * Schreiboperationen werden mit Transaktionen durchgeführt und Lese-Operationen mit Readonly-Transaktionen:
 * <a href="https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions">siehe Dokumentation</a>.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MannschaftReadService {
    private final MannschaftRepository repo;
    private final SpecificationBuilder specificationBuilder;

    /**
     * Einen Mannschaften anhand seiner ID suchen.
     *
     * @param id Die Id des gesuchten Mannschaften
     * @param user UserDetails-Objekt
     * @param fetchSpielerList true, falls die Spieler mitgeladen werden sollen
     * @return Der gefundene Mannschaft
     * @throws NotFoundException Falls kein Mannschaft gefunden wurde
     * @throws AccessForbiddenException Falls die erforderlichen Rollen nicht gegeben sind
     */
    @Observed(name = "find-by-id")
    public @NonNull Mannschaft findById(final UUID id, final UserDetails user, final boolean fetchSpielerList) {
        log.debug("findById: id={}, user={}", id, user);

        final var mannschaftOptional = fetchSpielerList ? repo.findByIdFetchSpielerList(id) : repo.findById(id);
        final var mannschaft = mannschaftOptional.orElse(null);

        // beide find()-Methoden innerhalb von observe() liefern ein Optional
        if (mannschaft != null && mannschaft.getUsername().contentEquals(user.getUsername())) {
            // eigene Mannschaftendaten
            return mannschaft;
        }

        final var rollen = user
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .map(str -> str.substring(Rolle.ROLE_PREFIX.length()))
            .map(Rolle::valueOf)
            .toList();
        if (!rollen.contains(ADMIN)) {
            // nicht admin, aber keine eigenen (oder keine) Mannschaftendaten
            throw new AccessForbiddenException(rollen);
        }

        // admin: Mannschaftendaten evtl. nicht gefunden
        if (mannschaft == null) {
            throw new NotFoundException(id);
        }
        log.debug("findById: mannschaft={}, umsaetze={}", mannschaft, fetchSpielerList ? mannschaft.getSpielerList() : "N/A");
        return mannschaft;
    }

    /**
     * Mannschaften anhand von Suchkriterien als Collection suchen.
     *
     * @param suchkriterien Die Suchkriterien
     * @return Die gefundenen Mannschaften oder eine leere Liste
     * @throws NotFoundException Falls keine Mannschaften gefunden wurden
     */
    @SuppressWarnings("ReturnCount")
    public @NonNull Collection<Mannschaft> find(@NonNull final Map<String, List<String>> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return repo.findAll();
        }

        if (suchkriterien.size() == 1) {
            final var namen = suchkriterien.get("name");
            if (namen != null && namen.size() == 1) {
                final var mannschaften = repo.findByName(namen.getFirst());
                if (mannschaften.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                log.debug("find (name): {}", mannschaften);
                return mannschaften;
            }
        }

        final var specification = specificationBuilder
            .build(suchkriterien)
            .orElseThrow(() -> new NotFoundException(suchkriterien));
        final var mannschaften = repo.findAll(specification);
        if (mannschaften.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("find: {}", mannschaften);
        return mannschaften;
    }

    /**
     * Abfrage, welche Namen es zu einem Präfix gibt.
     *
     * @param prefix Name-Präfix.
     * @return Die passenden Namen.
     * @throws NotFoundException Falls keine Namen gefunden wurden.
     */
    public @NonNull Collection<String> findNamenByPrefix(final String prefix) {
        log.debug("findNamenByPrefix: {}", prefix);
        final var namen = repo.findNamenByPrefix(prefix);
        if (namen.isEmpty()) {
            //noinspection NewExceptionWithoutArguments
            throw new NotFoundException();
        }
        log.debug("findNamenByPrefix: {}", namen);
        return namen;
    }
}
