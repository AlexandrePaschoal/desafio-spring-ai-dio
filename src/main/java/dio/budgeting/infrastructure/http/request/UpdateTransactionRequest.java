package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.UpdateTransactionInput;
import dio.budgeting.domain.TransactionStatus;
import dio.budgeting.domain.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionRequest(

        @NotBlank(
                message = "A descrição é obrigatória."
        )
        @Size(
                max = 255,
                message = "A descrição deve possuir no máximo 255 caracteres."
        )
        String description,

        @Positive(
                message = "O valor da transação deve ser maior que zero."
        )
        long amount,

        @NotNull(
                message = "A categoria é obrigatória."
        )
        UUID categoryId,

        @NotNull(
                message = "O tipo da transação é obrigatório."
        )
        TransactionType type,

        @NotNull(
                message = "A data da transação é obrigatória."
        )
        LocalDate date,

        @NotNull(
                message = "O status da transação é obrigatório."
        )
        TransactionStatus status

) {

    public UpdateTransactionInput toInput() {
        return new UpdateTransactionInput(
                description.trim(),
                amount,
                categoryId,
                type,
                date,
                status
        );
    }
}