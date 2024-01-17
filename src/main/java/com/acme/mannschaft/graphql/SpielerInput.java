package com.acme.mannschaft.graphql;

/**
 * Name für einen Spieler.
 *
 * @param vorname Der Vorname des Spieler.
 * @param nachname Die Nachname des Spieler.
 */
record SpielerInput (String vorname, String nachname){
}
