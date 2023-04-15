package pl.lodz.p.it.ssbd2023.ssbd05.entities.mow;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.AbstractEntity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "meter")
@NamedQueries({
    @NamedQuery(
        name = "Meter.findAll",
        query = "SELECT m FROM Meter m"),
    @NamedQuery(
        name = "Meter.findById",
        query = "SELECT m FROM Meter m WHERE m.id = :id"),
    @NamedQuery(
        name = "Meter.findByCategoryId",
        query = "SELECT m FROM Meter m WHERE m.category.id = :categoryId"),
    @NamedQuery(
        name = "Meter.findByCategoryName",
        query = "SELECT m FROM Meter m WHERE m.category.name = :categoryName"),
    @NamedQuery(
        name = "Meter.findByPlaceId",
        query = "SELECT m FROM Meter m WHERE m.place.id = :placeId"),
    @NamedQuery(
        name = "Meter.findByPlaceNumberAndBuildingId",
        query = """
            SELECT m FROM Meter m
            WHERE m.place.placeNumber = :placeNumber
                  AND m.place.building.id = :buildingId"""),
    @NamedQuery(
        name = "Meter.findByCategoryIdAndPlaceId",
        query = """
            SELECT m FROM Meter m
            WHERE m.category.id = :categoryId
                  AND m.place.id = :placeId"""),
    @NamedQuery(
        name = "Meter.findByCategoryIdAndPlaceNumberAndBuildingId",
        query = """
            SELECT m FROM Meter m
            WHERE m.category.id = :categoryId
                  AND m.place.placeNumber = :placeNumber
                  AND m.place.building.id = :buildingId"""),
    @NamedQuery(
        name = "Meter.findByCategoryNameAndPlaceId",
        query = """
            SELECT m FROM Meter m
            WHERE m.category.name = :categoryName
                  AND m.place.id = :placeId"""),
    @NamedQuery(
        name = "Meter.findByCategoryNameAndPlaceNumberAndBuildingId",
        query = """
            SELECT m FROM Meter m
            WHERE m.category.name = :categoryName
                  AND m.place.placeNumber = :placeNumber
                  AND m.place.building.id = :buildingId""")
})
@NoArgsConstructor
public class Meter extends AbstractEntity implements Serializable {
    @NotNull
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "category_id", referencedColumnName = "id", updatable = false, nullable = false)
    @Getter
    @Setter
    private Category category;

    @NotNull
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "place_id", referencedColumnName = "id", updatable = false, nullable = false)
    @Getter
    @Setter
    private Place place;

    @NotNull
    @OneToMany(mappedBy = "meter", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Set<Reading> readings = new HashSet<>();

    public Meter(Category category, Place place) {
        this.category = category;
        this.place = place;
    }
}