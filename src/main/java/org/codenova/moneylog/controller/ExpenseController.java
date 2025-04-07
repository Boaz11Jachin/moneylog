package org.codenova.moneylog.controller;

import lombok.AllArgsConstructor;
import org.codenova.moneylog.entity.Expense;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.query.DailyExpense;
import org.codenova.moneylog.query.ExpenseWithCategory;
import org.codenova.moneylog.repository.CategoryRepository;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.codenova.moneylog.request.SearchPeriodRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@AllArgsConstructor
@RequestMapping("/expense")
public class ExpenseController {


    private CategoryRepository categoryRepository;
    private ExpenseRepository expenseRepository;

    @GetMapping("/history")
    public String historyHandle(Model model,
                                @SessionAttribute("user") User user,
                                @ModelAttribute SearchPeriodRequest searchPeriodRequest) {

        model.addAttribute("categorys", categoryRepository.findAll());
        model.addAttribute("now", LocalDate.now());


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


        //model.addAttribute("expenses", expenseRepository.findWithCategoryByUserId(user.getId()));
        //model.addAttribute("expenses2", expenseRepository.findByUserIdAndDuration(user.getId(), LocalDate.now().minusDays(10), LocalDate.now()));

        LocalDate today = LocalDate.now();

        LocalDate startDate;
        LocalDate endDate;
        if (searchPeriodRequest.getStartDate() != null && searchPeriodRequest.getEndDate() != null) {
            startDate = searchPeriodRequest.getStartDate();
            endDate = searchPeriodRequest.getEndDate();
        } else {
            today = LocalDate.now();
            startDate = today.minusDays(today.getDayOfMonth() - 1);
            endDate = startDate.plusMonths(1).minusDays(1);
        }


        model.addAttribute("expenses", expenseRepository.findByUserIdAndDuration(user.getId(), startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);


        return "expense/history";
    }

    @PostMapping("/history")
    public String historyPostHandle(@ModelAttribute Expense expense,
                                    @SessionAttribute("user") User user) {

        Expense exp = Expense.builder().userId(user.getId())
                .expenseDate(expense.getExpenseDate())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .categoryId(expense.getCategoryId()).build();

        expenseRepository.save(exp);

        return "redirect:/expense/history";
    }

    @GetMapping("/report")
    public String reportHandle(Model model, @SessionAttribute("user") User user) {


        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(today.getDayOfMonth() - 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        model.addAttribute("categoryExpense", expenseRepository.getCategoryExpenseByPeriodOrderByCategoryId(
                user.getId(), startDate, endDate));

        // --------------------
        List<DailyExpense> list = expenseRepository.getDailyExpenseByUserIdAndPeriod(user.getId(), startDate, endDate);
        Map<LocalDate, DailyExpense> listMap = new HashMap<>();
        for(DailyExpense expense : list) {
            //    System.out.println(expense);
            listMap.put(expense.getExpenseDate(), expense);
        }

        // System.out.println(listMap);

        List<DailyExpense> fullList = new ArrayList<>();
        for(int i = 0; startDate.plusDays(i).isBefore(endDate) || startDate.plusDays(i).isEqual(endDate); i++ ) {
            LocalDate d = startDate.plusDays(i);
            // if 만약 이 날짜에 해당하는 데이터를 불러왓따면, 그때는 그걸 add
            if(listMap.get(d) != null) {
                fullList.add(listMap.get(d));
            }else {
                // else 없다면 이날짜로 데이터를 생성해서 add
                fullList.add(DailyExpense.builder().expenseDate(d).total(0).build());
            }
        }


        model.addAttribute("dailyExpense", fullList);

        return "expense/report";
    }


}
