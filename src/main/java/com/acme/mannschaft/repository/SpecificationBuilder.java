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
package com.acme.mannschaft.repository;

import com.acme.mannschaft.entity.Mannschaft;
import com.acme.mannschaft.entity.Mannschaft_;
import com.acme.mannschaft.entity.Trainer_;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Singleton-Klasse, um Specifications für Queries in Spring Data JPA zu bauen.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Component
@Slf4j
public class SpecificationBuilder {
    /**
     * Specification für eine Query mit Spring Data bauen.
     *
     * @param queryParams als MultiValueMap
     * @return Specification für eine Query mit Spring Data
     */
    public Optional<Specification<Mannschaft>> build(final Map<String, ? extends List<String>> queryParams) {
        log.debug("build: queryParams={}", queryParams);

        if (queryParams.isEmpty()) {
            // keine Suchkriterien
            return Optional.empty();
        }

        final var specs = queryParams
            .entrySet()
            .stream()
            .map(this::toSpecification)
            .toList();

        if (specs.isEmpty() || specs.contains(null)) {
            return Optional.empty();
        }

        return Optional.of(Specification.allOf(specs));
    }

    @SuppressWarnings("CyclomaticComplexity")
    private Specification<Mannschaft> toSpecification(final Map.Entry<String, ? extends List<String>> entry) {
        log.trace("toSpec: entry={}", entry);
        final var key = entry.getKey();
        final var values = entry.getValue();

        if (values == null || values.size() != 1) {
            return null;
        }

        final var value = values.getFirst();
        return switch (key) {
            case "name" -> name(value);
            case "gruendungsjahr" -> gruendungsjahr(value);
            case "vorname" -> vorname(value);
            case "nachname" -> nachname(value);
            default -> null;
        };
    }

    private Specification<Mannschaft> name(final String teil) {
        // root ist jakarta.persistence.criteria.Root<Mannschaft>
        // query ist jakarta.persistence.criteria.CriteriaQuery<Mannschaft>
        // builder ist jakarta.persistence.criteria.CriteriaBuilder
        // https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/meta-model.html
        return (root, query, builder) -> builder.like(
            builder.lower(root.get(Mannschaft_.name)),
            builder.lower(builder.literal(STR."%\{teil}%"))
        );
    }

    private Specification<Mannschaft> gruendungsjahr(final String gruendungsjahr) {
        final LocalDate gruendungsjahrLocalDate;
        try {
            gruendungsjahrLocalDate = LocalDate.parse(gruendungsjahr);
        } catch (final NumberFormatException e) {
            //noinspection ReturnOfNull
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get(Mannschaft_.gruendungsjahr), gruendungsjahrLocalDate);
    }

    private Specification<Mannschaft> vorname(final String prefix) {
        return (root, query, builder) -> builder.like(root.get(Mannschaft_.trainer).get(Trainer_.vorname), STR."\{prefix}%");
    }

    private Specification<Mannschaft> nachname(final String prefix) {
        return (root, query, builder) -> builder.like(
            builder.lower(root.get(Mannschaft_.trainer).get(Trainer_.nachname)),
            builder.lower(builder.literal(STR."\{prefix}%"))
        );
    }
}
