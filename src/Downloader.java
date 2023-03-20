import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.concurrent.SynchronousQueue;
import java.util.ArrayList;



public class Downloader extends Thread {

    private static int serversocket = 6000;
    //private ArrayList<String> words;

    /*public Downloader() {
        this.words = new ArrayList<String>();
    }*/
    public static void main(String[] args) {
        Downloader d = new Downloader();
        d.start();
    }

    public void run() {
        //String url = args[0];
        //String url = "https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos";

        try (Socket s = new Socket("localhost", serversocket)) {
            System.out.println("SOCKET=" + s);

            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            out.writeUTF("type | new_url");

            String url = in.readUTF();

            System.out.println("Url: " + url);




        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }
    }

    public void Crawler(String url) {
        try {

            Document doc = Jsoup.connect(url).get();
            StringTokenizer tokens = new StringTokenizer(doc.text());

            while (tokens.hasMoreElements()) {
                String word = tokens.nextToken().toLowerCase();
                System.out.println(word);
                //this.words.add(word);
            }

            /*Elements links = doc.select("a[href]");
            for (Element link : links)
                System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");*/
            System.out.println("FIM");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
