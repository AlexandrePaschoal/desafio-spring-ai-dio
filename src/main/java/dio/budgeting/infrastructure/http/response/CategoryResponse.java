package dio.budgeting.infrastructure.http.response;

import dio.budgeting.infrastructure.persistence.entity.CategoryEntity;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name
) {

    public static CategoryResponse from(
            CategoryEntity category
    ) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}