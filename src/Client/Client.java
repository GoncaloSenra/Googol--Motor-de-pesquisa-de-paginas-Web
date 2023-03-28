
package Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import SearchModule.SMInterface;

public class Client extends UnicastRemoteObject implements CInterface {
    private int Id;
    private HashMap<Integer, String[]> infoDownloaders;
    private HashMap<Integer, String[]> infoBarrels;
    private ArrayList<String[]> topSearches;

    public void UpadateDownloaders(HashMap<Integer, String[]> info) throws RemoteException{
        this.infoDownloaders = info;
    }
    public void UpadateBarrels(HashMap<Integer, String[]> info) throws RemoteException{
        this.infoBarrels = info;
    }
    public void UpadateTopSearches(ArrayList<String[]> info) throws RemoteException{
        this.topSearches = info;
    }

    public Client() throws RemoteException {
        super();
        this.infoDownloaders = new HashMap<>();
        this.infoBarrels = new HashMap<>();
        this.topSearches = new ArrayList<>();
    }

    public static void main(String args[]) {
        while(true) {
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
                String response = "";
                String[] splited;
                String auxLogin = "";
                String auxRegistry = "";
                boolean logged = false;
                while (true) {
                    System.out.println("1 - Index link (1 <url>)\n" +
                            "2 - Search (2 <words>)\n" +
                            "3 - Search pages (3 <url>)\n" +
                            "4 - Administration page (login required)\n" +
                            "5 - Login (5 <username password>)\n" +
                            "6 - Registry (6 <username password>)\n" +
                            "7 - Logout"
                    );
                    System.out.print("> ");
                    message = sc.nextLine();
                    splited = message.split(" ");
                    if (splited[0].equals("1")) {
                        response = h.IndexUrl(splited[1]);
                    } else if (splited[0].equals("2")) {
                        String[] aux = new String[splited.length - 1];
                        for (int i = 1; i < splited.length; i++) {
                            aux[i - 1] = splited[i];
                        }
                        HashMap<Integer, String> opt = new HashMap<>();
                        String mes = "";
                        opt = h.SearchLinks(aux, -1);
                        if (opt.get(0) != null) {
                            response = opt.get(0);
                        } else if (opt.get(1) != null) {
                            response = opt.get(1);
                        } else {
                            int pages = 0;
                            for (Map.Entry<Integer, String> entry : opt.entrySet()) {
                                pages = entry.getKey();
                                mes = entry.getValue();
                            }
                            System.out.println(mes);
                            System.out.println("Press (n) for the next page, and any key to exit");
                            String action = sc.nextLine();
                            if (action.equals("n")) {
                                for (int i = 1; i < pages; i++) {
                                    opt = h.SearchLinks(aux, i);
                                    for (Map.Entry<Integer, String> entry : opt.entrySet()) {
                                        mes = entry.getValue();
                                    }
                                    System.out.println(mes);
                                    if (pages != i + 1) {
                                        System.out.println("Press (n) for the next page, and any key to exit");
                                        action = sc.nextLine();
                                        if (!action.equals("n")) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (splited[0].equals("3")) {
                        if (logged) {
                            response = h.SearchPointers(splited[1]);
                        } else {
                            response = "You need to be logged!\n";
                        }
                    } else if (splited[0].equals("4")) {

                        // DOWNLOADERS
                        response = "Active Downloaders: \n";
                        if (c.infoDownloaders.size() == 0) {
                            response += "There are no active Downloaders\n\n";
                        } else {
                            for (Map.Entry<Integer, String[]> entry : c.infoDownloaders.entrySet()) {
                                response += "ID: " + entry.getKey() + " IP ADDRESS: " + entry.getValue()[0] + "\n";
                            }
                            response += "\n";
                        }

                        // BARRELS
                        response += "Active Barrels: \n";
                        if (c.infoBarrels.size() == 0) {
                            response += "There are no active Barrels\n\n";
                        } else {
                            for (Map.Entry<Integer, String[]> entry : c.infoBarrels.entrySet()) {
                                response += "ID: " + entry.getKey() + " IP ADDRESS: " + entry.getValue()[0] + "\n";
                            }
                            response += "\n";
                        }

                        // TOP SEARCHES
                        response += "Top 10 Searches: \n";
                        if (c.topSearches.size() == 0) {
                            response += "No searches, no party!!!\n\n";
                        } else {
                            for (int i = 0; i < c.topSearches.size(); i++) {
                                response += (i + 1) + " - " + c.topSearches.get(i)[0] + " (" + c.topSearches.get(i)[1] + " search(es))\n";
                            }
                            response += "\n";
                        }

                    } else if (splited[0].equals("5")) {
                        if (!logged) {
                            auxLogin = h.log(splited[1], splited[2]);
                            response = auxLogin;

                            if (auxLogin.equals("Login successful!")) {
                                logged = true;
                            }
                        } else {
                            response = "Already logged in!\n";
                        }
                    } else if (splited[0].equals("6")) {
                        if (!logged) {
                            auxRegistry = h.regist(splited[1], splited[2]);
                            response = auxRegistry;
                        } else {
                            response = "Already logged in!\n";
                        }

                    } else if (splited[0].equals("7")) {
                        response = "Logout successful!\n";
                    } else {
                        response = "Command not found\n";
                    }

                    System.out.println(response);

                }
            } catch (Exception e) {
                System.out.println("Exception in main: " + e);
            }
        }
    }
}
