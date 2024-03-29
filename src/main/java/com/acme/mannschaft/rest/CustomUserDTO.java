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

import com.acme.mannschaft.security.CustomUser;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import static com.acme.mannschaft.security.Rolle.MANNSCHAFT;
import static com.acme.mannschaft.security.Rolle.ROLE_PREFIX;

/**
 * DTO für Benutzerdaten. Public für AuthController.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 * @param username Benutzername
 * @param password Passwort
 */
public record CustomUserDTO(String username, String password) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns.
     *
     * @return Objekt für den Anwendungskern
     */
    UserDetails toUserDetails() {
        return new CustomUser(username, password, List.of
            (new SimpleGrantedAuthority(STR."\{ROLE_PREFIX}\{MANNSCHAFT}")));
    }
}
