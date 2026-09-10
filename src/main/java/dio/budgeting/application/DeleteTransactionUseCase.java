package dio.budgeting.application;

import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public DeleteTransactionUseCase(
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ) {
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    public void execute(UUID transactionId) {

        if (transactionId == null) {
            throw new IllegalArgumentException(
                    "O ID da transação é obrigatório."
            );
        }

        UUID userId =
                currentUserService.getCurrentUserId();

        transactionRepository.deleteByIdAndUserId(
                transactionId,
                userId
        );
    }
}