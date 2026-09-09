package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.TransactionType;

public record TransactionResponse(
        String id,
        String category,
        String description,
        TransactionType type,
        double amount
) {

    public static TransactionResponse from(
            TransactionOutput output
    ) {

        return new TransactionResponse(
                output.id(),
                output.category(),
                output.description(),
                output.type(),
                output.value()
        );
    }
}