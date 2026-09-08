package dio.budgeting.application;

import dio.budgeting.domain.user.User;
import dio.budgeting.domain.user.UserRepository;
import dio.budgeting.infrastructure.persistence.entity.CategoryEntity;
import dio.budgeting.infrastructure.persistence.repository.CategoryEntityRepository;
import dio.budgeting.infrastructure.persistence.repository.TransactionEntityRepository;
import dio.budgeting.infrastructure.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private static final String UNCATEGORIZED_NAME =
            "Sem categoria";

    private static final List<String> DEFAULT_CATEGORIES =
            List.of(
                    UNCATEGORIZED_NAME,
                    "Supermercado",
                    "Farmácia",
                    "Automóvel"
            );

    private final CategoryEntityRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final TransactionEntityRepository transactionRepository;

    public CategoryService(
            CategoryEntityRepository categoryRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            TransactionEntityRepository transactionRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.transactionRepository = transactionRepository;
    }

    public void createDefaultCategories(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Usuário inválido."
            );
        }

        for (String categoryName : DEFAULT_CATEGORIES) {

            boolean alreadyExists =
                    categoryRepository
                            .existsByNameIgnoreCaseAndUserId(
                                    categoryName,
                                    user.getId()
                            );

            if (!alreadyExists) {

                CategoryEntity category =
                        new CategoryEntity(
                                categoryName,
                                user
                        );

                categoryRepository.save(category);
            }
        }
    }

    public List<CategoryEntity> findAllForCurrentUser() {

        UUID userId =
                currentUserService.getCurrentUserId();

        return categoryRepository
                .findAllByUserId(userId);
    }

    @Transactional
    public CategoryEntity createForCurrentUser(
            String name
    ) {

        UUID userId =
                currentUserService.getCurrentUserId();

        String normalizedName =
                name == null
                        ? ""
                        : name.trim();

        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da categoria é obrigatório."
            );
        }

        boolean alreadyExists =
                categoryRepository
                        .existsByNameIgnoreCaseAndUserId(
                                normalizedName,
                                userId
                        );

        if (alreadyExists) {
            throw new IllegalArgumentException(
                    "Já existe uma categoria com esse nome."
            );
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Usuário não encontrado."
                                )
                        );

        CategoryEntity category =
                new CategoryEntity(
                        normalizedName,
                        user
                );

        return categoryRepository.save(category);
    }

    @Transactional
    public CategoryEntity getOrCreateUncategorized(
            User user
    ) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Usuário inválido."
            );
        }

        return categoryRepository
                .findByNameIgnoreCaseAndUserId(
                        UNCATEGORIZED_NAME,
                        user.getId()
                )
                .orElseGet(() ->
                        categoryRepository.save(
                                new CategoryEntity(
                                        UNCATEGORIZED_NAME,
                                        user
                                )
                        )
                );
    }

    @Transactional
    public void deleteForCurrentUser(
            UUID categoryId
    ) {

        UUID userId =
                currentUserService.getCurrentUserId();

        CategoryEntity category =
                categoryRepository
                        .findByIdAndUserId(
                                categoryId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Categoria não encontrada."
                                )
                        );

        if (
                category.getName()
                        .equalsIgnoreCase(
                                UNCATEGORIZED_NAME
                        )
        ) {
            throw new IllegalArgumentException(
                    "A categoria 'Sem categoria' não pode ser excluída."
            );
        }

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Usuário não encontrado."
                                )
                        );

        CategoryEntity uncategorized =
                getOrCreateUncategorized(user);

        var transactions =
                transactionRepository
                        .findAllByCategoryIdAndUserId(
                                categoryId,
                                userId
                        );

        for (var transaction : transactions) {
            transaction.setCategory(
                    uncategorized
            );
        }

        transactionRepository.saveAll(
                transactions
        );

        categoryRepository.delete(
                category
        );
    }
}