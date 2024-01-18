/*
 * Copyright (C) 2023 - present Juergen Zimmermann, Hochschule Karlsruhe
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.mannschaft.graphql;

import com.acme.mannschaft.entity.Trainer;
import com.acme.mannschaft.entity.Mannschaft;
import com.acme.mannschaft.entity.Spieler;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

/**
 * Mapper zwischen Entity-Klassen.
 * Siehe build\generated\sources\annotationProcessor\java\...\MannschaftInputMapperImpl.java.
 */
@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface MannschaftInputMapper {
    /**
     * Ein MannschaftInput-Objekt in ein Objekt für Mannschaft konvertieren.
     *
     * @param input MannschaftInput ohne ID, version, erzeugt, aktualisiert, interesseStr
     * @return Konvertiertes Mannschaft-Objekt mit null als ID
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "erzeugt", ignore = true)
    @Mapping(target = "aktualisiert", ignore = true)
    Mannschaft toMannschaft(MannschaftInput input);

    /**
     * Ein TrainerInput-Objekt in ein Objekt für Trainer konvertieren.
     *
     * @param input TrainerInput ohne ID und mannschaft
     * @return Konvertiertes Trainer-Objekt mit null als ID
     */
    @Mapping(target = "id", ignore = true)
    Trainer toTrainer(TrainerInput input);

    /**
     * Ein SpielerInput-Objekt in ein Objekt für Spieler konvertieren.
     *
     * @param input SpielerInput-Objekt ohne ID und mannschaft
     * @return Konvertiertes Spieler-Objekt mit null als ID
     */
    @Mapping(target = "id", ignore = true)
    Spieler toSpieler(SpielerInput input);
}
