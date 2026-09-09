package dio.budgeting.application;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUserCategoriesUseCase {

    private final CategoryService categoryService;

    public ListUserCategoriesUseCase(
            CategoryService categoryService
    ) {
        this.categoryService =
                categoryService;
    }

    @Tool(
            name = "list-user-categories",
            description = """
                    Lista os nomes das categorias financeiras
                    disponíveis para o usuário autenticado.
                    Use esta ferramenta antes de registrar
                    ou consultar uma transação quando precisar
                    identificar a categoria correta.
                    """
    )
    public List<String> execute() {

        return categoryService
                .findAllForCurrentUser()
                .stream()
                .map(category ->
                        category.getName()
                )
                .toList();
    }
}