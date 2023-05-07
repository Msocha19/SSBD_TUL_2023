package pl.lodz.p.it.ssbd2023.ssbd05.mok.cdi.endpoint.dto.request;

import jakarta.validation.constraints.AssertFalse;
import lombok.Data;
import pl.lodz.p.it.ssbd2023.ssbd05.utils.annotations.ValidPassword;

@Data
public class ChangePasswordDto {

    @ValidPassword
    private String oldPassword;

    @ValidPassword
    private String newPassword;

    @AssertFalse
    public boolean isPasswordsSame() {
        return oldPassword.equals(newPassword);
    }

}
