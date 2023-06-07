package pl.lodz.p.it.ssbd2023.ssbd05.entities.mow;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.ssbd2023.ssbd05.entities.AbstractEntity;
import pl.lodz.p.it.ssbd2023.ssbd05.mow.EntityControlListenerMOW;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rate", uniqueConstraints = {
    @UniqueConstraint(name = "unq_rate_0", columnNames = {"effective_date", "category_id"})})
@NoArgsConstructor
@NamedQueries({
    @NamedQuery(
        name = "Rate.findAll",
        query = "SELECT r FROM Rate r"),
    @NamedQuery(
        name = "Rate.findById",
        query = "SELECT r FROM Rate r WHERE r.id = :id"),
    @NamedQuery(
        name = "Rate.findCurrentRates",
        query = """
            SELECT r FROM Rate r
            WHERE r.effectiveDate = 
                (SELECT max(r1.effectiveDate) 
                 FROM Rate r1 WHERE r1.effectiveDate <= CURRENT_DATE AND r1.category = r.category)
            """),
    @NamedQuery(
        name = "Rate.findCurrentRateByCategoryId",
        query = """
            SELECT r FROM Rate r
            WHERE r.effectiveDate = 
                (SELECT max(r1.effectiveDate) 
                 FROM Rate r1 WHERE r1.effectiveDate <= CURRENT_DATE AND r1.category = r.category)
            AND r.category.id = :categoryId
            """),

    // accounting_rule queries
    @NamedQuery(
        name = "Rate.findByAccountingRule",
        query = "SELECT r FROM Rate r WHERE r.accountingRule = :accounting_rule"),
    @NamedQuery(
        name = "Rate.findByEffectiveDateAndAccountingRule",
        query = "SELECT r FROM Rate r WHERE r.effectiveDate = :effectiveDate AND r.accountingRule = :ar"),
    @NamedQuery(
        name = "Rate.findByEffectiveDateBeforeAndAccountingRule",
        query = "SELECT r FROM Rate r WHERE r.effectiveDate < :effectiveDate AND r.accountingRule = :ar"),
    @NamedQuery(
        name = "Rate.findByEffectiveDateAfterAndAccountingRule",
        query = "SELECT r FROM Rate r WHERE r.effectiveDate >= :effectiveDate AND r.accountingRule = :ar"),

    // category queries
    @NamedQuery(
        name = "Rate.findByCategory",
        query = "SELECT r FROM Rate r WHERE r.category = :category"),
    @NamedQuery(
        name = "Rate.findByCategoryId",
        query = "SELECT r FROM Rate r WHERE r.category.id = :categoryId ORDER BY r.effectiveDate DESC"),
    @NamedQuery(
        name = "Rate.countByCategoryId",
        query = "SELECT count(r.id) FROM Rate r WHERE r.category.id = :categoryId"),
    @NamedQuery(
        name = "Rate.findByEffectiveDateAndCategory",
        query = "SELECT r FROM Rate r WHERE r.effectiveDate = :effectiveDate AND r.category = :category"),
    @NamedQuery(
        name = "Rate.findByEffectiveDateBeforeAndCategory",
        query = "SELECT r FROM Rate r WHERE r.effectiveDate < :effectiveDate AND r.category = :category"),
    @NamedQuery(
        name = "Rate.findByEffectiveDateAfterAndCategory",
        query = "SELECT r FROM Rate r WHERE r.effectiveDate >= :effectiveDate AND r.category = :category"),

    // mixed queries
    @NamedQuery(
        name = "Rate.findByCategoryAndAccountingRule",
        query = "SELECT r FROM Rate r WHERE r.category = :category AND r.accountingRule = :ar"),
    @NamedQuery(
        name = "Rate.findByEffectiveDateAndCategoryAndAccountingRule",
        query = """
            SELECT r FROM Rate r
            WHERE r.effectiveDate = :effectiveDate
            AND r.category = :category
            AND r.accountingRule = :accounting_rule"""),
    @NamedQuery(
        name = "Rate.findByEffectiveDateBeforeAndCategoryAndAccountingRule",
        query = """
            SELECT r FROM Rate r
            WHERE r.effectiveDate < :effectiveDate
            AND r.category = :category
            AND r.accountingRule = :accounting_rule"""),
    @NamedQuery(
        name = "Rate.findByEffectiveDateAfterAndCategoryAndAccountingRule",
        query = """
            SELECT r FROM Rate r
            WHERE r.effectiveDate >= :effectiveDate
            AND r.category = :category
            AND r.accountingRule = :accounting_rule"""),

    // only date queries
    @NamedQuery(
        name = "Rate.findByEffectiveDate",
        query = "SELECT r FROM Rate r WHERE r.effectiveDate = :effectiveDate"),
    @NamedQuery(
        name = "Rate.findByEffectiveDateBefore",
        query = "SELECT r FROM Rate r WHERE r.effectiveDate < :effectiveDate"),
    @NamedQuery(
        name = "Rate.findByEffectiveDateAfter",
        query = "SELECT r FROM Rate r WHERE r.effectiveDate >= :effectiveDate"),
})
@EntityListeners({EntityControlListenerMOW.class})
public class Rate extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @PositiveOrZero
    @NotNull
    @Basic(optional = false)
    @Column(name = "value", nullable = false, scale = 2, precision = 38)
    @Getter
    @Setter
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Basic(optional = false)
    @Column(name = "accounting_rule", nullable = false)
    @Getter
    @Setter
    private AccountingRule accountingRule;

    @NotNull
    @Future
    @Basic(optional = false)
    @Column(name = "effective_date", nullable = false)
    @Getter
    @Setter
    private LocalDate effectiveDate;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", referencedColumnName = "id", updatable = false, nullable = false)
    @Getter
    @Setter
    private Category category;

    public Rate(BigDecimal value, AccountingRule accountingRule, LocalDate effectiveDate) {
        this.value = value;
        this.accountingRule = accountingRule;
        this.effectiveDate = effectiveDate;
    }
}
