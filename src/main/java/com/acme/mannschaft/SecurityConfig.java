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
package com.acme.mannschaft;

import java.util.Map;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static com.acme.mannschaft.rest.MannschaftGetController.NAME_PATH;
import static com.acme.mannschaft.rest.MannschaftGetController.REST_PATH;
import static com.acme.mannschaft.security.AuthController.AUTH_PATH;
import static com.acme.mannschaft.security.Rolle.ADMIN;
import static com.acme.mannschaft.security.Rolle.MANNSCHAFT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

// https://github.com/spring-projects/spring-security/tree/master/samples
/**
 * Security-Konfiguration.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@SuppressWarnings("TrailingComment")
interface SecurityConfig {
    // https://foojay.io/today/how-to-do-password-hashing-in-java-applications-the-right-way
    int SALT_LENGTH = 32; // default: 16
    int HASH_LENGTH = 64; // default: 32
    int PARALLELISM = 1; // default: 1 (Bouncy Castle kann keine Parallelitaet)
    int NUMBER_OF_BITS = 14;
    int MEMORY_CONSUMPTION_KBYTES = 1 << NUMBER_OF_BITS; // default: 2^14 KByte = 16 MiB  ("Memory Cost Parameter")
    int ITERATIONS = 3; // default: 3

    /**
     * Bean-Definition, um den Zugriffsschutz an der REST-Schnittstelle zu konfigurieren,
     * d.h. vor Anwendung von @PreAuthorize.
     *
     * @param http Injiziertes Objekt von HttpSecurity als Ausgangspunkt für die Konfiguration.
     * @return Objekt von SecurityFilterChain
     * @throws Exception Wegen HttpSecurity.authorizeHttpRequests()
     */
    // https://github.com/spring-projects/spring-security-samples/blob/main/servlet/java-configuration/...
    // ...authentication/preauth/src/main/java/example/SecurityConfiguration.java
    @Bean
    @SuppressWarnings("LambdaBodyLength")
    default SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(authorize -> {
                final var restPathMannschaftId = STR."\{REST_PATH}/*";
                authorize
                    // https://spring.io/blog/2020/06/30/url-matching-with-pathpattern-in-spring-mvc
                    // https://docs.spring.io/spring-security/reference/current/servlet/integrations/mvc.html
                    .requestMatchers(GET, REST_PATH).hasRole(ADMIN.name())
                    .requestMatchers(
                        GET,
                        STR."\{REST_PATH}\{NAME_PATH}/*",
                        "/swagger-ui.html"
                    ).hasRole(ADMIN.name())
                    .requestMatchers(GET, restPathMannschaftId).hasAnyRole(ADMIN.name(), MANNSCHAFT.name())
                    .requestMatchers(PUT, restPathMannschaftId).hasRole(ADMIN.name())
                    .requestMatchers(PATCH, restPathMannschaftId).hasRole(ADMIN.name())
                    .requestMatchers(DELETE, restPathMannschaftId).hasRole(ADMIN.name())
                    .requestMatchers(
                        GET,
                        STR."\{AUTH_PATH}/rollen",
                        STR."\{REST_PATH}\{NAME_PATH}/*"
                    ).hasRole(MANNSCHAFT.name())
                    .requestMatchers(POST, "/dev/db_populate").hasRole(ADMIN.name())

                    .requestMatchers(POST, REST_PATH, "/graphql", STR."\{AUTH_PATH}/login").permitAll()

                    .requestMatchers(
                        // Actuator: Health mit Liveness und Readiness fuer Kubernetes
                        EndpointRequest.to(HealthEndpoint.class),
                        // Actuator: Prometheus fuer Monitoring
                        EndpointRequest.to(PrometheusScrapeEndpoint.class)
                    ).permitAll()
                    // OpenAPI bzw. Swagger UI und GraphiQL
                    .requestMatchers(GET, "/v3/api-docs.yaml", "/v3/api-docs", "/graphiql").permitAll()
                    .requestMatchers("/error", "/error/**").permitAll()

                    // https://docs.spring.io/spring-security/reference/servlet/integrations/mvc.html
                    // https://marco.dev/spring-boot-h2-error
                    // .requestMatchers(toH2Console()).permitAll()

                    .anyRequest().authenticated();
            })
            .httpBasic(withDefaults())
            // Spring Security erzeugt keine HttpSession und verwendet keine fuer SecurityContext
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .formLogin(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
            .build();
    }

    /**
     * Bean-Definition, um den Verschlüsselungsalgorithmus für Passwörter bereitzustellen.
     * Es wird Argon2id statt bcrypt (Default-Algorithmus von Spring Security) verwendet.
     *
     * @return Objekt für die Verschlüsselung von Passwörtern.
     */
    @Bean
    default PasswordEncoder passwordEncoder() {
        // https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html
        // https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/Password_Storage_Cheat_Sheet.md
        // https://www.rfc-editor.org/rfc/rfc9106.html
        final var idForEncode = "argon2id";
        final Map<String, PasswordEncoder> encoders = Map.of(
            idForEncode,
            new Argon2PasswordEncoder(
                SALT_LENGTH,
                HASH_LENGTH,
                PARALLELISM,
                MEMORY_CONSUMPTION_KBYTES,
                ITERATIONS
            )
        );
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }
}
