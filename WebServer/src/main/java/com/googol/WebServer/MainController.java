package com.googol.WebServer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {


    /**
     * Redireciona para a página de pesquisa
     * @return
     */
    @GetMapping("/")
    public String redirect() {

        return "redirect:/search";
    }
}
