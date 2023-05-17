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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/index")
public class IndexController {

    private final WebServerRMI rmi;

    public IndexController(WebServerRMI rmi) {
        this.rmi = rmi;
    }

    @GetMapping("")
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

}
