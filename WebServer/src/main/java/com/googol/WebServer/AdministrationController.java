package com.googol.WebServer;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.HtmlUtils;

@Controller
@RequestMapping("/admin")
public class AdministrationController {

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
    public String adminGet() {

        return "administration";
    }
}
