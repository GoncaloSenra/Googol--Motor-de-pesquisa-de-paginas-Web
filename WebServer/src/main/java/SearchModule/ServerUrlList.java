
package SearchModule;

import StorageBarrel.IndexedURL;

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerUrlList extends Thread{

    private LinkedBlockingQueue<String> Urls;
    private static int serverPort = 6000;

    public ServerUrlList() {
        this.Urls = new LinkedBlockingQueue<>();
    }

    public LinkedBlockingQueue<String> getUrls() {
        return Urls;
    }

    public void run(){

        // Se houver informação sobre a última execução a thread vai buscar o objeto BlockingQueue ao ficheiro
        try {
            String path = "src/main/java/SearchModule/queue.obj";
            //System.out.println(path);
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                if (file.length() != 0) {
                    FileInputStream fileIn = new FileInputStream(file);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    this.Urls = (LinkedBlockingQueue<String>) in.readObject();
                    in.close();
                    fileIn.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Socket TCP que comunica com os Downloaders e o Search Module
        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            //System.out.println("A escuta no porto 6000");
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);

                //Nova Thread para o canal de comunicaçao TCP
                new Connection(clientSocket, Urls);
            }
        } catch(IOException e) {
            System.out.println("Listen:" + e.getMessage());
        }
    }

}

class Connection extends Thread {
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    LinkedBlockingQueue<String> Urls;

    public Connection (Socket aClientSocket, LinkedBlockingQueue<String> urls) {
        try{
            clientSocket = aClientSocket;
            Urls = urls;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }

    public void run(){
        String resposta;
        try {
            while(true) {
                String data = in.readUTF();

                String[] buffer = data.split(";");

                //Downloader pede link ao ServerUrlList
                if (buffer[0].equals("Type | new_url")) {
                    out.writeUTF(Urls.take());

                    data = in.readUTF();
                    buffer = data.split(";");
                    int num_links = 0;
                    out.writeUTF("Link(s) received!");
                    System.out.println(data);
                    String[] aux = null;
                    if (buffer.length > 1){
                        aux = buffer[1].split("\\| ");
                        num_links = Integer.parseInt(aux[1]);
                        System.out.println("num_links: "+ num_links);
                        for (int i = 2; i < num_links + 2; i++) {
                            aux = buffer[i].split("\\| ");
                            //System.out.println(Arrays.toString(aux));
                            if (aux.length == 2 && !Urls.contains(aux[1]))
                                Urls.add(aux[1]);
                        }
                    }

                }
                //Downloader envia lista de urls ao ServerUrlList encontrados no link enviado
                else if (buffer[0].equals("Type | url_list")) {
                    System.out.println(data);
                    int num_links = 0;
                    out.writeUTF("Link(s) received!");
                    String[] aux = buffer[1].split("\\| ");
                    num_links = Integer.parseInt(aux[1]);

                    for (int i = 2; i < num_links + 2; i++) {
                        aux = buffer[i].split("\\| ");
                        Urls.add(aux[1]);
                    }
                    return;
                }

                try {
                    String path = "src/main/java/SearchModule/queue.obj";
                    //System.out.println(path);
                    File file = new File(path);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fileOut = new FileOutputStream(file, false);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(Urls);
                    out.close();
                    fileOut.close();

                } catch (FileNotFoundException e) {
                    System.out.println("FOS: " + e);
                } catch (IOException e) {
                    System.out.println("OOS: " + e);
                }

                //resposta=data.toUpperCase();
                //out.writeUTF(resposta);
            }
        } catch(EOFException e) {
            System.out.println("EOF:" + e);
        } catch(IOException e) {
            System.out.println("IO:" + e);
        } catch (InterruptedException e) {
            System.out.println("IE:" + e);
        }
    }
}

