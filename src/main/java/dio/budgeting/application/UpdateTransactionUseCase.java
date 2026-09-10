package dio.budgeting.application;

import dio.budgeting.application.input.UpdateTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public UpdateTransactionUseCase(
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ) {
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    public TransactionOutput execute(
            UUID transactionId,
            UpdateTransactionInput input
    ) {

        if (transactionId == null) {
            throw new IllegalArgumentException(
                    "O ID da transação é obrigatório."
            );
        }

        if (input == null) {
            throw new IllegalArgumentException(
                    "Os dados da transação são obrigatórios."
            );
        }

        UUID userId =
                currentUserService.getCurrentUserId();

        Transaction existingTransaction =
                transactionRepository
                        .findByIdAndUserId(
                                transactionId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transação não encontrada."
                                )
                        );

        Transaction updatedTransaction =
                new Transaction(
                        existingTransaction.getId(),
                        input.description(),
                        input.amount(),
                        input.type(),
                        input.date(),
                        input.status(),
                        input.categoryId(),
                        existingTransaction.getCategoryName()
                );

        Transaction savedTransaction =
                transactionRepository.save(
                        updatedTransaction,
                        userId
                );

        return TransactionOutput.from(
                savedTransaction
        );
    }
}