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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.mannschaft.rest.patch;

import com.acme.mannschaft.entity.Mannschaft;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collection;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static com.acme.mannschaft.rest.patch.PatchOperationType.REPLACE;

/**
 * Klasse, um PATCH-Operationen auf Mannschaft-Objekte anzuwenden.
 */
@Component
@Slf4j
public final class MannschaftPatcher {
    MannschaftPatcher() {
    }

    /**
     * PATCH-Operationen werden auf ein Mannschaft-Objekt angewandt.
     *
     * @param mannschaft Das zu modifizierende Mannschaft-Objekt.
     * @param operations Die anzuwendenden Operationen.
     * @param request Das Request-Objekt, um ggf. die URL für ProblemDetail zu ermitteln
     * @throws InvalidPatchOperationException Falls die Patch-Operation nicht korrekt ist.
     */
    public void patch(
        final Mannschaft mannschaft,
        final Collection<PatchOperation> operations,
        final HttpServletRequest request
    ) {
        final var replaceOps = operations.stream()
            .filter(op -> op.op() == REPLACE)
            .toList();
        log.debug("patch: replaceOps={}", replaceOps);
        final var uri = URI.create(request.getRequestURL().toString());
        replaceOps(mannschaft, replaceOps, uri);
    }

    private void replaceOps
        (final Mannschaft mannschaft, @NonNull final Iterable<@NonNull PatchOperation> ops, final URI uri) {
        ops.forEach(op -> {
            switch (op.path()) {
                case "/name" -> mannschaft.setName(op.value());
                default -> throw new InvalidPatchOperationException(uri);
            }
        });
        log.trace("replaceOps: mannschaft={}", mannschaft);
    }
}
