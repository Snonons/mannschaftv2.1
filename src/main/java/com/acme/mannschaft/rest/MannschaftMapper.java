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
package com.acme.mannschaft.rest;

import com.acme.mannschaft.entity.Mannschaft;
import com.acme.mannschaft.entity.Spieler;
import com.acme.mannschaft.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

/**
 * Mapper zwischen Entity-Klassen.
 * Siehe build\generated\sources\annotationProcessor\java\main\...\MannschaftMapperImpl.java.
 */
@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
interface MannschaftMapper {
    /**
     * Ein DTO-Objekt von MannschaftDTO in ein Objekt für ein neues Mannschaft-Objekt konvertieren.
     *
     * @param dto DTO-Objekt für MannschaftDTO ohne ID, version, erzeugt, aktualisiert, interesseStr, username
     * @return Konvertiertes Mannschaft-Objekt mit null als ID
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "erzeugt", ignore = true)
    @Mapping(target = "aktualisiert", ignore = true)
    @Mapping(target = "username", ignore = true)
    Mannschaft toMannschaft(MannschaftDTO dto);

    /**
     * Ein DTO-Objekt von TrainerDTO in ein Objekt für Trainer konvertieren.
     *
     * @param dto DTO-Objekt für TrainerDTO ohne ID und mannschaft
     * @return Konvertiertes Trainer-Objekt mit null als ID
     */
    @Mapping(target = "id", ignore = true)
    Trainer toTrainer(TrainerDTO dto);

    /**
     * Ein DTO-Objekt von SpielerDTO in ein Objekt für Spieler konvertieren.
     *
     * @param dto DTO-Objekt für SpielerDTO ohne ID und mannschaft
     * @return Konvertiertes Spieler-Objekt mit null als ID
     */
    @Mapping(target = "id", ignore = true)
    Spieler toSpieler(SpielerDTO dto);

    /**
     * Ein DTO-Objekt von MannschaftDTO in ein Objekt für ein zu änderndes Mannschaft-Objekt konvertieren.
     *
     * @param dto DTO-Objekt für MannschaftUpdateDTO ohne ID, version, erzeugt, aktualisiert, trainer, spielerList,
     *            interesseStr, username
     * @return Konvertiertes Mannschaft-Objekt mit null als ID, trainer und spielerList
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "erzeugt", ignore = true)
    @Mapping(target = "aktualisiert", ignore = true)
    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "spielerList", ignore = true)
    @Mapping(target = "username", ignore = true)
    Mannschaft toMannschaft(MannschaftUpdateDTO dto);
}
