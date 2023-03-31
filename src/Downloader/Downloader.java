
package Downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.*;
import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import SearchModule.URL;
import SearchModule.SMInterface;


public class Downloader extends UnicastRemoteObject implements DInterface, Serializable  {

    public static int MAX_RETRY = 3;
    private static int serversocket = 6000;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private int Id;
    private String IP = "localhost";
    private int numBarrels;
    public int terminate = 0;
    public int UDPPORT;

    public Downloader() throws RemoteException {
        super();
    }

    // Função chamada pelo Search Module para atualizar o número de Barrels ativos
    public void UpdateNumBarrels(int num) throws RemoteException {
        this.numBarrels = num;
        System.out.println("num: " + num);
    }

    // Função chamada pelo Search Module para fechar o Downloader
    public void ExitDownloaders() throws RemoteException {
        //System.exit(0);
        terminate = 1;
    }

    
    public static void main(String[] args) {

        try {
            SMInterface sm = (SMInterface) LocateRegistry.getRegistry(8888).lookup("Downloader");
            Downloader d = new Downloader();
            d.Id = sm.NewDownloader((DInterface) d);

            // Define UDPPORT
            int digits = 0;
            int auxID = d.Id + 1;
            while (auxID != 0) {
                auxID = auxID / 10;
                digits++;
            }
            auxID = d.Id + 1;
            for (int i = 0; i < 4 - digits; i++) {
                auxID = auxID * 10;
            }

            d.UDPPORT = auxID;

            // Thread que termina o Downloader de forma segura de maneira a não haver perda de informação
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        if (d.terminate == 0)
                            sm.TerminateDownloader(d.Id);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Downloader is being terminated!");
                }
            });

            Thread t = new Thread(new Runnable(){
                public void run() {
                    while(true) {
                        if (d.terminate == 2) {
                            System.exit(0);
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (java.lang.InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            });

            t.start();

            // Regex para filtrar links indesejados (por exemplo 'javascript- popup ...')
            String regex =  "(http|https|ftp)://[\\w_-]+(\\.[\\w_-]+)+([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";

            MulticastSocket socket = null;

            // Abrir Socket TCP
            try (Socket s = new Socket(d.IP, serversocket)) {

                System.out.println("SOCKET=" + s);

                socket = new MulticastSocket();
                InetAddress group = InetAddress.getByName(d.MULTICAST_ADDRESS);

                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                try {
                    while (true) {

                        // Quadno a variável de condição terminate é ativada (função ExitDownloader) o ciclo é quebrado
                        if (d.terminate == 1) {
                            d.terminate++;
                            break;
                        }

                        // Este ciclo verifica se existem barrels ativos, logo, se não houver nenhum barrel o Downloader não processa nenhum link
                        boolean close = false;
                        while (d.numBarrels == 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (java.lang.InterruptedException e) {
                                System.out.println(e.getMessage());
                            }

                            if (d.terminate == 1) {
                                close = true;
                                break;
                            }
                            //System.out.println(d.numBarrels);
                        }

                        if (close) {
                            continue;
                        }


                        Pattern p = Pattern.compile(regex);

                        // Pede novo link à Queue
                        out.writeUTF("Type | new_url");
                        String url = in.readUTF();


                        System.out.println("Url: " + url);

                        ArrayList<String> links = null;
                        ArrayList<String> words = null;
                        String title = "";
                        String quote = "";

                        if (p.matcher(url).matches()) {
                            try {
                                Document doc = Jsoup.connect(url).get();

                                links = d.CrawlerUrls(url, doc);
                                words = d.CrawlerWords(url, doc);
                                title = doc.title();

                                //Encontra o elemento HTML paragraph (<p>) e retira o texto associado para a Quote
                                Element firstParagraph = doc.select("p").first();
                                if (firstParagraph == null || firstParagraph.text().equals("Aviso(s): O browser não tem suporte ativo para JavaScript. É necessária a sua ativação para poder continuar a usar a aplicação.")) {
                                    quote = "No quote!";
                                } else {
                                    quote = firstParagraph.text();
                                }


                            } catch (IOException e) {
                                System.out.println("Jsoup Connection: " + e.getMessage());
                            }

                            String message = "";

                            if (links != null) {
                                message = "Type | url_list; item_count | " + links.size() + "; ";
                                for (String str : links) {
                                    message = message + ("item | " + str + "; ");
                                }

                                // Compacta o pacote e envia por multicast
                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                ObjectOutputStream outMulticast = new ObjectOutputStream(bytes);


                                int numPacket = sm.getPacket();
                                URL link = new URL(url, title, links, words, quote, numPacket, d.UDPPORT);
                                sm.increasePacket(link);
                                outMulticast.writeObject(link);

                                //System.out.println(packet.getTitle());
                                byte[] buffer = bytes.toByteArray();

                                ByteArrayOutputStream bytescompressed = new ByteArrayOutputStream();
                                GZIPOutputStream gzipos = new GZIPOutputStream(bytescompressed);
                                gzipos.write(buffer);
                                gzipos.close();
                                byte[] compressedObject = bytescompressed.toByteArray();


                                DatagramPacket Dpacket = new DatagramPacket(compressedObject, compressedObject.length, group, d.PORT);


                                // ACKS
                                int count = 0;
                                int times = 0;
                                boolean sent = false;

                                // Tenta receber um acknowledgment de todos os Barrels ativos, no máximo 3 vezes (MAX_RETRY),
                                // se não conseguir adiciona novamente o link à fila
                                while (times < MAX_RETRY) {
                                    System.out.println("TRY: " + (times + 1));
                                    System.out.println("BARRELS: " + d.numBarrels);
                                    try (DatagramSocket aSocket = new DatagramSocket(d.UDPPORT)) {
                                        aSocket.setSoTimeout(2000);
                                        System.out.println("Socket Datagram à escuta no porto " + d.UDPPORT);
                                        socket.send(Dpacket);

                                        while(count < d.numBarrels){
                                            byte[] buff = new byte[128];
                                            DatagramPacket request = new DatagramPacket(buff, buff.length);
                                            aSocket.receive(request);
                                            String str = new String(request.getData(), 0, request.getLength());

                                            if (str.equals("Received!")) {
                                                System.out.println("Recebido =)");
                                                count++;
                                            }

                                        }

                                    } catch(SocketTimeoutException e) {
                                        System.out.println("TimeOut, sending packet again!");
                                        times++;
                                        continue;
                                    }catch (SocketException e) {
                                        System.out.println("Socket: " + e.getMessage());
                                    } catch (IOException e) {
                                        System.out.println("IO: " + e.getMessage());
                                    }
                                    sent = true;
                                    break;
                                }


                                if (sent == false) {
                                    message = "Type | url_list; item_count | 1; item | " + link.getUrl();
                                }

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
                    System.out.println("IO: " + e.getMessage());
                } finally {
                    System.out.println("Closing Sockets");
                    socket.close();
                    s.close();
                }

            } catch (IllegalArgumentException e) {
                System.out.println("BadUrl: " + e.getMessage());
            }catch (UnknownHostException e) {
                System.out.println("Sock:" + e.getMessage());
            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
            }
        } catch (RemoteException | NotBoundException re) {
            System.out.println("Connection with Search Module failed!");
        }
    }

    // Retira as palavras do url eviado pela fila
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

    // Retira as links do url eviado pela fila
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
