package dio.budgeting.infrastructure.persistence.entity;

import dio.budgeting.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "category",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_name_user",
                        columnNames = {
                                "name",
                                "user_id"
                        }
                )
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntity {

    @Id
    private UUID id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public CategoryEntity(
            String name,
            User user
    ) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.user = user;
    }
}