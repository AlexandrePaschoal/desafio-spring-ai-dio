package dio.budgeting.domain.user;

public record RegisterUserRequest(
        String name,
        String email,
        String password
) {
}