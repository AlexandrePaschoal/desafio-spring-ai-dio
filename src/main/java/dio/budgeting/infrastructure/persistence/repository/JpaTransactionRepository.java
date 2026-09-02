package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.user.User;
import dio.budgeting.domain.user.UserRepository;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionEntityRepository transactionEntityRepository;
    private final UserRepository userRepository;

    public JpaTransactionRepository(
            TransactionEntityRepository transactionEntityRepository,
            UserRepository userRepository
    ) {
        this.transactionEntityRepository = transactionEntityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Transaction save(
            Transaction transaction,
            UUID userId
    ) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        )
                );

        TransactionEntity entity =
                TransactionEntity.from(transaction, user);

        return transactionEntityRepository
                .save(entity)
                .toDomain();
    }

    @Override
    public List<Transaction> findAllByCategoryAndUserId(
            Category category,
            UUID userId
    ) {
        return transactionEntityRepository
                .findAllByCategoryAndUserId(category, userId)
                .stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }
}