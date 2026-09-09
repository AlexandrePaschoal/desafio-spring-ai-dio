package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.TransactionStatus;
import dio.budgeting.domain.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(

        @NotBlank(
                message = "A descrição é obrigatória."
        )
        @Size(
                max = 255,
                message = "A descrição deve possuir no máximo 255 caracteres."
        )
        String description,

        @NotNull(
                message = "A categoria é obrigatória."
        )
        UUID categoryId,

        @Positive(
                message = "O valor da transação deve ser maior que zero."
        )
        long amount,

        TransactionType type,

        LocalDate date,

        TransactionStatus status

) {

    public PersistTransactionInput toInput() {

        TransactionType transactionType =
                type != null
                        ? type
                        : TransactionType.EXPENSE;

        LocalDate transactionDate =
                date != null
                        ? date
                        : LocalDate.now();

        TransactionStatus transactionStatus =
                status != null
                        ? status
                        : TransactionStatus.COMPLETED;

        return new PersistTransactionInput(
                description.trim(),
                amount,
                categoryId,
                transactionType,
                transactionDate,
                transactionStatus
        );
    }
}