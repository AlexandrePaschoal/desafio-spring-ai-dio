package dio.budgeting.application;

import dio.budgeting.application.input.PersistAiTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.security.CurrentUserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class PersistAiTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final CategoryService categoryService;

    public PersistAiTransactionUseCase(
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService,
            CategoryService categoryService
    ) {
        this.transactionRepository =
                transactionRepository;

        this.currentUserService =
                currentUserService;

        this.categoryService =
                categoryService;
    }

    @Tool(
            name = "persist-ai-transaction",
            description = """
                    Persiste uma nova transação financeira.
                    A categoria deve ser informada pelo nome
                    e precisa existir entre as categorias do usuário.
                    """
    )
    public TransactionOutput execute(
            PersistAiTransactionInput input
    ) {

        var userId =
                currentUserService
                        .getCurrentUserId();

        var category =
                categoryService
                        .findByNameForCurrentUser(
                                input.categoryName()
                        );

        var transaction =
                transactionRepository.save(
                        new Transaction(
                                input.description(),
                                input.amount(),
                                category.getId(),
                                category.getName()
                        ),
                        userId
                );

        return TransactionOutput.from(
                transaction
        );
    }
}