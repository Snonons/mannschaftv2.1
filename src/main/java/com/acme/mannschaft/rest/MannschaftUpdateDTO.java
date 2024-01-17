package com.acme.mannschaft.rest;

import java.time.LocalDate;

/**
 * ValueObject für das Ändern einer neuen Mannschaft.
 *
 * @param name Gültiger Name einer Mannschaft.
 * @param gruendungsjahr Gründungsjahr einer Mannschaft.
 */
record MannschaftUpdateDTO(
    String name,
    LocalDate gruendungsjahr
) {
}
