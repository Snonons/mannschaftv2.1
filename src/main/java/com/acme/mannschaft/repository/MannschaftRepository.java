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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import static com.acme.mannschaft.entity.Mannschaft.TRAINER_GRAPH;
import static com.acme.mannschaft.entity.Mannschaft.TRAINER_SPIELERLIST_GRAPH;

/**
 * Repository für den DB-Zugriff bei Mannschaften.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Repository
public interface MannschaftRepository extends JpaRepository<Mannschaft, UUID>, JpaSpecificationExecutor<Mannschaft> {
    @EntityGraph(TRAINER_GRAPH)
    @NonNull
    @Override
    List<Mannschaft> findAll();

    @EntityGraph(TRAINER_GRAPH)
    @NonNull
    @Override
    List<Mannschaft> findAll(@NonNull Specification<Mannschaft> spec);

    @EntityGraph(TRAINER_GRAPH)
    @NonNull
    @Override
    Optional<Mannschaft> findById(@NonNull UUID id);

    /**
     * Mannschaft einschließlich Umsätze anhand der ID suchen.
     *
     * @param id Mannschaft ID
     * @return Gefundener Mannschaft
     */
    @Query("""
        SELECT DISTINCT m
        FROM     Mannschaft m
        WHERE    m.id = :id
        """)
    @EntityGraph(TRAINER_SPIELERLIST_GRAPH)
    @NonNull
    Optional<Mannschaft> findByIdFetchSpielerList(UUID id);

    /**
     * Mannschaften anhand des Namens suchen.
     *
     * @param name Der (Teil-) Name der gesuchten Mannschaften
     * @return Die gefundenen Mannschaften oder eine leere Collection
     */
    @Query("""
        SELECT   m
        FROM     Mannschaft m
        WHERE    lower(m.name) LIKE concat('%', lower(:name), '%')
        ORDER BY m.id
        """)
    @EntityGraph(TRAINER_GRAPH)
    Collection<Mannschaft> findByName(CharSequence name);

    /**
     * Abfrage, welche Namen es zu einem Präfix gibt.
     *
     * @param prefix Name-Präfix.
     * @return Die passenden Namen oder eine leere Collection.
     */
    @Query("""
        SELECT DISTINCT m.name
        FROM     Mannschaft m
        WHERE    lower(m.name) LIKE concat(lower(:prefix), '%')
        ORDER BY m.name
        """)
    Collection<String> findNamenByPrefix(String prefix);
}
