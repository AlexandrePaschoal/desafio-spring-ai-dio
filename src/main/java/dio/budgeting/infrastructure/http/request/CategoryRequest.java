package dio.budgeting.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(
                message = "O nome da categoria é obrigatório."
        )
        @Size(
                max = 50,
                message = "O nome da categoria deve possuir no máximo 50 caracteres."
        )
        String name

) {
}