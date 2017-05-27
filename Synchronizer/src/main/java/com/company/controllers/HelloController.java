package com.company.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Rinat on 27.05.2017.
 */
@Controller
public class HelloController {
  @RequestMapping(value = "/index", method = RequestMethod.GET)
  public String index(Model model) {
    model.addAttribute("temp", "Greetings from Spring Boot!");
    return "index";
  }
}
