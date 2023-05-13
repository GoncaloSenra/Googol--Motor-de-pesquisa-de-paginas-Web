package com.googol.WebServer;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import RMICon.WebServerRMI;
import forms.URL;

import java.net.http.HttpRequest;
import java.rmi.RemoteException;
import java.util.ArrayList;

@Controller
public class TestController {

    @Resource(name = "appCreateRMIRegistry")
    private WebServerRMI rmi;

    @Resource(name = "sessionSearchList")
    private ArrayList<String[]> list;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public WebServerRMI appCreateRMIRegistry() {
        try {
            System.out.println("NEW REGISTRY");
            return new WebServerRMI();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ArrayList<String[]> sessionSearchList() {
        return new ArrayList<>();
    }

    @GetMapping("/")
    public String redirect() {
        return "redirect:/greeting";
    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        model.addAttribute("othername", "SD");
        return "greeting";
    }
    @GetMapping("/index")
    public String index(Model model) {

        model.addAttribute("url", new URL());

        return "index";
    }

    @PostMapping("/send_url")
    public String sendURL(@ModelAttribute URL url) {
        System.out.println(url.toString());
        rmi.IndexLink(url.getLink());

        return "redirect:/index";
    }

    @GetMapping("/search")
    public String searchGet(Model model) {

        model.addAttribute("url", new URL());

        return "search";
    }

    @PostMapping("/search")
    public String searchPost(@ModelAttribute URL url) {
        System.out.println(url.toString());

        list = rmi.Search(url.getLink());

        return "redirect:/search";
    }



}
