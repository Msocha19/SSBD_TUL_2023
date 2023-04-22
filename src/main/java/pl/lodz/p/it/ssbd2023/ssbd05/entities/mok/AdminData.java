package pl.lodz.p.it.ssbd2023.ssbd05.entities.mok;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "admin_data")
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
public class AdminData extends AccessLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    public AdminData(Account account) {
        super(AccessType.ADMIN, account);
    }
}
