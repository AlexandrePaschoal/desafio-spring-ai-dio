package dio.budgeting.application.input;

import dio.budgeting.domain.TransactionStatus;
import dio.budgeting.domain.TransactionType;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;

public record PersistAiTransactionInput(

        @ToolParam(
                description = "Descrição da transação financeira"
        )
        String description,

        @ToolParam(
                description = "Valor da transação em centavos"
        )
        long amount,

        @ToolParam(
                description = "Nome exato da categoria existente do usuário"
        )
        String categoryName,

        @ToolParam(
                description = "Tipo da transação: EXPENSE para despesa ou INCOME para receita"
        )
        TransactionType type,

        @ToolParam(
                description = "Data da movimentação ou vencimento no formato YYYY-MM-DD"
        )
        LocalDate date,

        @ToolParam(
                description = "Status da transação: PENDING para pendente ou COMPLETED para pago/recebido"
        )
        TransactionStatus status

) {
}