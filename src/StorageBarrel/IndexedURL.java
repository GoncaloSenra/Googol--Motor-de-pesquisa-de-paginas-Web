package StorageBarrel;

import java.io.Serializable;
import java.util.ArrayList;

public class IndexedURL implements Serializable {
    private String url;
    private String title;
    private String quote;
    private ArrayList<String> links;

    public IndexedURL(String Url, String Title, ArrayList<String> Links) {
        this.url = Url;
        this.title = Title;
        this.quote = "";
        this.links = Links;
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

    public ArrayList<String> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<String> links) {
        this.links = links;
    }
}
