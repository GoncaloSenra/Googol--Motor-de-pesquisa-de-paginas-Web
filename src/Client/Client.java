
package Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import SearchModule.SMInterface;

//O SM tambem vai ter de ter um interfce do cliente para poder mandar as infos de administracao
//quando existem alteraçoes o SM chama uma funçao do cliente (RMI callback)

public class Client extends UnicastRemoteObject implements CInterface {
    private int Id;
    private HashMap<Integer, String[]> infoDownloaders;
    private HashMap<Integer, String[]> infoBarrels;
    private HashMap<String, Integer> topSearches;

    public void UpadateDownloaders(HashMap<Integer, String[]> info) throws RemoteException{
        this.infoDownloaders = info;
    }
    public void UpadateBarrels(HashMap<Integer, String[]> info) throws RemoteException{
        this.infoBarrels = info;
    }
    public void UpadateTopSearches(HashMap<Integer, String[]> info) throws RemoteException{

    }

    public Client() throws RemoteException {
        super();
        this.infoDownloaders = new HashMap<>();
        this.infoBarrels = new HashMap<>();
        this.topSearches = new HashMap<>();
    }

    public static void main(String args[]) {
        try {
            SMInterface h = (SMInterface) LocateRegistry.getRegistry(6666).lookup("Client");
            Client c = new Client();
            h.NewClient((CInterface) c);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        h.TerminateClient(c.Id);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Client is being terminated!");
                }
            });

            Scanner sc = new Scanner(System.in);
            String message;
            String response;
            String[] splited;
            while(true) {
                System.out.println("1 - Index link (1 <url>)\n" +
                        "2 - Search (2 <words>)\n" +
                        "3 - Search pages (3 <url>)\n" +
                        "4 - Administration page (login required)"
                );
                System.out.print("> ");
                message = sc.nextLine();
                splited = message.split(" ");
                if (splited[0].equals("1")) {
                    response = h.IndexUrl(splited[1]);
                } else if (splited[0].equals("2")) {
                    String[] aux = new String[splited.length - 1];
                    for (int i = 1; i < splited.length; i++) {
                        aux[i-1] = splited[i];
                    }
                    response = h.SearchLinks(aux);
                } else if (splited[0].equals("3")) {
                    response = h.SearchPointers(splited[1]);
                } else if (splited[0].equals("4")) {
                    response = "Active Downloaders: \n";
                    if (c.infoDownloaders.size() == 0){
                        response += "There are no active Downloaders\n\n";
                    } else {
                        for (Map.Entry<Integer, String[]> entry : c.infoDownloaders.entrySet()) {
                            response += "ID: " + entry.getKey() + " IP ADDRESS: " + entry.getValue()[0] + "\n";
                        }
                        response += "\n";
                    }


                    response += "Active Barrels: \n";
                    if (c.infoBarrels.size() == 0){
                        response += "There are no active Barrels\n\n";
                    } else {
                        for (Map.Entry<Integer, String[]> entry : c.infoBarrels.entrySet()) {
                            response += "ID: " + entry.getKey() + " IP ADDRESS: " + entry.getValue()[0] + "\n";
                        }
                        response += "\n";
                    }

                }else {
                    response = "Command not found";
                }

                System.out.println(response);

            }
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
            e.printStackTrace();
        }
    }
}
