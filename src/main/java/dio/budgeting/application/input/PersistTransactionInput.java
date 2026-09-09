package dio.budgeting.application.input;

import dio.budgeting.domain.TransactionType;

import java.util.UUID;

public record PersistTransactionInput(
        String description,
        long amount,
        UUID categoryId,
        TransactionType type
) {
}