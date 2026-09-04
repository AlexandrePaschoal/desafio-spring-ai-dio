package dio.budgeting.domain.user;

import dio.budgeting.application.CategoryService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CategoryService categoryService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryService = categoryService;
    }

    @Transactional
    public User register(RegisterUserRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Dados de cadastro inválidos."
            );
        }

        String name = request.name() == null
                ? ""
                : request.name().trim();

        String email = request.email() == null
                ? ""
                : request.email()
                .trim()
                .toLowerCase();

        String password = request.password();

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome é obrigatório."
            );
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "O nome é muito longo."
            );
        }

        if (email.isBlank()) {
            throw new IllegalArgumentException(
                    "O e-mail é obrigatório."
            );
        }

        if (email.length() > 150) {
            throw new IllegalArgumentException(
                    "O e-mail é muito longo."
            );
        }

        if (
                !email.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                )
        ) {
            throw new IllegalArgumentException(
                    "O e-mail informado é inválido."
            );
        }

        if (
                password == null ||
                        password.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "A senha é obrigatória."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "A senha deve possuir pelo menos 8 caracteres."
            );
        }

        if (password.length() > 72) {
            throw new IllegalArgumentException(
                    "A senha deve possuir no máximo 72 caracteres."
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Não foi possível realizar o cadastro com os dados informados."
            );
        }

        String encodedPassword =
                passwordEncoder.encode(password);

        User user = new User(
                name,
                email,
                encodedPassword
        );

        User savedUser =
                userRepository.save(user);

        categoryService.createDefaultCategories(
                savedUser
        );

        return savedUser;
    }

    public User authenticate(LoginRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Credenciais inválidas."
            );
        }

        String email = request.email() == null
                ? ""
                : request.email()
                .trim()
                .toLowerCase();

        String password = request.password();

        if (
                email.isBlank() ||
                        password == null ||
                        password.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Credenciais inválidas."
            );
        }

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Credenciais inválidas."
                        )
                );

        boolean passwordMatches =
                passwordEncoder.matches(
                        password,
                        user.getPassword()
                );

        if (!passwordMatches) {
            throw new IllegalArgumentException(
                    "Credenciais inválidas."
            );
        }

        return user;
    }
}