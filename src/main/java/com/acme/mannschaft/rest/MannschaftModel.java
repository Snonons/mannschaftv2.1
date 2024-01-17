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

import com.acme.mannschaft.entity.Trainer;
import com.acme.mannschaft.entity.Spieler;
import com.acme.mannschaft.entity.Mannschaft;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * Model-Klasse für Spring HATEOAS. @lombok.Data fasst die Annotationen @ToString, @EqualsAndHashCode, @Getter, @Setter
 * und @RequiredArgsConstructor zusammen.
 * <img src="../../../../../asciidoc/MannschaftModel.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@JsonPropertyOrder({
    "name", "gruendungsjahr", "spielerList", "trainer"
})
@Relation(collectionRelation = "mannschaften", itemRelation = "mannschaft")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@ToString(callSuper = true)
class MannschaftModel extends RepresentationModel<MannschaftModel> {
    private final String name;
    private final LocalDate gruendungsjahr;
    private final List<Spieler> spielerList;
    private final Trainer trainer;

    MannschaftModel(final Mannschaft mannschaft) {
        name = mannschaft.getName();
        gruendungsjahr = mannschaft.getGruendungsjahr();
        spielerList = mannschaft.getSpielerList();
        trainer = mannschaft.getTrainer();
    }
}
