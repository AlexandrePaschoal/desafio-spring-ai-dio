package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryEntityRepository
        extends JpaRepository<CategoryEntity, UUID> {

    List<CategoryEntity> findAllByUserId(
            UUID userId
    );

    Optional<CategoryEntity>
    findByNameIgnoreCaseAndUserId(
            String name,
            UUID userId
    );

    boolean existsByNameIgnoreCaseAndUserId(
            String name,
            UUID userId
    );
}