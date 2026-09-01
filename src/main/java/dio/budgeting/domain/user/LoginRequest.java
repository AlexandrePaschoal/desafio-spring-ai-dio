package dio.budgeting.domain.user;

public record LoginRequest(
        String email,
        String password
) {
}