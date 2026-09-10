package dio.budgeting.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(
            Transaction transaction,
            UUID userId
    );

    List<Transaction> findAllByCategoryIdAndUserId(
            UUID categoryId,
            UUID userId
    );

    Optional<Transaction> findByIdAndUserId(
            UUID transactionId,
            UUID userId
    );

    void deleteByIdAndUserId(
            UUID transactionId,
            UUID userId
    );
}