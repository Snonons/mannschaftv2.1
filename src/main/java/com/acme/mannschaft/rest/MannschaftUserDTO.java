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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ValueObject für einen neuen Mannschaften mit den Benutzerdaten an der REST-Schnittstelle.
 * Beim Lesen wird die Klasse MannschaftModel für die Ausgabe verwendet, d.h. ohne die Benutzerdaten.
 * @param mannschaftDTO Der Mannschaft.
 * @param userDTO Die Benutzerdaten.
 */
record MannschaftUserDTO(
    @JsonProperty("mannschaft")
    MannschaftDTO mannschaftDTO,

    @JsonProperty("user")
    CustomUserDTO userDTO
) {
}
