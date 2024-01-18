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

import com.acme.mannschaft.rest.patch.MannschaftPatcher;
import com.acme.mannschaft.security.PasswordInvalidException;
import com.acme.mannschaft.security.UsernameExistsException;
import com.acme.mannschaft.service.ConstraintViolationsException;
import com.acme.mannschaft.service.MannschaftReadService;
import com.acme.mannschaft.service.MannschaftWriteService;
import com.acme.mannschaft.service.VersionOutdatedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import static com.acme.mannschaft.rest.MannschaftGetController.ID_PATTERN;
import static com.acme.mannschaft.rest.MannschaftGetController.REST_PATH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

/**
 * Eine Controller-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Methoden der Klasse abgebildet werden.
 * <img src="../../../../../asciidoc/MannschaftWriteController.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Controller
@RequestMapping(REST_PATH)
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("java:S1075")
public class MannschaftWriteController {
    /**
     * Basispfad für "type" innerhalb von ProblemDetail.
     */
    @SuppressWarnings("TrailingComment")
    public static final String PROBLEM_PATH = "/problem/";

    private static final String VERSIONSNUMMER_FEHLT = "Versionsnummer fehlt";

    private final MannschaftWriteService service;
    private final MannschaftMapper mapper;
    private final UriHelper uriHelper;

    /**
     * Einen neuen Mannschaft-Datensatz anlegen.
     *
     * @param mannschaftUserDTO Das Mannschaftenobjekt mit den Benutzerdaten aus dem eingegangenen Request-Body.
     * @param request Das Request-Objekt, um Location im Response-Header zu erstellen.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder Statuscode 422 falls Constraints verletzt
     *      sind oder Statuscode 400 falls syntaktische Fehler im Request-Body vorliegen.
     * @throws URISyntaxException falls die URI im Request-Objekt nicht korrekt wäre
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Einen neuen Mannschaften anlegen", tags = "Neuanlegen")
    @ApiResponse(responseCode = "201", description = "Mannschaft neu angelegt")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @SuppressWarnings("TrailingComment")
    ResponseEntity<Void> post(
        @RequestBody final MannschaftUserDTO mannschaftUserDTO,
        final HttpServletRequest request
    ) throws URISyntaxException {
        log.debug("post: mannschaftUserDTO={}", mannschaftUserDTO);

        final var mannschaftDTO = mannschaftUserDTO.mannschaftDTO();
        final var userDTO = mannschaftUserDTO.userDTO();
        if (mannschaftDTO == null || userDTO == null) {
            return badRequest().build();
        }

        final var mannschaftInput = mapper.toMannschaft(mannschaftDTO);
        mannschaftInput.setUsername(userDTO.username());

        final var mannschaft = service.create(mannschaftInput, userDTO.toUserDetails());
        final var baseUri = uriHelper.getBaseUri(request);
        final var location = new URI(STR."\{baseUri.toString()}/\{mannschaft.getId()}");
        return created(location).build();
    }

    /**
     * Einen vorhandenen Mannschaft-Datensatz überschreiben.
     *
     * @param id ID des zu aktualisierenden Mannschaften.
     * @param mannschaftUpdateDTO Das Mannschaftenobjekt aus dem eingegangenen Request-Body.
     * @param version Versionsnummer aus dem Header If-Match
     * @param request Das Request-Objekt, um ggf. die URL für ProblemDetail zu ermitteln
     * @return Response mit Statuscode 204 oder Statuscode 400, falls der JSON-Datensatz syntaktisch nicht korrekt ist
     *      oder 412 falls die Versionsnummer nicht ok ist oder 428 falls die Versionsnummer fehlt.
     */
    @PutMapping(path = "{id:" + ID_PATTERN + "}", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Einen Mannschaften mit neuen Werten aktualisieren", tags = "Aktualisieren")
    @ApiResponse(responseCode = "204", description = "Aktualisiert")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "404", description = "Mannschaft nicht vorhanden")
    @ApiResponse(responseCode = "412", description = "Versionsnummer falsch")
    @ApiResponse(responseCode = "428", description = VERSIONSNUMMER_FEHLT)
    ResponseEntity<Void> put(
        @PathVariable final UUID id,
        @RequestBody final MannschaftUpdateDTO mannschaftUpdateDTO,
        @RequestHeader("If-Match") final Optional<String> version,
        final HttpServletRequest request
    ) {
        log.debug("put: id={}, mannschaftUpdateDTO={}", id, mannschaftUpdateDTO);
        final int versionInt = getVersion(version, request);
        final var mannschaftInput = mapper.toMannschaft(mannschaftUpdateDTO);
        final var mannschaft = service.update(mannschaftInput, id, versionInt);
        log.debug("put: {}", mannschaft);
        return noContent().eTag(STR."\"\{mannschaft.getVersion()}\"").build();
    }

    @SuppressWarnings({"MagicNumber", "RedundantSuppression"})
    private int getVersion(final Optional<String> versionOpt, final HttpServletRequest request) {
        log.trace("getVersion: {}", versionOpt);
        if (versionOpt.isEmpty()) {
            throw new VersionInvalidException(
                PRECONDITION_REQUIRED,
                VERSIONSNUMMER_FEHLT,
                URI.create(request.getRequestURL().toString()));
        }

        final var versionStr = versionOpt.get();
        if (versionStr.length() < 3 ||
            versionStr.charAt(0) != '"' ||
            versionStr.charAt(versionStr.length() - 1) != '"') {
            throw new VersionInvalidException(
                PRECONDITION_FAILED,
                STR."Ungueltiges ETag \{versionStr}",
                URI.create(request.getRequestURL().toString())
            );
        }

        final int version;
        try {
            version = Integer.parseInt(versionStr.substring(1, versionStr.length() - 1));
        } catch (final NumberFormatException ex) {
            throw new VersionInvalidException(
                PRECONDITION_FAILED,
                STR."Ungueltiges ETag \{versionStr}",
                URI.create(request.getRequestURL().toString()),
                ex
            );
        }

        log.trace("getVersion: version={}", version);
        return version;
    }

    @ExceptionHandler
    ProblemDetail onConstraintViolations(
        final ConstraintViolationsException ex,
        final HttpServletRequest request
    ) {
        log.debug("onConstraintViolations: {}", ex.getMessage());

        final var mannschaftViolations = ex.getViolations()
            .stream()
            .map(violation -> {
                final var path = violation.getPropertyPath();
                final var msg = violation.getMessage();
                final var annot = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
                return STR."\{path}: \{msg} (\{annot})";
            })
            .toList();
        log.trace("onConstraintViolations: {}", mannschaftViolations);
        // [ und ] aus dem String der Liste entfernen
        final var violationsStr = mannschaftViolations.toString();
        final var detail = violationsStr.substring(1, violationsStr.length() - 2);

        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, detail);
        problemDetail.setType(URI.create(STR."\{PROBLEM_PATH}\{ProblemType.CONSTRAINTS.getValue()}"));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));

        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onUsernameExists(
        final UsernameExistsException ex,
        final HttpServletRequest request
    ) {
        log.debug("onUsernameExists: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(STR."\{PROBLEM_PATH}\{ProblemType.CONSTRAINTS.getValue()}"));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onPasswordInvalid(
        final PasswordInvalidException ex,
        final HttpServletRequest request
    ) {
        log.debug("onPasswordInvalid: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(STR."\{PROBLEM_PATH}\{ProblemType.CONSTRAINTS.getValue()}"));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onVersionOutdated(
        final VersionOutdatedException ex,
        final HttpServletRequest request
    ) {
        log.debug("onVersionOutdated: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(PRECONDITION_FAILED, ex.getMessage());
        problemDetail.setType(URI.create(STR."\{PROBLEM_PATH}\{ProblemType.PRECONDITION.getValue()}"));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }

    @ExceptionHandler
    ProblemDetail onMessageNotReadable(
        final HttpMessageNotReadableException ex,
        final HttpServletRequest request
    ) {
        log.debug("onMessageNotReadable: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create(STR."\{PROBLEM_PATH}\{ProblemType.BAD_REQUEST.getValue()}"));
        problemDetail.setInstance(URI.create(request.getRequestURL().toString()));
        return problemDetail;
    }
}
