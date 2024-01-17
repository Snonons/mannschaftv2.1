package com.acme.mannschaft.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.UniqueElements;
import static com.acme.mannschaft.entity.Mannschaft.TRAINER_GRAPH;
import static com.acme.mannschaft.entity.Mannschaft.TRAINER_SPIELERLIST_GRAPH;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static java.util.Collections.emptyList;

/**
 * Daten einer Mannschaft. In DDD ist Mannschaft ein Aggregate Root.
 */
@Entity
@Table(name = "mannschaft")
@NamedEntityGraph(name = TRAINER_GRAPH, attributeNodes = @NamedAttributeNode("trainer"))
@NamedEntityGraph(name = TRAINER_SPIELERLIST_GRAPH, attributeNodes = {
    @NamedAttributeNode("trainer"), @NamedAttributeNode("spielerList")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@ToString
public class Mannschaft {
    public interface NeuValidation {
    }

    /**
     * NamedEntityGraph für das Attribut "trainer".
     */
    public static final String TRAINER_GRAPH = "Mannschaft.adresse";

    /**
     * NamedEntityGraph für die Attribute "trainer" und "spielerList".
     */
    public static final String TRAINER_SPIELERLIST_GRAPH = "Mannschaft.trainerSpielerList";

    private static final int USERNAME_MAX_LENGTH = 20;

    /**
     * Die ID der Mannschaft.
     */
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * Versionsnummer für optimistische Synchronisation.
     */
    @Version
    private int version;

    /**
     * Der Name der Mannschaft.
     */
    @NotNull
    private String name;

    /**
     * Das Gründungsjahr der Mannschaft.
     */
    @Past
    private LocalDate gruendungsjahr;

    /**
     * Liste der Spieler, die der Mannschaft angehören.
     */
    @OneToMany(cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "mannschaft_id")
    @OrderColumn(name = "idx", nullable = false)
    @Valid
    @NotNull
    @ToString.Exclude
    private List<Spieler> spielerList;

    /**
     * Der Trainer der Mannschaft.
     */
    @OneToOne(optional = false, cascade = {PERSIST, REMOVE}, fetch = LAZY, orphanRemoval = true)
    @Valid
    @NotNull(groups = Mannschaft.NeuValidation.class)
    private Trainer trainer;

    @Size(max = USERNAME_MAX_LENGTH)
    private String username;

    @CreationTimestamp
    private LocalDateTime erzeugt;

    @UpdateTimestamp
    private LocalDateTime aktualisiert;

    /**
     * Mannschaftendaten überschreiben.
     *
     * @param mannschaft Neue Mannschaftendaten.
     */
    public void set(final Mannschaft mannschaft) {
        name = mannschaft.name;
        gruendungsjahr = mannschaft.gruendungsjahr;
    }
}
