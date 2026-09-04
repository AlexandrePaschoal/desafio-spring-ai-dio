package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.security.CurrentUserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListTransactionsByCategoryUseCase {

    private final TransactionRepository
            transactionRepository;

    private final CurrentUserService
            currentUserService;

    public ListTransactionsByCategoryUseCase(
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ) {

        this.transactionRepository =
                transactionRepository;

        this.currentUserService =
                currentUserService;
    }

    @Tool(
            name = "list-transactions-by-category",
            description = "Lista transações financeiras por categoria"
    )
    public List<TransactionOutput> execute(

            @ToolParam(
                    description = "ID da categoria"
            )
            UUID categoryId
    ) {

        var userId =
                currentUserService.getCurrentUserId();

        return transactionRepository
                .findAllByCategoryIdAndUserId(
                        categoryId,
                        userId
                )
                .stream()
                .map(TransactionOutput::from)
                .toList();
    }
}