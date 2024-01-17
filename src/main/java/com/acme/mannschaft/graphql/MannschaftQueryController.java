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
package com.acme.mannschaft.graphql;

import com.acme.mannschaft.entity.Mannschaft;
import com.acme.mannschaft.entity.Spieler;
import com.acme.mannschaft.service.MannschaftReadService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import static java.util.Collections.emptyMap;

/**
 * Eine Controller-Klasse für das Lesen mit der GraphQL-Schnittstelle und den Typen aus dem GraphQL-Schema.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Controller
@RequiredArgsConstructor
@Slf4j
class MannschaftQueryController {
    private final MannschaftReadService service;

    /**
     * Suche anhand der Mannschaft-ID.
     *
     * @param id ID des zu suchenden Mannschaften
     * @param authentication Authentication-Objekt für Security
     * @return Der gefundene Mannschaft
     */
    @QueryMapping("mannschaft")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANNSCHAFT')")
    Mannschaft findById(@Argument final UUID id, final Authentication authentication) {
        final var user = (UserDetails) authentication.getPrincipal();
        log.debug("findById: id={}, user={}", id, user);
        final var mannschaft = service.findById(id, user, true);
        log.debug("findById: mannschaft={}", mannschaft);
        return mannschaft;
    }

    /**
     * Suche mit diversen Suchkriterien.
     *
     * @param input Suchkriterien und ihre Werte, z.B. `nachname` und `Alpha`
     * @return Die gefundenen Mannschaften als Collection
     */
    @QueryMapping("mannschaften")
    @PreAuthorize("hasRole('ADMIN')")
    Collection<Mannschaft> find(@Argument final Optional<Suchkriterien> input) {
        log.debug("find: input={}", input);
        final var suchkriterien = input.map(Suchkriterien::toMap).orElse(emptyMap());
        // TODO Mitladen von umsaetze
        final var mannschaften = service.find(suchkriterien);
        log.debug("find: mannschaften={}", mannschaften);
        return mannschaften;
    }

    /**
     * Handler-Methode für das GraphQL-Field "umsaetze" für den GraphQL-Type "Mannschaft".
     *
     * @param mannschaft Injiziertes Objekt vom GraphQL-Type "Mannschaft", zu dem diese Handler-Methode aufgerufen wird.
     * @param first die ersten Umsatzdaten in der Liste der Umsätze zum gefundenen Mannschaften
     * @return Liste mit Umsatz-Objekten, die für das Field "umsaetze" beim gefundenen Mannschaft-Objekt verwendet werden.
     */
    @SchemaMapping
    Collection<Spieler> spielerList(final Mannschaft mannschaft, @Argument final Integer first) {
        log.debug("spielerList: mannschaft={}, spielerList={}, first={}", mannschaft, mannschaft.getSpielerList(), first);
        if (first == null) {
            return mannschaft.getSpielerList();
        }
        if (first <= 0) {
            return List.of();
        }

        final var anzahlUmsaetze = mannschaft.getSpielerList().size();
        final var end = first <= anzahlUmsaetze ? first : anzahlUmsaetze;
        return mannschaft.getSpielerList().subList(0, end);
    }
}
