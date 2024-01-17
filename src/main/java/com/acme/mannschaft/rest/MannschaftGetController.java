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
package com.acme.mannschaft.rest;

import com.acme.mannschaft.entity.Mannschaft;
import com.acme.mannschaft.service.MannschaftReadService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.acme.mannschaft.rest.MannschaftGetController.REST_PATH;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

/**
 * Eine Controller-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Methoden der Klasse abgebildet werden. Public, damit Pfade für Zugriffsschutz verwendet werden können.
 * <img src="../../../../../asciidoc/MannschaftGetController.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@RestController
@RequestMapping(REST_PATH)
@OpenAPIDefinition(info = @Info(title = "Mannschaft API", version = "v2"))
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"ClassFanOutComplexity", "java:S1075"})
public class MannschaftGetController {
    /**
     * Basispfad für die REST-Schnittstelle.
     */
    public static final String REST_PATH = "/rest";

    /**
     * Pfad, um Namen abzufragen.
     */
    public static final String NAME_PATH = "/name";

    /**
     * Muster für eine UUID.
     */
    public static final String ID_PATTERN = "[\\da-f]{8}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{12}";

    private final MannschaftReadService service;
    private final ObservationRegistry observationRegistry;
    private final UriHelper uriHelper;

    // https://localhost:8080/swagger-ui.html
    // https://localhost:8080/swagger-ui.html
    /**
     * Suche anhand der Mannschaft-ID als Pfad-Parameter.
     *
     * @param id ID des zu suchenden Mannschaften
     * @param version Versionsnummer aus dem Header If-None-Match
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @param authentication Authentication-Objekt für Security
     * @return Ein Response mit dem Statuscode 200 und dem gefundenen Mannschaften mit Atom-Links oder Statuscode 404.
     */
    @GetMapping(path = "{id:" + ID_PATTERN + "}", produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit der Mannschaft-ID", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "Mannschaft gefunden")
    @ApiResponse(responseCode = "404", description = "Mannschaft nicht gefunden")
    @SuppressWarnings("ReturnCount")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    ResponseEntity<MannschaftModel> getById(
        @PathVariable final UUID id,
        @RequestHeader("If-None-Match") final Optional<String> version,
        final HttpServletRequest request,
        final Authentication authentication
    ) {
        final var user = (UserDetails) authentication.getPrincipal();
        log.debug("getById: id={}, version={}, user={}", id, version, user);
        // KEIN Optional https://github.com/spring-projects/spring-security/issues/3208
        if (user == null) {
            log.error("Trotz Spring Security wurde get() ohne Benutzerkennung aufgerufen");
            return status(FORBIDDEN).build();
        }

        // "Distributed Tracing" durch https://micrometer.io bei Aufruf eines anderen Microservice
        final var mannschaft = Observation
            .createNotStarted("find-by-id", observationRegistry)
            .observe(() -> service.findById(id, user, false));
        log.debug("getById: {}", mannschaft);

        @SuppressWarnings("DataFlowIssue")
        final var currentVersion = STR."\"\{mannschaft.getVersion()}\"";
        if (Objects.equals(version.orElse(null), currentVersion)) {
            return status(NOT_MODIFIED).build();
        }

        final var model = mannschaftToModel(mannschaft, request);
        log.debug("getById: model={}", model);
        return ok().eTag(currentVersion).body(model);
    }

    private MannschaftModel mannschaftToModel(final Mannschaft mannschaft, final HttpServletRequest request) {
        final var model = new MannschaftModel(mannschaft);
        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var idUri = STR."\{baseUri}/\{mannschaft.getId()}";

        final var selfLink = Link.of(idUri);
        final var listLink = Link.of(baseUri, LinkRelation.of("list"));
        final var addLink = Link.of(baseUri, LinkRelation.of("add"));
        final var updateLink = Link.of(idUri, LinkRelation.of("update"));
        final var removeLink = Link.of(idUri, LinkRelation.of("remove"));
        model.add(selfLink, listLink, addLink, updateLink, removeLink);
        return model;
    }

    /**
     * Suche mit diversen Suchkriterien als Query-Parameter.
     *
     * @param suchkriterien Query-Parameter als Map.
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @return Ein Response mit dem Statuscode 200 und den gefundenen Mannschaften als CollectionModel oder Statuscode 404.
     */
    @GetMapping(produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit Suchkriterien", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "CollectionModel mid den Mannschaften")
    @ApiResponse(responseCode = "404", description = "Keine Mannschaften gefunden")
    CollectionModel<MannschaftModel> get(
        @RequestParam @NonNull final MultiValueMap<String, String> suchkriterien,
        final HttpServletRequest request
    ) {
        log.debug("get: suchkriterien={}", suchkriterien);

        final var baseUri = uriHelper.getBaseUri(request).toString();
        final var models = service.find(suchkriterien)
            .stream()
            .map(mannschaft -> {
                final var model = new MannschaftModel(mannschaft);
                model.add(Link.of(STR."\{baseUri}/\{mannschaft.getId()}"));
                return model;
            })
            .toList();
        log.debug("get: {}", models);
        return CollectionModel.of(models);
    }

    /**
     * Abfrage, welche Namen es zu einem Präfix gibt.
     *
     * @param prefix Name-Präfix als Pfadvariable.
     * @return Die passenden Namen oder Statuscode 404, falls es keine gibt.
     */
    @GetMapping(path = NAME_PATH + "/{prefix}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Suche Namen mit Praefix", tags = "Suchen")
    String getNamenByPrefix(@PathVariable final String prefix) {
        log.debug("getNamenByPrefix: {}", prefix);
        final var namen = service.findNamenByPrefix(prefix);
        log.debug("getNamenByPrefix: {}", namen);
        return namen.stream()
            .map(name -> STR."\"\{name}\"")
            .toList()
            .toString();
    }
}
