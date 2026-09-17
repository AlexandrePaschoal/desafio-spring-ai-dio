package dio.budgeting.infrastructure.web;

import dio.budgeting.domain.user.UpdateInitialBalanceRequest;
import dio.budgeting.domain.user.User;
import dio.budgeting.domain.user.UserService;
import dio.budgeting.infrastructure.security.CurrentUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(
            UserService userService,
            CurrentUserService currentUserService
    ) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    // =========================
    // SALDO INICIAL
    // =========================

    @PatchMapping("/me/initial-balance")
    public ResponseEntity<?> updateInitialBalance(
            @RequestBody UpdateInitialBalanceRequest request
    ) {

        try {

            UUID userId =
                    currentUserService.getCurrentUserId();

            User user =
                    userService.updateInitialBalance(
                            userId,
                            request
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "initialBalance",
                            user.getInitialBalance(),

                            "initialBalanceDate",
                            user.getInitialBalanceDate()
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}