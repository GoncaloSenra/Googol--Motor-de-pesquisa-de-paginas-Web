package com.googol.WebServer;

import RMICon.WebServerRMI;
import forms.Tokens;
import forms.URL;
import forms.Number;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final WebServerRMI rmi;

    public SearchController(WebServerRMI rmi) {
        this.rmi = rmi;
    }

    @Resource(name = "sessionSearchList")
    private ArrayList<String[]> list;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ArrayList<String[]> sessionSearchList() {
        return new ArrayList<>();
    }

    @Resource(name = "sessionSearchPages")
    private Number TotalPages;
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number sessionSearchPages() {
        return new Number();
    }

    @Resource(name = "sessionSearchLastSearch")
    private Tokens LastSearch;
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Tokens sessionSearchLastSearch() {
        return new Tokens();
    }

    @GetMapping("")
    public String searchGet(@RequestParam(name="tokens", defaultValue = "") String tokens, @RequestParam(name ="page", defaultValue = "-1") int NumPage , Model model) {

        Number page = new Number();
        page.setNum(NumPage);

        System.out.println(page.getNum());

        System.out.println(tokens);
        if (!tokens.equals("") || !LastSearch.getWords().equals("")) {

            HashMap<Integer, ArrayList<String[]>> response;

            if (!tokens.equals("")) {
                response = rmi.Search(tokens, page.getNum());
            } else {
                response = rmi.Search(LastSearch.getWords(), page.getNum());
            }


            if (response.get(0) != null){
                TotalPages = new Number();
                list.clear();
                LastSearch = new Tokens();
            } else {
                for (Map.Entry<Integer, ArrayList<String[]>> entry : response.entrySet()) {
                    TotalPages.setNum(entry.getKey());
                    list = entry.getValue();
                }
            }
        }

        model.addAttribute("tokens", tokens);
        model.addAttribute("list", list);
        model.addAttribute("totalPages", TotalPages.getNum());
        model.addAttribute("page", page);

        return "search";
    }
    /*
    @GetMapping("/{page}")
    public String searchGet(Model model, @PathVariable int page) {



        return "search";
    }
    */
    /*
    @PostMapping("")
    public String searchPost(@ModelAttribute Tokens search, @ModelAttribute(value = "-1") int page) {
        System.out.println(search.toString());

        HashMap<Integer, ArrayList<String[]>> response = rmi.Search(search.getWords(), page);


        if (response.get(0) != null){
            TotalPages = new Number();
            list.clear();
            LastSearch = new Tokens();
        } else {
            for (Map.Entry<Integer, ArrayList<String[]>> entry : response.entrySet()) {
                TotalPages.setNum(entry.getKey());
                list = entry.getValue();
            }
        }


        return "redirect:/search/" + page;
    }
    */

}
