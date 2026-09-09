package dio.budgeting.application.input;

import org.springframework.ai.tool.annotation.ToolParam;

public record PersistAiTransactionInput(

        @ToolParam(
                description = "Descrição do gasto"
        )
        String description,

        @ToolParam(
                description = "Valor do gasto em centavos"
        )
        long amount,

        @ToolParam(
                description = "Nome exato da categoria existente do usuário"
        )
        String categoryName

) {
}