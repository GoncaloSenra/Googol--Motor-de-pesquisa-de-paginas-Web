package com.googol.WebServer;

import RMICon.WebServerRMI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import RMICon.WebServerRMI;

@Controller
@RequestMapping("/HackerNews")
public class HackerNewsController {

    private final WebServerRMI rmi;

    public HackerNewsController(WebServerRMI rmi) {
        this.rmi = rmi;
    }

    @PostMapping("")
    public String indexFromHackerNews(@RequestParam(name="tokens", defaultValue = "") String tokens) {

        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-agent", "GoogleBot");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String jsonResponse = in.readLine();
            in.close();

            JSONArray jsonArray = new JSONArray(jsonResponse);
            System.out.println(jsonArray.toString());

            String[] splitedTokens = tokens.split(" ");

            for (int i = 0; i < 100; i++) {
                URL url2 = new URL("https://hacker-news.firebaseio.com/v0/item/" + jsonArray.get(i) + ".json?print=pretty");
                con = (HttpURLConnection) url2.openConnection();
                BufferedReader in2 = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in2.readLine()) != null) {
                    response.append(inputLine);
                }
                in2.close();
                System.out.println(response);
                JSONObject json = new JSONObject(response.toString());

                System.out.println(json.toString());

                if (json.keySet().contains("url") && json.keySet().contains("text")) {
                    String[] text = json.get("text").toString().split(" :.,;");
                    for (String tok : splitedTokens) {
                        for (String word : text) {
                            if (word.equals(tok)) {
                                rmi.IndexLink(json.get("url").toString());
                            }
                        }
                    }
                }

                //System.out.println("------------>" + i + " / 500");

            }

            System.out.println("HN: " + tokens);

        } catch (IOException e){
            e.printStackTrace();
        }
        return "redirect:/search";
    }
}
