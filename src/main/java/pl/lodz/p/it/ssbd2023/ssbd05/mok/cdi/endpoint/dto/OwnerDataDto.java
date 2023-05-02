package pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.mok.AccessType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class OwnerDataDto extends AccessLevelDto {
    @NotNull
    @Valid
    private AddressDto address;

    public OwnerDataDto(Long id, Long version, @NotNull AccessType level, AddressDto address) {
        super(id, version, level);
        this.address = address;
    }
}
