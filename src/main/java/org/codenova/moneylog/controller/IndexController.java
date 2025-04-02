package org.codenova.moneylog.controller;

import org.codenova.moneylog.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Optional;


@Controller
public class IndexController {

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
            model.addAttribute("user", user.get());
            return "home";
        }else{
            return "redirect:/";
        }
    }


}
