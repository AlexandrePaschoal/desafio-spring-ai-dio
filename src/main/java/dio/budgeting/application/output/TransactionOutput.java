package dio.budgeting.application.output;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionStatus;
import dio.budgeting.domain.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record TransactionOutput(
        String id,
        String description,
        String category,
        TransactionType type,
        LocalDate date,
        TransactionStatus status,
        double value
) {

    public static TransactionOutput from(
            Transaction transaction
    ) {

        return new TransactionOutput(
                transaction.getId()
                        .uuid()
                        .toString(),

                transaction.getDescription(),

                transaction.getCategoryName(),

                transaction.getType(),

                transaction.getDate(),

                transaction.getStatus(),

                BigDecimal
                        .valueOf(
                                transaction.getAmount()
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        )
                        .doubleValue()
        );
    }
}