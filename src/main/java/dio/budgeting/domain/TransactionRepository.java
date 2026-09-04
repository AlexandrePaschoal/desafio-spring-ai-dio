package dio.budgeting.domain;

import java.util.List;
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
}