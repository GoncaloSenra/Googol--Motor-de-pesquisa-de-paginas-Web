package SearchModule;

import java.io.Serializable;
import java.util.ArrayList;

public class URL implements Serializable {
    private String url;
    private String title;
    private String quote;
    private ArrayList<String> urls;
    private ArrayList<String> words;

    public URL(String url, String title, ArrayList<String> URLS, ArrayList<String> WORDS) {
        this.title = title;
        this.url = url;
        this.quote = "";
        this.urls = URLS;
        this.words = WORDS;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }

    public ArrayList<String> getWords() {
        return words;
    }

    public void setWords(ArrayList<String> words) {
        this.words = words;
    }
}
