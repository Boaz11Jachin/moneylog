package org.codenova.moneylog.controller;

import lombok.AllArgsConstructor;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.time.LocalDate;
import java.util.Optional;


@Controller
@AllArgsConstructor
public class IndexController {
    private final JavaMailSenderImpl mailSender;
    private ExpenseRepository expenseRepository;


    @GetMapping({"/index", "/"})
    public String indexHandel(@SessionAttribute("user") Optional<User> user) {
        if(user.isEmpty()){
            return "index";
        }else{
            return "redirect:/home";
        }
    }

    @GetMapping("/home")
    public String homeHandel(@SessionAttribute("user") Optional<User> user, Model model) {


        if(user.isPresent()){

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate endDate = today.plusDays(7 - today.getDayOfWeek().getValue());

            model.addAttribute("user", user.get());
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);


            model.addAttribute("totalAmountByPeriod",
                    expenseRepository.getTotalAmountByPeriod(user.get().getId(),
                            startDate, endDate) );

            model.addAttribute("top3ExpensesByPeriod", expenseRepository.getTop3ExpensesByPeriod(user.get().getId(),
                    startDate, endDate) );

            model.addAttribute("categoryExpenseByPeriods", expenseRepository.getCategoryExpenseByPeriod(user.get().getId(),
                    startDate, endDate) );


            return "/home";
        }else{
            return "redirect:/";
        }

    }


}
