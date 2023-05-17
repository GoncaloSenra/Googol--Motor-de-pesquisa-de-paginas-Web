package com.googol.WebServer;

import RMICon.WebServerRMI;
import forms.Number;
import forms.Tokens;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Controller
@RequestMapping("/pointers")
@Scope("session")
public class PointersController {

    private final WebServerRMI rmi;

    public PointersController(WebServerRMI rmi) {
        this.rmi = rmi;
    }

    @Resource(name = "sessionSearchPointersList")
    private HashSet<String[]> list;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public HashSet<String[]> sessionSearchPointersList() {
        return new HashSet<>();
    }

    @GetMapping("")
    public String pointersGet(@RequestParam(name="tokens", defaultValue = "") String url, Model model) {

        HashSet<String[]> response;
        if (!url.equals("")) {
            response = rmi.SearchPointers(url);

            if (response != null) {
                list = response;
            } else {
                list = new HashSet<>();
            }
        } else {
            list.clear();
        }

        model.addAttribute("list", list);

        return "pointers";
    }

}
