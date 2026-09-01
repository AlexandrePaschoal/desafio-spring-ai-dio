package dio.budgeting.infrastructure.web;

import dio.budgeting.domain.user.LoginRequest;
import dio.budgeting.domain.user.RegisterUserRequest;
import dio.budgeting.domain.user.User;
import dio.budgeting.domain.user.UserService;
import dio.budgeting.infrastructure.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterUserRequest request
    ) {

        try {

            User user =
                    userService.register(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            Map.of(
                                    "id",
                                    user.getId(),

                                    "name",
                                    user.getName(),

                                    "email",
                                    user.getEmail()
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

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {

        try {

            User user =
                    userService.authenticate(request);

            String token =
                    jwtService.generateToken(user);

            return ResponseEntity.ok(
                    Map.of(
                            "token",
                            token,

                            "user",
                            Map.of(
                                    "id",
                                    user.getId(),

                                    "name",
                                    user.getName(),

                                    "email",
                                    user.getEmail()
                            )
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Credenciais inválidas."
                            )
                    );
        }
    }
}