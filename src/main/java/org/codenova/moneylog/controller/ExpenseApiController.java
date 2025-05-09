package org.codenova.moneylog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.query.CategoryExpense;
import org.codenova.moneylog.query.DailyExpense;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.codenova.moneylog.response.ChartDataResponse;
import org.codenova.moneylog.response.DailyChartDataResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/expense")
@AllArgsConstructor
public class ExpenseApiController {

    private ExpenseRepository expenseRepository;
    private ObjectMapper objectMapper;

    @GetMapping("/next-month")
    @ResponseBody
    public String nextMonth(@RequestParam("date")LocalDate date){

        System.out.println(date);
        return date.plusMonths(1).minusDays(1).toString();

    }

    @GetMapping("/auto-complete")
    @ResponseBody
    public String autoComplete(@RequestParam("word") String word) throws JsonProcessingException {
        List<String> list =expenseRepository.getDistinctDescription(word+"%");


        return objectMapper.writeValueAsString(list);
    }

    @GetMapping("/dataset/category")
    @ResponseBody
    public String datasetCategory(@SessionAttribute("user") User user) throws JsonProcessingException {

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(today.getDayOfMonth() - 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        List<CategoryExpense> categoryExpenses = expenseRepository.getCategoryExpenseByPeriodOrderByCategoryId(
                user.getId(), startDate, endDate);

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for(CategoryExpense one : categoryExpenses){
            labels.add(one.getCategoryName());
            data.add(one.getTotal());
        }

        return objectMapper.writeValueAsString(ChartDataResponse.builder().labels(labels).data(data).build());
//        return objectMapper.writeValueAsString(categoryExpenses);
    }

    @GetMapping("/dataset/daily")
    @ResponseBody
    public String datasetDaily(@SessionAttribute("user") User user) throws JsonProcessingException {

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(today.getDayOfMonth() - 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<DailyExpense> list = expenseRepository.getDailyExpenseByUserIdAndPeriod(user.getId(), startDate, endDate);
        Map<LocalDate, DailyExpense> listMap = new HashMap<>();
        for(DailyExpense expense : list) {
            //    System.out.println(expense);
            listMap.put(expense.getExpenseDate(), expense);
        }

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

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for(DailyExpense one : fullList){
            labels.add(one.getExpenseDate().toString());
            data.add(one.getTotal());
        }


        return objectMapper.writeValueAsString(ChartDataResponse.builder().labels(labels).data(data).build());
    }

}
