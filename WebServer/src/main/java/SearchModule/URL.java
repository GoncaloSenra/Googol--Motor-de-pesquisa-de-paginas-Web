package SearchModule;

import java.io.Serializable;
import java.util.ArrayList;

// Classe utilizada para guardar toda a informação necessária para ser enviada por multicast
public class URL implements Serializable {
    private String url;
    private String title;
    private String quote;
    private ArrayList<String> urls;
    private ArrayList<String> words;
    private int packet;
    private int UDPPORT;

    public URL(String url, String title, ArrayList<String> URLS, ArrayList<String> WORDS, String QUOTE, int packet, int port) {
        this.title = title;
        this.url = url;
        this.quote = QUOTE;
        this.urls = URLS;
        this.words = WORDS;
        this.packet = packet;
        this.UDPPORT = port;
    }

    public int getUDPPORT() {
        return UDPPORT;
    }

    public void setUDPPORT(int UDPPORT) {
        this.UDPPORT = UDPPORT;
    }

    public int getPacket() {
        return packet;
    }

    public void setPacket(int packet) {
        this.packet = packet;
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
