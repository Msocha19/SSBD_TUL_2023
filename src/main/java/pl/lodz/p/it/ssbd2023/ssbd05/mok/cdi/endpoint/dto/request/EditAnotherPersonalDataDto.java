package pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.mok.Language;
import pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.AccessLevelDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditAnotherPersonalDataDto {
    @NotNull
    private Long id;

    @NotNull
    private Long version;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 1, max = 100)
    private String firstName;

    @NotBlank
    @Size(min = 1, max = 100)
    private String lastName;

    @NotNull
    private Language language;

    @NotNull
    private List<AccessLevelDto> accessLevels;
}
