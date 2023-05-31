package pl.lodz.p.it.ssbd2023.ssbd05.mow.ejb.facades;

import static pl.lodz.p.it.ssbd2023.ssbd05.shared.Roles.MANAGER;
import static pl.lodz.p.it.ssbd2023.ssbd05.shared.Roles.OWNER;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.mow.AccountingRule;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.mow.Category;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.mow.Rate;
import pl.lodz.p.it.ssbd2023.ssbd05.shared.AbstractFacade;
import pl.lodz.p.it.ssbd2023.ssbd05.shared.Page;

import java.time.LocalDate;
import java.util.List;

@Stateless
@DenyAll
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class RateFacade extends AbstractFacade<Rate> {
    @PersistenceContext(unitName = "ssbd05mowPU")
    private EntityManager em;

    public RateFacade() {
        super(Rate.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    //CurrentRates

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findCurrentRates() {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findCurrentRates", Rate.class);
        return tq.getResultList();
    }

    //AccountingRules

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByAccountingRule(AccountingRule accountingRule) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByAccountingRule", Rate.class);
        tq.setParameter("accounting_rule", accountingRule);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateAndAccountingRule(LocalDate effectiveDate, AccountingRule accountingRule) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDateAndAccountingRule", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        tq.setParameter("ar", accountingRule);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateBeforeAndAccountingRule(LocalDate effectiveDate,
                                                                 AccountingRule accountingRule) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDateBeforeAndAccountingRule", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        tq.setParameter("ar", accountingRule);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateAfterAndAccountingRule(LocalDate effectiveDate,
                                                                AccountingRule accountingRule) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDateAfterAndAccountingRule", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        tq.setParameter("ar", accountingRule);
        return tq.getResultList();
    }

    // Category

    @RolesAllowed({MANAGER})
    public List<Rate> findByCategoryId(Category category) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByCategory", Rate.class);
        tq.setParameter("category", category);
        return tq.getResultList();
    }

    @RolesAllowed({MANAGER})
    public Page<Rate> findByCategoryId(Long categoryId, int page, int pageSize) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByCategoryId", Rate.class);
        tq.setParameter("categoryId", categoryId);

        tq.setFirstResult(page * pageSize);
        tq.setMaxResults(pageSize);

        Long count = countByCategoryId(categoryId);
        return new Page<>(tq.getResultList(), count, pageSize, page);
    }

    @RolesAllowed({MANAGER})
    public Long countByCategoryId(Long categoryId) {
        TypedQuery<Long> tq = em.createNamedQuery("Rate.countByCategoryId", Long.class);
        tq.setParameter("categoryId", categoryId);
        return tq.getSingleResult();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateAndCategory(LocalDate effectiveDate, Category category) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDateAndCategory", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        tq.setParameter("category", category);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateBeforeAndCategory(LocalDate effectiveDate, Category category) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDateBeforeAndCategory", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        tq.setParameter("category", category);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateAfterAndCategory(LocalDate effectiveDate, Category category) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDateAfterAndCategory", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        tq.setParameter("category", category);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByCategoryAndAccountingRule(Category category, AccountingRule accountingRule) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByCategoryAndAccountingRule", Rate.class);
        tq.setParameter("category", category);
        tq.setParameter("ar", accountingRule);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateAndCategoryAndAccountingRule(LocalDate effectiveDate, Category category,
                                                                      AccountingRule accountingRule) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDateAndCategoryAndAccountingRule", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        tq.setParameter("category", category);
        tq.setParameter("accounting_rule", accountingRule);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateBeforeAndCategoryAndAccountingRule(LocalDate effectiveDate, Category category,
                                                                            AccountingRule accountingRule) {
        TypedQuery<Rate> tq =
            em.createNamedQuery("Rate.findByEffectiveDateBeforeAndCategoryAndAccountingRule", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        tq.setParameter("category", category);
        tq.setParameter("accounting_rule", accountingRule);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateAfterAndCategoryAndAccountingRule(LocalDate effectiveDate, Category category,
                                                                           AccountingRule accountingRule) {
        TypedQuery<Rate> tq =
            em.createNamedQuery("Rate.findByEffectiveDateAfterAndCategoryAndAccountingRule", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        tq.setParameter("category", category);
        tq.setParameter("accounting_rule", accountingRule);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDate(LocalDate effectiveDate) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDate", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateBefore(LocalDate effectiveDate) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDateBefore", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        return tq.getResultList();
    }

    @RolesAllowed({OWNER, MANAGER})
    public List<Rate> findByEffectiveDateAfter(LocalDate effectiveDate) {
        TypedQuery<Rate> tq = em.createNamedQuery("Rate.findByEffectiveDateAfter", Rate.class);
        tq.setParameter("effectiveDate", effectiveDate);
        return tq.getResultList();
    }

}
