package dio.budgeting.domain.user;

import java.time.LocalDate;

public record UpdateInitialBalanceRequest(
        Long initialBalance,
        LocalDate initialBalanceDate
) {
}