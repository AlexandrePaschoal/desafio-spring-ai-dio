package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.security.CurrentUserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListTransactionsByCategoryUseCase {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public ListTransactionsByCategoryUseCase(
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ) {
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Tool(
            name = "list-transactions-by-category",
            description = "Lista transações financeiras por categoria"
    )
    public List<TransactionOutput> execute(
            @ToolParam(description = "Categoria de uma transação")
            Category category
    ) {
        var userId = currentUserService.getCurrentUserId();

        return transactionRepository
                .findAllByCategoryAndUserId(category, userId)
                .stream()
                .map(TransactionOutput::from)
                .toList();
    }
}