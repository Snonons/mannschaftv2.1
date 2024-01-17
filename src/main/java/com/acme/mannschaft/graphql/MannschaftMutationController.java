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
import com.acme.mannschaft.service.ConstraintViolationsException;
import com.acme.mannschaft.service.EmailExistsException;
import com.acme.mannschaft.service.MannschaftWriteService;
import graphql.GraphQLError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import static org.springframework.graphql.execution.ErrorType.BAD_REQUEST;

/**
 * Eine Controller-Klasse für das Schreiben mit der GraphQL-Schnittstelle und den Typen aus dem GraphQL-Schema.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Controller
@RequiredArgsConstructor
@Slf4j
class MannschaftMutationController {
    private final MannschaftWriteService service;
    private final MannschaftInputMapper mapper;

    /**
     * Einen neuen Mannschaften anlegen.
     *
     * @param input Die Eingabedaten für einen neuen Mannschaften
     * @return Die generierte ID für den neuen Mannschaften als Payload
     */
    @MutationMapping
    CreatePayload create(@Argument final MannschaftInput input, @AuthenticationPrincipal final User user) {
        log.debug("create: input={}", input);
        final var mannschaftNew = mapper.toMannschaft(input);
        final var id = service.create(mannschaftNew, user).getId();
        log.debug("create: id={}", id);
        return new CreatePayload(id);
    }

    @GraphQlExceptionHandler
    @SuppressWarnings("unused")
    GraphQLError onDateTimeParseException(final DateTimeParseException ex) {
        final List<Object> path = List.of("input", "geburtsdatum");
        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message(STR."Das Datum \{ex.getParsedString()} ist nicht korrekt.")
            .path(path)
            .build();
    }

    @GraphQlExceptionHandler
    @SuppressWarnings("unused")
    Collection<GraphQLError> onConstraintViolations(final ConstraintViolationsException ex) {
        return ex.getViolations()
            .stream()
            .map(this::violationToGraphQLError)
            .collect(Collectors.toList());
    }

    private GraphQLError violationToGraphQLError(final ConstraintViolation<Mannschaft> violation) {
        // String oder Integer als Listenelement
        final List<Object> path = new ArrayList<>(List.of("input"));
        violation.getPropertyPath()
            .forEach(node -> path.add(node.toString()));

        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message(violation.getMessage())
            .path(path)
            .build();
    }
}
