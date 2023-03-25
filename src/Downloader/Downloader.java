
package Downloader;

import StorageBarrel.StorageBarrel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.*;
import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import SearchModule.URL;
import SearchModule.SMInterface;


public class Downloader extends UnicastRemoteObject implements DInterface, Serializable  {

    private static int serversocket = 6000;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;

    public Downloader() throws RemoteException {
        super();
    }

    public static void main(String[] args) {

        Downloader d = null;
        SMInterface sm = null;
        try {
            sm = (SMInterface) LocateRegistry.getRegistry(8888).lookup("Downloader");
            d = new Downloader();

            sm.NewDownloader((DInterface) d);

        } catch (RemoteException | NotBoundException re) {
            System.out.println("Exception in Downloader.main: " + re);
        }

        String regex =  "(http|https|ftp)://[\\w_-]+(\\.[\\w_-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";

        MulticastSocket socket = null;

        // Open TCP Socket
        try (Socket s = new Socket("localhost", serversocket)) {

            System.out.println("SOCKET=" + s);

            try {
                socket = new MulticastSocket();  // create socket without binding it (only for sending)

                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                while (true) {
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

                            links = d.CrawlerUrls(url, doc);
                            words = d.CrawlerWords(url, doc);
                            title = doc.title();
                            //TODO: Falta citaçao


                        } catch (IOException e) {
                            System.out.println("Jsoup Connection: " + e.getMessage());
                        }

                        String message = "";

                        if (links != null) {
                            message = "Type | url_list; item_count | " + links.size() + "; ";
                            for (String str : links) {
                                message = message + ("item | " + str + "; ");
                            }

                            // Envia pacote por multicast

                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            ObjectOutputStream outMulticast = new ObjectOutputStream(bytes);

                            System.out.println("packet arrived");
                            InetAddress group = InetAddress.getByName(d.MULTICAST_ADDRESS);
                            outMulticast.writeObject(new URL(url, title, links, words));
                            //System.out.println(packet.getTitle());
                            byte[] buffer = bytes.toByteArray();

                            DatagramPacket Dpacket = new DatagramPacket(buffer, buffer.length, group, d.PORT);
                            socket.send(Dpacket);

                            bytes.close();
                            outMulticast.close();

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
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket.close();
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
