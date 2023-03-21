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

        while(true) {
            try (Socket s = new Socket("localhost", serversocket)) {
                System.out.println("SOCKET=" + s);

                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());


                out.writeUTF("Type | new_url");

                String url = in.readUTF();

                System.out.println("Url: " + url);

                ArrayList<String> links = Crawler(url);
                String message = "Type | url_list; item_count | " + links.size() + "; ";

                for (String str: links) {
                    message = message + ("item | " + str + "; ");
                }

                System.out.println(message);

                out.writeUTF("Type | url_list; item_count | 2");

                String response = in.readUTF();

                System.out.println("teste: " + response);


            } catch (UnknownHostException e) {
                System.out.println("Sock:" + e.getMessage());
            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
            } catch (IOException e) {
                //System.out.println("IO:" + e.getMessage());
            }
        }
    }

    public ArrayList<String> Crawler(String url) {
        ArrayList<String> arraylinks = new ArrayList<>();
        try {

            Document doc = Jsoup.connect(url).get();
            /*StringTokenizer tokens = new StringTokenizer(doc.text());

            while (tokens.hasMoreElements()) {
                String word = tokens.nextToken().toLowerCase();
                System.out.println(word);
                //this.words.add(word);
            }*/

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
                arraylinks.add(link.attr("abs:href"));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return arraylinks;
    }
}
