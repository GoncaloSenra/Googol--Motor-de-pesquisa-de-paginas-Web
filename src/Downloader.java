import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.SynchronousQueue;

class LinkQueue {
    private SynchronousQueue<String> links = new SynchronousQueue<>();

}

public class Downloader /*implements Runnable*/{
    /*@Override
    public void run() {
        //INSERT MAIN CODE HERE
    }*/
    public static void main(String args[]) {
        //String url = args[0];
        String url = "https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos";
        try {
            Document doc = Jsoup.connect(url).get();
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements() && countTokens++ < 100)
                System.out.println(tokens.nextToken().toLowerCase());
            Elements links = doc.select("a[href]");
            for (Element link : links)
                System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
