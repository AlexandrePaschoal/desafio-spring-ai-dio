package dio.budgeting.application.input;

import dio.budgeting.domain.TransactionType;
import org.springframework.ai.tool.annotation.ToolParam;

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
        TransactionType type

) {
}