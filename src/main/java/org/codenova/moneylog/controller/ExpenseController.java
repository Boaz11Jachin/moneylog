package org.codenova.moneylog.controller;

import lombok.AllArgsConstructor;
import org.codenova.moneylog.entity.Expense;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.query.ExpenseWithCategory;
import org.codenova.moneylog.repository.CategoryRepository;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Controller
@AllArgsConstructor
@RequestMapping("/expense")
public class ExpenseController {

    private CategoryRepository categoryRepository;
    private ExpenseRepository expenseRepository;

    @GetMapping("/history")
    public String historyHandle(Model model,
                                @SessionAttribute("user") User user) {

        model.addAttribute("categorys", categoryRepository.findAll());
        model.addAttribute("now" , LocalDate.now());


//        List<Expense> expenses = expenseRepository.findByUserId(user.getId());
//        List<ExpenseWithCategory> expenseWithCategories = new ArrayList<>();
//
//        for (Expense expense : expenses) {
//
//            ExpenseWithCategory ewc = ExpenseWithCategory.builder().userId(user.getId())
//                    .expenseDate(expense.getExpenseDate())
//                    .description(expense.getDescription())
//                    .amount(expense.getAmount())
//                    .categoryId(expense.getCategoryId())
//                    .categoryName(categoryRepository.findById(expense.getCategoryId()).getName())
//                    .categorySort(categoryRepository.findById(expense.getCategoryId()).getSort())
//                    .build();
//            expenseWithCategories.add(ewc);
//        }
//
//        model.addAttribute("expenseWithCategories", expenseWithCategories);


        model.addAttribute("expenses", expenseRepository.findWithCategoryByUserId(user.getId()));
        model.addAttribute("expenses2", expenseRepository.findByUserIdAndDuration(user.getId(), LocalDate.now().minusDays(10), LocalDate.now()));

        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.minusDays(today.getDayOfMonth()-1);
        LocalDate lastDay = today.plusMonths(1).minusDays(today.getDayOfMonth());



        return "expense/history";
    }

    @PostMapping("/history")
    public String historyPostHandle(@ModelAttribute Expense expense,
                                    @SessionAttribute("user") User user){

        Expense exp = Expense.builder().userId(user.getId())
                .expenseDate(expense.getExpenseDate())
                        .description(expense.getDescription())
                                .amount(expense.getAmount())
                                        .categoryId(expense.getCategoryId()).build();

        expenseRepository.save(exp);

        return "redirect:/expense/history";
    }




}
