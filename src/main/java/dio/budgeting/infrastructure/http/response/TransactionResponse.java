package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.TransactionStatus;
import dio.budgeting.domain.TransactionType;

import java.time.LocalDate;

public record TransactionResponse(
        String id,
        String category,
        String description,
        TransactionType type,
        LocalDate date,
        TransactionStatus status,
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
                output.date(),
                output.status(),
                output.value()
        );
    }
}