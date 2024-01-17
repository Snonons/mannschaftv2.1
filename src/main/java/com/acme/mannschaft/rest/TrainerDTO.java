package com.acme.mannschaft.rest;

/**
 * ValueObject für das Neuanlegen und Ändern einer neuen Mannschaft.
 *
 * @param vorname Vorname
 * @param nachname Nachname
 */
record TrainerDTO (
    String vorname,
    String nachname
) {
}
