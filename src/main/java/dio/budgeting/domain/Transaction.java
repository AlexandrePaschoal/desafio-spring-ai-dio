package dio.budgeting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Transaction {

    private TransactionId id;
    private String description;
    private long amount;
    private TransactionType type;
    private LocalDate date;
    private TransactionStatus status;
    private UUID categoryId;
    private String categoryName;

    public Transaction(
            String description,
            long amount,
            TransactionType type,
            LocalDate date,
            TransactionStatus status,
            UUID categoryId,
            String categoryName
    ) {
        this.id = new TransactionId();
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.status = status;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
}