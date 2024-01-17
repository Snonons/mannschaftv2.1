package com.acme.mannschaft.graphql;

import java.util.List;

import com.acme.mannschaft.security.CustomUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import static com.acme.mannschaft.security.Rolle.MANNSCHAFT;
import static com.acme.mannschaft.security.Rolle.ROLE_PREFIX;

/**
 * Eine Value-Klasse für Eingabedaten passend zu MannschaftInput aus dem GraphQL-Schema.
 *
 * @param name Name
 * @param gruendungsjahr Gründungsjahr
 * @param spielerList Liste der Spieler
 * @param trainer Trainer
 */
record MannschaftInput (
    String name,
    String gruendungsjahr,
    List<SpielerInput> spielerList,
    TrainerInput trainer,
    String username,
    String password
) {
    /**
     * Konvertierung in ein Objekt der Entity-Klasse CustomUser.
     *
     * @return Das konvertierte CustomUser-Objekt
     */
    UserDetails toUserDetails() {
        return new CustomUser(username, password, List.of(new SimpleGrantedAuthority(STR."\{ROLE_PREFIX}\{MANNSCHAFT}")));
    }
}
