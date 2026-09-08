package dio.budgeting.infrastructure.http;

import dio.budgeting.application.CategoryService;
import dio.budgeting.infrastructure.http.request.CategoryRequest;
import dio.budgeting.infrastructure.http.response.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> findAll() {

        return categoryService
                .findAllForCurrentUser()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(
            @Valid @RequestBody CategoryRequest request
    ) {

        var category =
                categoryService.createForCurrentUser(
                        request.name()
                );

        return CategoryResponse.from(category);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {

        categoryService.deleteForCurrentUser(id);
    }
}