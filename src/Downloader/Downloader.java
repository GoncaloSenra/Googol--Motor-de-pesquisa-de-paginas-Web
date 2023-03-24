
package Downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import SearchModule.URL;


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

        String regex =  "(http|https|ftp)://[\\w_-]+(\\.[\\w_-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";

        // Run Multicast Server
        MulticastServerDownloader msd = new MulticastServerDownloader();
        msd.start();

        msd.packet = new URL("piroca", "teste", null, null);
        msd.send_packet = true;

        // Open TCP Socket
        try (Socket s = new Socket("localhost", serversocket)) {

            System.out.println("SOCKET=" + s);

            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            while(true) {
                Pattern p = Pattern.compile(regex);

                out.writeUTF("Type | new_url");
                String url = in.readUTF();

                System.out.println("Url: " + url);

                ArrayList<String> links = null;
                ArrayList<String> words = null;
                String title = "";

                if (p.matcher(url).matches()) {
                    try {
                        Document doc = Jsoup.connect(url).get();

                        links = CrawlerUrls(url, doc);
                        words = CrawlerWords(url, doc);
                        title = doc.title();
                        //TODO: Falta citaçao


                    } catch (IOException e) {
                        System.out.println("Jsoup Connection: " + e.getMessage());
                    }

                    String message = "";

                    if (links != null){
                        message = "Type | url_list; item_count | " + links.size() + "; ";
                        for (String str : links) {
                            message = message + ("item | " + str + "; ");
                        }

                        msd.packet = new URL(url, title, null, null);
                        msd.send_packet = true;
                    } else {
                        message = "Type | url_list; item_count | 0";
                    }

                    System.out.println(message);
                    out.writeUTF(message);

                    String response = in.readUTF();
                    System.out.println("Response: " + response);
                } else {
                    String message = "Type | url_list; item_count | 0";

                    System.out.println(message);
                    out.writeUTF(message);

                    String response = in.readUTF();
                    System.out.println("Response: " + response);
                }
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

    public ArrayList<String> CrawlerWords(String url, Document doc) {
        ArrayList<String> arraywords = new ArrayList<>();

        //Document doc = Jsoup.connect(url).get();
        StringTokenizer tokens = new StringTokenizer(doc.text());

        while (tokens.hasMoreElements()) {
            String word = tokens.nextToken().toLowerCase();
            arraywords.add(word);
            //System.out.println(word);
        }

        return arraywords;
    }

    public ArrayList<String> CrawlerUrls(String url, Document doc) {
        ArrayList<String> arraylinks = new ArrayList<>();

        //Document doc = Jsoup.connect(url).get();

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            //System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
            arraylinks.add(link.attr("abs:href"));
        }


        return arraylinks;
    }
}

class MulticastServerDownloader extends Thread {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private long SLEEP_TIME = 5000;
    public boolean send_packet;
    public URL packet;

    public MulticastServerDownloader() {
        super("Server " + (long) (Math.random() * 1000));
        this.send_packet = false;
    }

    public void run() {
        MulticastSocket socket = null;
        System.out.println(this.getName() + " running...");
        try {
            socket = new MulticastSocket();  // create socket without binding it (only for sending)

            while (true) {

                if(this.send_packet) {

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(bytes);

                    System.out.println("packet arrived");
                    InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                    out.writeObject(packet);
                    System.out.println(packet.getTitle());
                    byte[] buffer = bytes.toByteArray();

                    DatagramPacket Dpacket = new DatagramPacket(buffer, buffer.length, group, PORT);
                    socket.send(Dpacket);

                    this.send_packet = false;
                    this.packet = null;

                    bytes.close();
                    out.close();
                }
                //try { sleep((long) (Math.random() * SLEEP_TIME)); } catch (InterruptedException e) { }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

}

