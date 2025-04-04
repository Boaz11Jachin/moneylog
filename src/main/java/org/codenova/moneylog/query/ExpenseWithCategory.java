package org.codenova.moneylog.query;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
public class ExpenseWithCategory {
    private long id;
    private int userId;
    private LocalDate expenseDate;
    private String description;
    private long amount;

    private int categoryId;

    private String categoryName;

}
