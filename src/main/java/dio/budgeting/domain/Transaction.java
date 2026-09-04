package dio.budgeting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Transaction {

    private TransactionId id;
    private String description;
    private long amount;
    private UUID categoryId;
    private String categoryName;

    public Transaction(
            String description,
            long amount,
            UUID categoryId,
            String categoryName
    ) {
        this.id = new TransactionId();
        this.description = description;
        this.amount = amount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
}