
package SearchModule;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import Client.Client;
import Downloader.DInterface;
import StorageBarrel.SBInterface;
import Client.CInterface;


public class SearchModule extends UnicastRemoteObject implements SMInterface {

    private static int serversocket = 6000;
    public static ArrayList<SBInterface> barrels = new ArrayList<>();
    public static ArrayList<Integer> activeBarrels = new ArrayList<>();
    public static ArrayList<DInterface> downloaders = new ArrayList<>();
    public static ArrayList<Integer> activeDownloaders = new ArrayList<>();
    public static ArrayList<CInterface> clients = new ArrayList<>();
    public HashMap<Integer, String[]> infoDownloaders = new HashMap<>();
    public HashMap<Integer, String[]> infoBarrels = new HashMap<>();
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

        if (!downloaders.isEmpty()) {
            int numBarrels = 0;
            for (Integer i : activeBarrels){
                if (i == 1) {
                    numBarrels++;
                }
            }
            for(int j = 0; j < activeDownloaders.size(); j++){
                if (activeDownloaders.get(j) == 1){
                    downloaders.get(j).UpdateNumBarrels(numBarrels);
                }
            }
        }

        String[] info = new String[2];
        try{
            info[0] = RemoteServer.getClientHost();
            info[1] = "";
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        infoBarrels.put(id, info);
        for (CInterface ci: clients){
            ci.UpadateBarrels(infoBarrels);
        }

        System.out.println("NEW BARREL -> " + id);
        return id;
    }

    public int NewDownloader(DInterface Idownloader) throws  RemoteException{
        int id = 0;
        boolean all_active = true;
        if (activeDownloaders.isEmpty()){
            activeDownloaders.add(1);
            downloaders.add(Idownloader);
            id = 0;
        } else {
            for (int i = 0; i < activeDownloaders.size(); i++){
                if (activeDownloaders.get(i) == 0) {
                    activeDownloaders.set(i, 1);
                    downloaders.set(i, Idownloader);
                    all_active = false;
                    id = i;
                    break;
                }
            }
            if (all_active) {
                activeDownloaders.add(1);
                downloaders.add(Idownloader);
                id = activeDownloaders.size() - 1;
            }

        }

        int numBarrels = 0;
        for (Integer i : activeBarrels){
            if (i == 1) {
                numBarrels++;
            }
        }
        for(int j = 0; j < activeDownloaders.size(); j++){
            if (activeDownloaders.get(j) == 1){
                downloaders.get(j).UpdateNumBarrels(numBarrels);
            }
        }

        String[] info = new String[2];
        try{
            info[0] = RemoteServer.getClientHost();
            info[1] = "";
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        infoDownloaders.put(id, info);
        for (CInterface ci: clients){
            ci.UpadateDownloaders(infoDownloaders);
        }

        System.out.println("NEW DOWNLOADER -> " + id);
        return id;
    }

    public int NewClient(CInterface Iclient) throws RemoteException {
        int id;
        clients.add(Iclient);
        id = clients.size() - 1;

        System.out.println("NEW CLIENT -> " + id);

        for (CInterface ci: clients){
            ci.UpadateDownloaders(infoDownloaders);
        }
        for (CInterface ci: clients){
            ci.UpadateBarrels(infoBarrels);
        }

        UpdateTopWords();

        return id;
    }

    public void TerminateBarrel(int id) throws RemoteException {
        activeBarrels.set(id, 0);
        int numBarrels = 0;
        for (Integer i : activeBarrels){
            if (i == 1) {
                numBarrels++;
            }
        }
        for(int j = 0; j < activeDownloaders.size(); j++){
            if (activeDownloaders.get(j) == 1){
                downloaders.get(j).UpdateNumBarrels(numBarrels);
            }
        }

        infoBarrels.remove(id);
        for (CInterface ci: clients){
            ci.UpadateBarrels(infoBarrels);
        }
    }

    public void TerminateDownloader(int id) throws RemoteException {
        activeDownloaders.set(id, 0);
        infoDownloaders.remove(id);
        for (CInterface ci: clients){
            ci.UpadateDownloaders(infoDownloaders);
        }
    }

    public void TerminateClient(int id) throws RemoteException {
        clients.remove(id);
    }

    public String SearchPointers(String link) throws RemoteException {

        String message = "";
        HashSet<String[]> aux;

        if (!activeBarrels.contains(1)){
            return "Currently there are no barrels available!";
        }

        while(true) {
            if (activeBarrels.get(chooseBarrel) == 1){
                aux = barrels.get(chooseBarrel).SearchPointerLinks(link);
                chooseBarrel++;
                if (chooseBarrel == activeBarrels.toArray().length){
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

    public HashMap<Integer, String> SearchLinks(String[] words, int group) throws RemoteException {

        String message = "";
        ArrayList<String[]> aux;
        HashMap <Integer, String> map = new HashMap<>();

        if (!activeBarrels.contains(1)){
            map.put(0, "Currently there are no barrels available!");
            return map;
        }

        if (group == -1){
            UpdateTopWords();
            group = 0;
        }

        while(true) {
            if (activeBarrels.get(chooseBarrel) == 1){
                aux = barrels.get(chooseBarrel).SearchWords(words);
                chooseBarrel++;
                System.out.println(chooseBarrel);
                if (chooseBarrel == activeBarrels.toArray().length){
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
            map.put(0, "Links not found!\n");
            return map;
        } else {
            int temp = 0;
            if (aux.size() > (group * 10) + 10) {
                temp = (group * 10) + 10;
            } else {
                temp = aux.size();
            }
            for (int i = group * 10; i < temp; i++) {
                message += aux.get(i)[1] + " - " + aux.get(i)[0] + "\n\""+ aux.get(i)[2] +"\"\n";
            }
            double pag = aux.size() / 10;
            int pages = (int) Math.ceil(pag) + 1;
            map.put(pages, message);
        }


        return map;
    }

    public void UpdateTopWords(){

        ArrayList<HashMap<String, Integer>> all_words_counters = new ArrayList<>();
        HashMap<String, Integer> result = new HashMap<>();

        for (int i = 0; i < activeBarrels.size(); i++) {
            if (activeBarrels.get(i) == 1) {
                try {
                    all_words_counters.add(barrels.get(i).getWord_counter());
                } catch (RemoteException e) {
                    System.out.println("Remote ex in SM.UpdateTopSearches " + e.getMessage());
                }
            }
        }

        for (Map<String, Integer> map : all_words_counters) {
            map.forEach((key, value) ->
                    result.merge(key, value, Integer::sum)
            );
        }

        ArrayList<String[]> final_counter = new ArrayList<>();
        String[] best = new String[2];
        best[0] = "";
        best[1] = "0";

        int num = 0;
        if (result.size() < 10){
            num = result.size();
        } else {
            num = 10;
        }

        for (int i = 0; i < num; i++) {
            for (Map.Entry<String, Integer> entry : result.entrySet()) {
                if (entry.getValue() > Integer.parseInt(best[1])) {
                    best[0] = entry.getKey();
                    best[1] = Integer.toString(entry.getValue());
                }
            }
            result.remove(best[0]);
            final_counter.add(best.clone());
            best[0] = "";
            best[1] = "0";
        }

        for (CInterface c: clients) {
            try {
                c.UpadateTopSearches(final_counter);
            } catch (RemoteException e) {
                System.out.println("Remote ex in SM.UpdateTopSearches " + e.getMessage());
            }
        }
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

            Registry d = LocateRegistry.createRegistry(8888);
            d.rebind("Downloader", h);

            System.out.println("Search Module Server ready!");
        } catch (RemoteException re) {
            System.out.println("Exception in SM.main: " + re);
        }
    }

}
