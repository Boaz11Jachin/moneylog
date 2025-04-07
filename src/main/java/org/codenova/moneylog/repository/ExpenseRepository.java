package org.codenova.moneylog.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.codenova.moneylog.entity.Expense;
import org.codenova.moneylog.query.CategoryExpense;
import org.codenova.moneylog.query.DailyExpense;
import org.codenova.moneylog.query.ExpenseWithCategory;


import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ExpenseRepository {

    public int save (Expense expense);
    public List<Expense> findByUserId (int userId);
    public List<ExpenseWithCategory> findByUserIdAndDuration(@Param("userId") int userId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    public List<ExpenseWithCategory> findWithCategoryByUserId (int userId);

    public long getTotalAmountByPeriod (@Param("userId") int userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    public List<ExpenseWithCategory> getTop3ExpensesByPeriod (@Param("userId") int userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    public List<CategoryExpense> getCategoryExpenseByPeriod (@Param("userId") int userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    public List<CategoryExpense> getCategoryExpenseByPeriodOrderByCategoryId (@Param("userId") int userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    public List<DailyExpense> getDailyExpenseByUserIdAndPeriod (@Param("userId") int userId,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate);

}
