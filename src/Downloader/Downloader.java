
package Downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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

            while(true) {
                out.writeUTF("Type | new_url");
                String url = in.readUTF();

                System.out.println("Url: " + url);

                ArrayList<String> links = CrawlerUrls(url);
                ArrayList<String> words = CrawlerWords(url);

                String message = "Type | url_list; item_count | " + links.size() + "; ";

                for (String str : links) {
                    message = message + ("item | " + str + "; ");
                }

                System.out.println(message);
                out.writeUTF(message);

                String response = in.readUTF();
                System.out.println("Response: " + response);
            }

        } catch (IllegalArgumentException e) {
            System.out.println("BadUrl: " + e.getMessage());
        }catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }


    }

    public ArrayList<String> CrawlerWords(String url) {
        ArrayList<String> arraywords = new ArrayList<>();
        try {

            Document doc = Jsoup.connect(url).get();
            StringTokenizer tokens = new StringTokenizer(doc.text());

            while (tokens.hasMoreElements()) {
                String word = tokens.nextToken().toLowerCase();
                arraywords.add(word);
                //System.out.println(word);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return arraywords;
    }

    public ArrayList<String> CrawlerUrls(String url) {
        ArrayList<String> arraylinks = new ArrayList<>();
        try {
            String regex =  "(http|https|ftp)://[\\w_-]+(\\.[\\w_-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
            Pattern p = Pattern.compile(regex);


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
                if ((p.matcher(link.attr("abs:href"))).matches())
                    arraylinks.add(link.attr("abs:href"));
            }


        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }

        return arraylinks;
    }
}

class MulticastDownloader extends Thread {

    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private long SLEEP_TIME = 5000;

    public MulticastDownloader() {
        super("Server " + (long) (Math.random() * 1000));
    }

    public void run() {
        MulticastSocket socket = null;
        long counter = 0;
        System.out.println(this.getName() + " running...");
        try {
            socket = new MulticastSocket();  // create socket without binding it (only for sending)
            while (true) {
                String message = this.getName() + " packet " + counter++;
                byte[] buffer = message.getBytes();
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);

                try { sleep((long) (Math.random() * SLEEP_TIME)); } catch (InterruptedException e) { }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

}

