package com.vti.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // sẽ trỏ tới /WEB-INF/jsp/register.jsp
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
