package com.googol.WebServer;

import RMICon.WebServerRMI;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
public class AdministrationController {

    private final WebServerRMI rmi;

    public AdministrationController(WebServerRMI rmi) {
        this.rmi = rmi;
    }

    /*
    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public Message onMessage(Message message) throws InterruptedException {
        System.out.println("Message received " + message);
        Thread.sleep(1000);
        return new Message(HtmlUtils.htmlEscape(message.content()));
    }
    */
    @GetMapping("")
    public String adminGet(Model model) {

        HashMap<Integer, String[]> barrels = rmi.getInfoBarrels();
        HashMap<Integer, String[]> downloaders = rmi.getInfoDownloaders();
        ArrayList<String[]> topSearches = rmi.getTopSearches();

        model.addAttribute("barrels", barrels);
        model.addAttribute("downloaders", downloaders);
        model.addAttribute("topSearches", topSearches);

        return "administration";
    }
}
