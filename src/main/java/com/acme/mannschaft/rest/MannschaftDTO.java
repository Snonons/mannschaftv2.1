package com.acme.mannschaft.rest;

import java.time.LocalDate;
import java.util.List;

/**
 * ValueObject für das Neuanlegen und Ändern einer neuen Mannschaft. Beim Lesen wird die Klasse MannschaftModel für die Ausgabe
 * verwendet.
 *
 * @param name Gültiger Name einer Mannschaft.
 * @param gruendungsjahr Gründungsjahr einer Mannschaft.
 * @param spielerList Die Liste der Spieler einer Mannschaft.
 * @param trainer Der Trainer einer Mannschaft.
 */
@SuppressWarnings("RecordComponentNumber")
record MannschaftDTO(
    String name,
    LocalDate gruendungsjahr,
    List<SpielerDTO> spielerList,
    TrainerDTO trainer
) {
}
