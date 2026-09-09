package dio.budgeting.infrastructure.persistence.entity;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionType;
import dio.budgeting.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEntity {

    @Id
    private UUID id;

    private String description;

    private long amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static TransactionEntity from(
            Transaction transaction,
            User user,
            CategoryEntity category
    ) {

        return new TransactionEntity(
                transaction.getId().uuid(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                category,
                user
        );
    }

    public Transaction toDomain() {

        TransactionType transactionType =
                this.type != null
                        ? this.type
                        : TransactionType.EXPENSE;

        return new Transaction(
                new TransactionId(this.id),
                this.description,
                this.amount,
                transactionType,
                this.category.getId(),
                this.category.getName()
        );
    }
}