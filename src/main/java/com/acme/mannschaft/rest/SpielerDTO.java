package com.acme.mannschaft.rest;

/**
 * ValueObject für das Neuanlegen und Ändern einer neuen Mannschaft.
 *
 * @param vorname Vorname
 * @param nachname Nachname
 */
record SpielerDTO (
    String vorname,
    String nachname
) {
}
