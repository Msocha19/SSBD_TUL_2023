package pl.lodz.p.it.ssbd2023.mow.ejb.facades;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import pl.lodz.p.it.ssbd2023.entities.mow.Reading;
import pl.lodz.p.it.ssbd2023.exceptions.DatabaseException;
import pl.lodz.p.it.ssbd2023.shared.AbstractFacade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class ReadingFacade extends AbstractFacade<Reading> {

    @PersistenceContext(unitName = "ssbd05mowPU")
    private EntityManager em;


    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ReadingFacade() {
        super(Reading.class);
    }

    public List<Reading> findByValue(BigDecimal value) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByValue", Reading.class);
            tq.setParameter("value", value);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByValue , Database Exception", e);
        }
    }

    public List<Reading> findByDate(LocalDateTime date) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByDate", Reading.class);
            tq.setParameter("date", date);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByDate , Database Exception", e);
        }
    }

    public List<Reading> findByDateAfter(LocalDateTime date) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByDateAfter", Reading.class);
            tq.setParameter("date", date);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByDateAfter , Database Exception", e);
        }
    }

    public List<Reading> findByDateBefore(LocalDateTime date) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByDateBefore", Reading.class);
            tq.setParameter("date", date);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByDateBefore , Database Exception", e);
        }
    }

    public List<Reading> findByDateBetween(LocalDateTime beginDate, LocalDateTime endDate) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByDateBetween", Reading.class);
            tq.setParameter("beginDate", beginDate);
            tq.setParameter("endDate", endDate);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByDateBetween , Database Exception", e);
        }
    }

    public List<Reading> findByMeterId(Long meterId) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByMeterId", Reading.class);
            tq.setParameter("meterId", meterId);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByMeterId , Database Exception", e);
        }
    }

    public List<Reading> findByMeterIdAndDate(Long meterId, LocalDateTime date) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByMeterIdAndDate", Reading.class);
            tq.setParameter("meterId", meterId);
            tq.setParameter("date", date);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByMeterIdAndDate , Database Exception", e);
        }
    }

    public List<Reading> findByMeterIdAndDateAfter(Long meterId, LocalDateTime date) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByMeterIdAndDateAfter", Reading.class);
            tq.setParameter("meterId", meterId);
            tq.setParameter("date", date);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByMeterIdAndDateAfter , Database Exception", e);
        }
    }

    public List<Reading> findByMeterIdAndDateBefore(Long meterId, LocalDateTime date) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByMeterIdAndDateBefore", Reading.class);
            tq.setParameter("meterId", meterId);
            tq.setParameter("date", date);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByMeterIdAndDateBefore , Database Exception", e);
        }
    }

    public List<Reading> findByMeterIdAndDateBetween(Long meterId, LocalDateTime beginDate, LocalDateTime endDate)
        throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByMeterIdAndDateBetween", Reading.class);
            tq.setParameter("meterId", meterId);
            tq.setParameter("beginDate", beginDate);
            tq.setParameter("endDate", endDate);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByMeterIdAndDateBetween , Database Exception", e);
        }
    }

    public List<Reading> findByPlaceId() throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByPlaceId", Reading.class);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByPlaceId , Database Exception", e);
        }
    }

    public List<Reading> findByPlaceIdAndDate(LocalDateTime date) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByPlaceIdAndDate", Reading.class);
            tq.setParameter("date", date);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByPlaceIdAndDate , Database Exception", e);
        }
    }

    public List<Reading> findByPlaceIdAndDateBetween(LocalDateTime beginDate, LocalDateTime endDate)
        throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByPlaceIdAndDateBetween", Reading.class);
            tq.setParameter("beginDate", beginDate);
            tq.setParameter("endDate", endDate);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByPlaceIdAndDateBetween , Database Exception", e);
        }
    }

    public List<Reading> findByPlaceIdAndDateAfter(LocalDateTime date) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByPlaceIdAndDateAfter", Reading.class);
            tq.setParameter("date", date);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByPlaceIdAndDateAfter , Database Exception", e);
        }
    }

    public List<Reading> findByPlaceIdAndDateBefore(LocalDateTime date) throws DatabaseException {
        try {
            TypedQuery<Reading> tq = em.createNamedQuery("Reading.findByPlaceIdAndDateBefore", Reading.class);
            tq.setParameter("date", date);
            return tq.getResultList();
        } catch (PersistenceException e) {
            throw new DatabaseException("Reading.findByPlaceIdAndDateBefore , Database Exception", e);
        }
    }
}
