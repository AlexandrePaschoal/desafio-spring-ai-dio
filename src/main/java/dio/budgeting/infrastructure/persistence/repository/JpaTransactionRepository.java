package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.user.User;
import dio.budgeting.domain.user.UserRepository;
import dio.budgeting.infrastructure.persistence.entity.CategoryEntity;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaTransactionRepository
        implements TransactionRepository {

    private final TransactionEntityRepository
            transactionEntityRepository;

    private final UserRepository
            userRepository;

    private final CategoryEntityRepository
            categoryEntityRepository;

    public JpaTransactionRepository(
            TransactionEntityRepository transactionEntityRepository,
            UserRepository userRepository,
            CategoryEntityRepository categoryEntityRepository
    ) {

        this.transactionEntityRepository =
                transactionEntityRepository;

        this.userRepository =
                userRepository;

        this.categoryEntityRepository =
                categoryEntityRepository;
    }

    @Override
    public Transaction save(
            Transaction transaction,
            UUID userId
    ) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Usuário não encontrado."
                                )
                        );

        CategoryEntity category =
                categoryEntityRepository
                        .findByIdAndUserId(
                                transaction.getCategoryId(),
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Categoria inválida."
                                )
                        );

        TransactionEntity entity =
                TransactionEntity.from(
                        transaction,
                        user,
                        category
                );

        return transactionEntityRepository
                .save(entity)
                .toDomain();
    }

    @Override
    public List<Transaction>
    findAllByCategoryIdAndUserId(
            UUID categoryId,
            UUID userId
    ) {

        return transactionEntityRepository
                .findAllByCategoryIdAndUserId(
                        categoryId,
                        userId
                )
                .stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }
}