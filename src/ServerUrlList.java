
import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerUrlList extends Thread{

    LinkedBlockingQueue<String> Urls = new LinkedBlockingQueue<>();
    private static int serverPort = 6000;

    public void run(){
        //System.out.println("testestetestetse\n");
        //Urls.add("https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos");
        //Urls.add("https://inforestudante.uc.pt/nonio/security/login.do");

        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            //System.out.println("A escuta no porto 6000");
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);

                //Nova Thread para o canal de comunicaçao TCP
                new Connection(clientSocket, Urls.take(), Urls);

            }
        } catch(IOException e) {
            System.out.println("Listen:" + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Queue:" + e.getMessage());
        }


    }

}

class Connection extends Thread {
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    String link;

    LinkedBlockingQueue<String> Urls;

    public Connection (Socket aClientSocket, String url, LinkedBlockingQueue<String> urls) {
        try{
            clientSocket = aClientSocket;
            link = url;
            Urls = urls;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }

    public void run(){
        String resposta;
        try {
            while(true){
                String data = in.readUTF();
                //System.out.println("teste\n");
                String[] buffer = data.split(";");

                //Downloader pede link ao ServerUrlList
                if (buffer[0].equals("Type | new_url")) {
                    out.writeUTF(link);

                }
                //Downloader envia lista de urls ao ServerUrlList encontrados no link enviado
                //Termina a thread connection
                else if (buffer[0].equals("Type | url_list")) {
                    out.writeUTF("Link(s) received!");
                    break;
                }

                //resposta=data.toUpperCase();
                //out.writeUTF(resposta);
            }
        } catch(EOFException e) {
            System.out.println("EOF:" + e);
        } catch(IOException e) {
            System.out.println("IO:" + e);
        }
    }
}
