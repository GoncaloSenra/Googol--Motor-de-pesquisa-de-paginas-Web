
package SearchModule;

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerUrlList extends Thread{

    private LinkedBlockingQueue<String> Urls;
    private static int serverPort = 6000;

    public ServerUrlList() {
        this.Urls = new LinkedBlockingQueue<>();
    }

    public void run(){
        //index https://inforestudante.uc.pt/nonio/security/login.do
        //index https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos

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

