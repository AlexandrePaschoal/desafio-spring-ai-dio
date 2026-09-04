package dio.budgeting.application.input;

import org.springframework.ai.tool.annotation.ToolParam;

import java.util.UUID;

public record PersistTransactionInput(

        @ToolParam(
                description = "Descrição do gasto"
        )
        String description,

        @ToolParam(
                description = "Valor do gasto (em centavos)"
        )
        long amount,

        @ToolParam(
                description = "ID da categoria da transação"
        )
        UUID categoryId

) {
}