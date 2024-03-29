
package Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import SearchModule.SMInterface;

public class Client extends UnicastRemoteObject implements CInterface {
    private int Id;
    private HashMap<Integer, String[]> infoDownloaders;
    private HashMap<Integer, String[]> infoBarrels;
    private ArrayList<String[]> topSearches;

    /*
    *   Funções para dar update de todas as informações do painel de administração
    * */
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

                // Thread que termina o cliente de forma segura
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            h.TerminateClient(c.Id);
                        } catch (RemoteException e) {
                            System.out.println("Search Module not active!");
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

                    // Indexar Link
                    if (splited[0].equals("1")) {
                        response = h.IndexUrl(splited[1]);
                    }
                    // Procura por urls que tenham todas as palavras inseridas pelo utilizador
                    else if (splited[0].equals("2")) {

                        String[] aux = new String[splited.length - 1];
                        for (int i = 1; i < splited.length; i++) {
                            aux[i - 1] = splited[i];
                        }

                        HashMap<Integer, ArrayList<String[]>> opt = new HashMap<>();
                        ArrayList<String[]> mes = new ArrayList<>();
                        // Pesquisa a primeira página (10 links) (group = -1 -> dá update ao contador de palavras)
                        opt = h.SearchLinks(aux, -1);

                        // ERROR
                        if (opt.get(0) != null) {
                            response = opt.get(0).get(0)[0];
                        } else {
                            int pages = 0;
                            for (Map.Entry<Integer, ArrayList<String[]>> entry : opt.entrySet()) {
                                pages = entry.getKey();
                                mes = entry.getValue();
                            }

                            for (String[] link : mes) {
                                System.out.println(link[1] + " - " + link[0] + "\n" + link[2] + "\n\n");
                            }

                            System.out.println("Pages: 1/" + pages);
                            System.out.println("Press (n) for the next page, or any key to exit");
                            String action = sc.nextLine();
                            if (action.equals("n")) {
                                // Percorre todas as páginas (10 em 10)
                                int i = 1;
                                while (true) {
                                    opt = h.SearchLinks(aux, i);
                                    for (Map.Entry<Integer, ArrayList<String[]>> entry : opt.entrySet()) {
                                        mes = entry.getValue();
                                    }

                                    for (String[] link : mes) {
                                        System.out.println(link[1] + " - " + link[0] + "\n" + link[2] + "\n\n");
                                    }

                                    System.out.println("Pages: " + (i + 1) + "/" + (pages));

                                    if (i < pages - 1 && i > 0) {
                                        System.out.println("Press (n) for the next page, (p) for the previous page, or any key to exit");
                                        action = sc.nextLine();
                                        if (action.equals("n")) {
                                            i++;
                                        } else if (action.equals("p")) {
                                            i--;
                                        } else {
                                            break;
                                        }
                                    } else if (i == pages - 1) {
                                        System.out.println("Press (p) for the previous page, or any key to exit");
                                        action = sc.nextLine();
                                        if (action.equals("p")) {
                                            i--;
                                        } else {
                                            break;
                                        }
                                    } else if (i == 0) {
                                        System.out.println("Press (n) for the next page, or any key to exit");
                                        action = sc.nextLine();
                                        if (action.equals("n")) {
                                            i++;
                                        } else {
                                            break;
                                        }
                                    }

                                }

                                /*for (i = 1; i < pages; i++) {
                                    opt = h.SearchLinks(aux, i);
                                    for (Map.Entry<Integer, String> entry : opt.entrySet()) {
                                        mes = entry.getValue();
                                    }
                                    System.out.println(mes);
                                    if (pages != i + 1) {
                                        System.out.println("Press (n) for the next page, or any key to exit");
                                        action = sc.nextLine();
                                        if (!action.equals("n")) {
                                            break;
                                        }
                                    }
                                }*/
                            }
                            response = "";
                        }
                    }
                    // Pesquisa por links que apontem para o link introduzido pelo utilizador
                    else if (splited[0].equals("3")) {
                        if (logged) {
                            HashSet<String[]> links= h.SearchPointers(splited[1]);

                            for (String[] link : links) {
                                if (link[1].equals("-1")) {
                                    System.out.println(link[0]);
                                    break;
                                } else {
                                    System.out.println(link[1] + " - " + link[0] + "\n" + link[2] + "\n\n");
                                }
                            }

                        } else {
                            response = "You need to be logged!\n";
                        }
                    }
                    // Mostra página de administração
                    else if (splited[0].equals("4")) {

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

                    }
                    // Login
                    else if (splited[0].equals("5")) {
                        if (!logged) {
                            auxLogin = h.log(splited[1], splited[2]);
                            response = auxLogin;

                            if (auxLogin.equals("Login successful!")) {
                                logged = true;
                            }
                        } else {
                            response = "Already logged in!\n";
                        }
                    }
                    // Registo
                    else if (splited[0].equals("6")) {
                        if (!logged) {
                            auxRegistry = h.regist(splited[1], splited[2]);
                            response = auxRegistry;
                        } else {
                            response = "Already logged in!\n";
                        }

                    }
                    // Logout
                    else if (splited[0].equals("7")) {
                        logged = false;
                        response = "Logout successful!\n";
                    } else {
                        response = "Command not found\n";
                    }

                    System.out.println(response);

                }
            } catch (RemoteException | NotBoundException e) {
                System.out.println("Trying to reconnect with the SearchModule!");
            }
        }
    }
}
