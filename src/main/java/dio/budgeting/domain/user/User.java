package dio.budgeting.domain.user;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =========================
    // SALDO INICIAL
    // =========================

    @Column(nullable = false)
    private Long initialBalance;

    @Column(nullable = false)
    private LocalDate initialBalanceDate;

    // =========================
    // CONSTRUTOR JPA
    // =========================

    protected User() {
    }

    // =========================
    // CONSTRUTOR
    // =========================

    public User(
            String name,
            String email,
            String password
    ) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();

        this.initialBalance = 0L;
        this.initialBalanceDate = LocalDate.now();
    }

    // =========================
    // GETTERS
    // =========================

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getInitialBalance() {
        return initialBalance;
    }

    public LocalDate getInitialBalanceDate() {
        return initialBalanceDate;
    }

    // =========================
    // ATUALIZAR SALDO INICIAL
    // =========================

    public void updateInitialBalance(
            Long initialBalance,
            LocalDate initialBalanceDate
    ) {
        if (initialBalance == null) {
            throw new IllegalArgumentException(
                    "O saldo inicial é obrigatório."
            );
        }

        if (initialBalanceDate == null) {
            throw new IllegalArgumentException(
                    "A data de referência é obrigatória."
            );
        }

        this.initialBalance = initialBalance;
        this.initialBalanceDate = initialBalanceDate;
    }
}