package com.googol.WebServer;

import RMICon.WebServerRMI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;


@Controller
@RequestMapping("/HackerNews")
public class HackerNewsController {

    private final WebServerRMI rmi;

    public HackerNewsController(WebServerRMI rmi) {
        this.rmi = rmi;
    }

    /**
     * Endpoint para idexar stories de um utilizador
     * @param tokens
     * @return
     */
    @PostMapping("")
    public String indexFromHackerNews(@RequestParam(name="tokens", defaultValue = "") String tokens) {
        System.out.println("HN: " + tokens);

        try {
            // Establecer conexao HTTP
            URL url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-agent", "GoogleBot");

            // Obter a response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String jsonResponse;
            StringBuilder res = new StringBuilder();

            while ((jsonResponse = in.readLine()) != null) {
                res.append(jsonResponse);
            }
            in.close();

            // Transformar em JSONArray
            JSONArray jsonArray = new JSONArray(res.toString());
            System.out.println(jsonArray.toString());

            String[] splitedTokens = tokens.split(" ");

            // Iterar as 100 melhores stories
            for (int i = 0; i < 100; i++) {
                // Establecer conexão HTTP
                URL url2 = new URL("https://hacker-news.firebaseio.com/v0/item/" + jsonArray.get(i) + ".json?print=pretty");
                con = (HttpURLConnection) url2.openConnection();

                // Obter a response
                BufferedReader in2 = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in2.readLine()) != null) {
                    response.append(inputLine);
                }
                in2.close();
                System.out.println(response);

                // Transformar em JSON
                JSONObject json = new JSONObject(response.toString());

                System.out.println(json);

                // Se a story tiver "text" e "url", e o(s) token(s) enviado(s) pelo utilizador estiverem presentes no texto, então o link será enviado parar indexar
                if (json.keySet().contains("url") && json.keySet().contains("text")) {
                    String[] text = json.get("text").toString().split(" ");
                    int tam = splitedTokens.length;
                    System.out.println(text.length);
                    for (String tok : splitedTokens) {
                        for (String word : text) {
                            System.out.println("---->" + word + " - " + tok + "<----");
                            if (word.equalsIgnoreCase(tok)) {
                                tam--;
                            }
                        }
                    }
                    if (tam <= 0) {
                        rmi.IndexLink(json.get("url").toString());
                    }
                }

                System.out.println("------------>" + i + " / 99");

            }


        } catch (IOException e){
            e.printStackTrace();
        }
        return "redirect:/search";
    }

    /**
     * Endpoint para indexar todas as stories de um utilizador
     * @param username
     * @param model
     * @return
     */
    @GetMapping("/users")
    public String indexUserStories(@RequestParam(name="username", defaultValue = "") String username, Model model) {

        if (!username.equals("")) {
            System.out.println(username);

            try {
                // Establecer conexão HTTP
                URL url = new URL("https://hacker-news.firebaseio.com/v0/user/"+ username + ".json?print=pretty");

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setDoOutput(true);
                con.setInstanceFollowRedirects(false);
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("User-agent", "GoogleBot");

                // Obter a response
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String jsonResponse;
                StringBuilder res = new StringBuilder();

                while ((jsonResponse = in.readLine()) != null) {
                    res.append(jsonResponse);
                }
                in.close();

                // Se o utilizador não existe, então é enviada uma mensagem de erro para o utilizador
                if (res.toString().equals("null")) {
                    model.addAttribute("not_found", "-1");
                    model.addAttribute("username", username);

                    return "users_hackernews";
                } else {
                    // Transforma em JSON
                    JSONObject json = new JSONObject(res.toString());

                    JSONArray stories = (JSONArray) json.get("submitted");

                    System.out.println(stories.toString());

                    // Percorre todas as stories do utilizador
                    for (int i = 0; i < stories.length(); i++) {
                        // Establecer a conexão HTTP
                        URL url2 = new URL("https://hacker-news.firebaseio.com/v0/item/" + stories.get(i) + ".json?print=pretty");
                        con = (HttpURLConnection) url2.openConnection();

                        // Obter a response
                        BufferedReader in2 = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in2.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in2.close();
                        System.out.println(response);

                        // Transformar em JSON
                        JSONObject jsonobj = new JSONObject(response.toString());

                        // Se a story tiver "url, entao o link é enviado para indexar"
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
