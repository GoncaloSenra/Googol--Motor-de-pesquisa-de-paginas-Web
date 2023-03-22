package SearchModule;

import java.io.Serializable;
import java.util.ArrayList;

public class URL implements Serializable {

    private String title;
    private String url;
    private String quote;
    private ArrayList<String> urls;

    public URL(String url, String title) {
        this.title = title;
        this.url = url;
        this.quote = "";
        this.urls = new ArrayList<>();
    }
}
