package pl.lodz.p.it.ssbd2023.ssbd05.mow.ejb.managers.impl;

import static pl.lodz.p.it.ssbd2023.ssbd05.shared.Roles.MANAGER;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionSynchronization;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptors;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.mow.Rate;
import pl.lodz.p.it.ssbd2023.ssbd05.exceptions.AppBaseException;
import pl.lodz.p.it.ssbd2023.ssbd05.exceptions.conflict.RateAlreadyEffectiveException;
import pl.lodz.p.it.ssbd2023.ssbd05.interceptors.GenericManagerExceptionsInterceptor;
import pl.lodz.p.it.ssbd2023.ssbd05.interceptors.LoggerInterceptor;
import pl.lodz.p.it.ssbd2023.ssbd05.mow.ejb.facades.RateFacade;
import pl.lodz.p.it.ssbd2023.ssbd05.mow.ejb.managers.RateManagerLocal;
import pl.lodz.p.it.ssbd2023.ssbd05.shared.AbstractManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Stateful
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Interceptors({
    GenericManagerExceptionsInterceptor.class,
    LoggerInterceptor.class,
})
@DenyAll
public class RateManager extends AbstractManager implements RateManagerLocal, SessionSynchronization {

    @Inject
    private RateFacade rateFacade;

    @Override
    @PermitAll
    public List<Rate> getCurrentRates() throws AppBaseException {
        return rateFacade.findCurrentRates();
    }

    @Override
    @RolesAllowed(MANAGER)
    public void createRate() throws AppBaseException {
        throw new UnsupportedOperationException();
    }

    @Override
    @RolesAllowed(MANAGER)
    public void removeFutureRate(Long id) throws AppBaseException {
        Optional<Rate> optionalRate = rateFacade.find(id);
        if (optionalRate.isPresent()) {
            Rate rate = optionalRate.get();
            LocalDate now = LocalDate.now();
            if (rate.getEffectiveDate().isBefore(now)) {
                throw new RateAlreadyEffectiveException();
            }
            rateFacade.remove(rate);
        }
    }
}
