
package SearchModule;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import StorageBarrel.SBInterface;


public class SearchModule extends UnicastRemoteObject implements SMInterface {

    private static int serversocket = 6000;
    public static ArrayList<SBInterface> barrels = new ArrayList<>();
    public static ArrayList<String> names = new ArrayList<>();
    public static ArrayList<Integer> activeBarrels = new ArrayList<>();

    public int chooseBarrel;

    public SearchModule() throws RemoteException {
        super();
        this.chooseBarrel = 0;
    }

    public int NewBarrel(SBInterface Ibarrel) throws RemoteException {
        int id = 0;
        boolean all_active = true;
        if (activeBarrels.isEmpty()){
            activeBarrels.add(1);
            barrels.add(Ibarrel);
            id = 0;
        } else {
            for (int i = 0; i < activeBarrels.size(); i++){
                if (activeBarrels.get(i) == 0) {
                    activeBarrels.set(i, 1);
                    barrels.set(i, Ibarrel);
                    all_active = false;
                    id = i;
                    break;
                }
            }
            if (all_active) {
                activeBarrels.add(1);
                barrels.add(Ibarrel);
                id = activeBarrels.size() - 1;
            }

        }

        System.out.println("NEW BARREL -> " + id);
        return id;
    }

    public void TerminateBarrel(int id) throws RemoteException {
        activeBarrels.set(id, 0);
    }

    public String SearchPointers(String link) throws RemoteException {

        String message = "";
        HashSet<String[]> aux;

        while(true) {
            if (activeBarrels.get(chooseBarrel) == 1){
                aux = barrels.get(chooseBarrel).SearchPointerLinks(link);
                chooseBarrel++;
                if (chooseBarrel == activeBarrels.size()){
                    chooseBarrel = 0;
                }
                break;
            } else {
                chooseBarrel++;
                if (chooseBarrel == activeBarrels.size()){
                    chooseBarrel = 0;
                }
            }
        }


        if (aux == null) {
            return "Links not found!\n";
        } else {
            for (String[] url: aux) {
                message += url[1] + " - " + url[0] + "\n\""+ url[2] +"\"\n";
            }
        }

        return message;
    }

    public String SearchLinks(String[] words) throws RemoteException {

        String message = "";
        HashMap<String, String[]> aux = barrels.get(chooseBarrel).SearchWords(words);

        while(true) {
            if (activeBarrels.get(chooseBarrel) == 1){
                aux = barrels.get(chooseBarrel).SearchWords(words);
                chooseBarrel++;
                if (chooseBarrel == activeBarrels.size()){
                    chooseBarrel = 0;
                }
                break;
            } else {
                chooseBarrel++;
                if (chooseBarrel == activeBarrels.size()){
                    chooseBarrel = 0;
                }
            }
        }

        chooseBarrel++;
        if (chooseBarrel == activeBarrels.size()){
            chooseBarrel = 0;
        }

        if (aux == null) {
            return "Links not found!\n";
        } else {
            for (Map.Entry<String, String[]> map: aux.entrySet()) {
                message += map.getValue()[0] + " - " + map.getKey() + "\n\""+ map.getValue()[1] +"\"\n";
            }
        }

        return message;
    }

    public String IndexUrl(String url) throws RemoteException {
        //System.out.println("print do lado do servidor...!.");
        String message = "Type | url_list; item_count | 1; item | " + url;
        String response = null;
        try (Socket s = new Socket("localhost", serversocket)) {
            //System.out.println("SOCKET=" + s);

            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            //System.out.println("SM: " +  message);
            out.writeUTF(message);

            response = in.readUTF();

            response = ("Response: " + response);


        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }

        return response;
    }


    public static void main(String[] args) {
        try {
            ServerUrlList UrlList = new ServerUrlList();
            UrlList.start();

            SearchModule h = new SearchModule();

            Registry c = LocateRegistry.createRegistry(6666);
            c.rebind("Client", h);

            Registry b = LocateRegistry.createRegistry(7777);
            b.rebind("Barrel", h);

            System.out.println("Search Module Server ready!");
        } catch (RemoteException re) {
            System.out.println("Exception in SM.main: " + re);
        }
    }

        //ServerUrlList UrlList = new ServerUrlList();
        //UrlList.start();
}
