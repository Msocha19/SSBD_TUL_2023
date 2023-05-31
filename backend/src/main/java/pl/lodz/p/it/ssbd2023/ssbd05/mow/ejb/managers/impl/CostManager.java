package pl.lodz.p.it.ssbd2023.ssbd05.mow.ejb.managers.impl;

import static pl.lodz.p.it.ssbd2023.ssbd05.shared.Roles.MANAGER;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionSynchronization;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptors;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.mow.Cost;
import pl.lodz.p.it.ssbd2023.ssbd05.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2023.ssbd05.interceptors.GenericManagerExceptionsInterceptor;
import pl.lodz.p.it.ssbd2023.ssbd05.interceptors.LoggerInterceptor;
import pl.lodz.p.it.ssbd2023.ssbd05.mow.ejb.facades.CostFacade;
import pl.lodz.p.it.ssbd2023.ssbd05.mow.ejb.managers.CostManagerLocal;
import pl.lodz.p.it.ssbd2023.ssbd05.shared.AbstractManager;

import java.util.List;

@Stateful
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Interceptors({
    GenericManagerExceptionsInterceptor.class,
    LoggerInterceptor.class,
})
@DenyAll
public class CostManager extends AbstractManager implements CostManagerLocal, SessionSynchronization {

    @Inject
    private CostFacade costFacade;

    @Override
    @RolesAllowed(MANAGER)
    public List<Cost> getAllCosts() throws AppBaseException {
        throw new UnsupportedOperationException();
    }

    @Override
    @RolesAllowed(MANAGER)
    public void createCost() throws AppBaseException {
        throw new UnsupportedOperationException();
    }

    @Override
    @RolesAllowed(MANAGER)
    public Cost getCostDetails(Long id) throws AppBaseException {
        throw new UnsupportedOperationException();
    }

    @Override
    @RolesAllowed(MANAGER)
    public void removeCost(Long id) throws AppBaseException {
        throw new UnsupportedOperationException();
    }
}