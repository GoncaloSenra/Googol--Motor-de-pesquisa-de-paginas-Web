
import java.net.*;
import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerUrlList extends Thread{

    LinkedBlockingQueue<String> Urls = new LinkedBlockingQueue<>();
    private static int serverPort = 6000;

    public void run(){

        Urls.add("https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos");

        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            System.out.println("A escuta no porto 6000");
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);

                //Nova Thread para o canal de comunicaçao TCP
                new Connection(clientSocket, Urls.take());

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

    public Connection (Socket aClientSocket, String url) {
        try{
            clientSocket = aClientSocket;
            link = url;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    //=============================
    public void run(){
        String resposta;
        try {
            while(true){
                String data = in.readUTF();
                //System.out.println("T[" + thread_number + "] Recebeu: "+data);
                if (data.equals("Type | new_url")) {
                    out.writeUTF(link);
                } else if (data.equals("Type | url_list")) {
                    out.writeUTF("Links received!");
                    break;
                }

                resposta=data.toUpperCase();
                out.writeUTF(resposta);
            }
        } catch(EOFException e) {
            System.out.println("EOF:" + e);
        } catch(IOException e) {
            System.out.println("IO:" + e);
        }
    }
}
