package dio.budgeting.infrastructure.persistence.entity;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.TransactionStatus;
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

import java.time.LocalDate;
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

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

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
                transaction.getDate(),
                transaction.getStatus(),
                category,
                user
        );
    }

    public Transaction toDomain() {

        TransactionType transactionType =
                this.type != null
                        ? this.type
                        : TransactionType.EXPENSE;

        LocalDate transactionDate =
                this.date != null
                        ? this.date
                        : LocalDate.now();

        TransactionStatus transactionStatus =
                this.status != null
                        ? this.status
                        : TransactionStatus.COMPLETED;

        return new Transaction(
                new TransactionId(this.id),
                this.description,
                this.amount,
                transactionType,
                transactionDate,
                transactionStatus,
                this.category.getId(),
                this.category.getName()
        );
    }
}