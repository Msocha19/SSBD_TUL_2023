package pl.lodz.p.it.ssbd2023.mok.ejb.facades;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.lodz.p.it.ssbd2023.entities.mok.AdminData;
import pl.lodz.p.it.ssbd2023.shared.AbstractFacade;

@Stateless
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class AdminDataFacade extends AbstractFacade<AdminData> {

    @PersistenceContext(unitName = "ssbd05mokPU")
    private EntityManager em;

    public AdminDataFacade() {
        super(AdminData.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
