package com.googol.WebServer;

import RMICon.WebServerRMI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        System.out.println("HN: " + tokens);

        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-agent", "GoogleBot");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String jsonResponse;
            StringBuilder res = new StringBuilder();

            while ((jsonResponse = in.readLine()) != null) {
                res.append(jsonResponse);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(res.toString());
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

                System.out.println(json);

                if (json.keySet().contains("url") && json.keySet().contains("text")) {
                    String[] text = json.get("text").toString().split(" ");
                    System.out.println(text.length);
                    for (String tok : splitedTokens) {
                        for (String word : text) {
                            System.out.println("---->" + word + " - " + tok + "<----");
                            if (word.equalsIgnoreCase(tok)) {
                                rmi.IndexLink(json.get("url").toString());
                            }
                        }
                    }
                }

                System.out.println("------------>" + i + " / 100");

            }


        } catch (IOException e){
            e.printStackTrace();
        }
        return "redirect:/search";
    }

    @GetMapping("/users")
    public String indexUserStories(@RequestParam(name="username", defaultValue = "") String username, Model model) {

        if (!username.equals("")) {
            System.out.println(username);

            try {
                URL url = new URL("https://hacker-news.firebaseio.com/v0/user/"+ username + ".json?print=pretty");

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setDoOutput(true);
                con.setInstanceFollowRedirects(false);
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("User-agent", "GoogleBot");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String jsonResponse;
                StringBuilder res = new StringBuilder();

                while ((jsonResponse = in.readLine()) != null) {
                    res.append(jsonResponse);
                }
                in.close();

                if (res.toString().equals("null")) {
                    model.addAttribute("not_found", "-1");
                    model.addAttribute("username", username);

                    return "users_hackernews";
                } else {
                    JSONObject json = new JSONObject(res.toString());

                    JSONArray stories = (JSONArray) json.get("submitted");

                    System.out.println(stories.toString());

                    for (int i = 0; i < stories.length(); i++) {
                        URL url2 = new URL("https://hacker-news.firebaseio.com/v0/item/" + stories.get(i) + ".json?print=pretty");
                        con = (HttpURLConnection) url2.openConnection();
                        BufferedReader in2 = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in2.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in2.close();
                        System.out.println(response);
                        JSONObject jsonobj = new JSONObject(response.toString());

                        if (jsonobj.keySet().contains("url")) {
                            rmi.IndexLink((String) jsonobj.get("url"));
                        }

                    }


                    model.addAttribute("not_found", "-2");
                    model.addAttribute("username", username);
                    return "users_hackernews";
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        model.addAttribute("not_found", "0");
        model.addAttribute("username", username);

        return "users_hackernews";

    }
}
