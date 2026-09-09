package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.security.CurrentUserService;
import org.springframework.stereotype.Service;

@Service
public class PersistTransactionUseCase {

    private final TransactionRepository
            transactionRepository;

    private final CurrentUserService
            currentUserService;

    public PersistTransactionUseCase(
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ) {

        this.transactionRepository =
                transactionRepository;

        this.currentUserService =
                currentUserService;
    }

    public TransactionOutput execute(
            PersistTransactionInput input
    ) {

        var userId =
                currentUserService.getCurrentUserId();

        var transaction =
                transactionRepository.save(
                        new Transaction(
                                input.description(),
                                input.amount(),
                                input.categoryId(),
                                null
                        ),
                        userId
                );

        return TransactionOutput.from(
                transaction
        );
    }
}