
package SearchModule;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
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

import Downloader.DInterface;
import StorageBarrel.SBInterface;
import Client.CInterface;
import RMICon.WSInterface;
import com.googol.WebServer.Message;

public class SearchModule extends UnicastRemoteObject implements SMInterface {

    private static int serversocket = 6000;
    public static ArrayList<SBInterface> barrels = new ArrayList<>();
    public static ArrayList<Integer> activeBarrels = new ArrayList<>();
    public static ArrayList<DInterface> downloaders = new ArrayList<>();
    public static ArrayList<Integer> activeDownloaders = new ArrayList<>();
    public static ArrayList<CInterface> clients = new ArrayList<>();
    public static WSInterface webserver;
    public HashMap<Integer, String[]> infoDownloaders = new HashMap<>();
    public HashMap<Integer, String[]> infoBarrels = new HashMap<>();
    public int chooseBarrel;
    public int packetID;
    public HashMap<Integer, URL> packets = new HashMap<>();

    public SearchModule() throws RemoteException {
        super();
        this.chooseBarrel = 0;
        this.packetID = 0;
    }

    public void BarrelBackup(URL temp) throws RemoteException {
        int numPacket = temp.getPacket();
        this.packets.put(numPacket, temp);
    }

    public int getPacket() throws RemoteException{
        return packetID;
    }

    public void increasePacket(URL link) throws RemoteException{
        packets.put(packetID, link);
        this.packetID++;
    }

    /*
    *   Funções que quando chamadas adicionam interface ao Array de interfaces e devolvem o ID ao programa que chamou a função
    * */
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
        if (webserver != null)
            webserver.UpadateBarrels(infoBarrels);
        //UpdateTopWords();

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
        if (webserver != null)
            webserver.UpadateDownloaders(infoDownloaders);

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

    public void NewWebServer(WSInterface Iwebserver) throws RemoteException {
        int id;
        webserver = Iwebserver;


        System.out.println("NEW WEBSERVER");

        webserver.UpadateDownloaders(infoDownloaders);

        webserver.UpadateBarrels(infoBarrels);


        UpdateTopWords();
    }

    /*
    *   Funções que terminam programas de forma segura (removendo as suas interfaces do Array de interfaces)
    * */
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

        if(webserver != null)
            webserver.UpadateBarrels(infoBarrels);
    }

    public void TerminateDownloader(int id) throws RemoteException {
        activeDownloaders.set(id, 0);
        infoDownloaders.remove(id);
        for (CInterface ci: clients){
            ci.UpadateDownloaders(infoDownloaders);
        }
        if(webserver != null)
            webserver.UpadateDownloaders(infoDownloaders);
    }

    public void TerminateClient(int id) throws RemoteException {
        clients.remove(id);
    }

    // Função que pesquisa por links que apontem para o link introduzido pelo utilizador
    public HashSet<String[]> SearchPointers(String link) throws RemoteException {

        String message = "";
        HashSet<String[]> aux;

        if (!activeBarrels.contains(1)){
            aux = new HashSet<>();
            String[] aux2 = {"Currently there are no barrels available!", "-1", ""};
            aux.add(aux2);
            return aux;
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
            aux = new HashSet<>();
            String[] aux2 = {"Links not found!", "-1", ""};
            aux.add(aux2);
            return aux;
        }

        return aux;
    }


    // Função que pesquisa por urls que tenham todas as palavras inseridas pelo utilizador
    public HashMap<Integer, ArrayList<String[]>> SearchLinks(String[] words, int group) throws RemoteException {

        String message = "";
        ArrayList<String[]> aux = new ArrayList<>();
        HashMap<Integer, ArrayList<String[]>> map = new HashMap<>();

        if (!activeBarrels.contains(1)){
            String [] mes = new String[3];
            mes[0] = "Currently there are no barrels available!\n";
            aux.add(mes);
            map.put(0, aux);
            return map;
        }

        int aux2 = 0;

        if (group == -1){
            aux2 = -1;
            group = 0;
            chooseBarrel++;
            System.out.println(chooseBarrel);
            if (chooseBarrel == activeBarrels.toArray().length){
                chooseBarrel = 0;
            }
            aux = AuxSearchLinks(words, 1);
        } else {
            aux = AuxSearchLinks(words, 0);
        }


        if (aux2 == -1){
            UpdateTopWords();
        }

        if (aux == null) {
            String [] mes = new String[3];
            mes[0] =  "Links not found!\n";
            aux = new ArrayList<>();
            aux.add(mes);
            map.put(0, aux);
            return map;
        } else {
            int temp = 0;
            if (aux.size() > (group * 10) + 10) {
                temp = (group * 10) + 10;
            } else {
                temp = aux.size();
            }
            ArrayList<String[]> temp2 = new ArrayList<>();
            for (int i = group * 10; i < temp; i++) {
                temp2.add(aux.get(i));
            }
            double pag = aux.size() / 10;
            int pages = (int) Math.ceil(pag) + 1;
            map.put(pages, temp2);
        }


        return map;
    }

    // Função que ajuda a recuperação de uma pesquisa quando um barrel é terminado inesperadamente
    public ArrayList<String[]> AuxSearchLinks(String[] words, int update) {

        ArrayList<String[]> aux = new ArrayList<>();

        while (true) {
            try{
                while(true) {
                    if (activeBarrels.get(chooseBarrel) == 1){
                        aux = barrels.get(chooseBarrel).SearchWords(words, update);
                        System.out.println("Barrel chosen: " + chooseBarrel);
                        break;
                    } else {
                        chooseBarrel++;
                        if (chooseBarrel == activeBarrels.size()){
                            chooseBarrel = 0;
                        }
                    }
                }
                break;
            } catch (RemoteException e) {
                System.out.println("Choosing another barrel!");
            }
        }

        return aux;

    }

    // Função que auxilia o Barril que quando inicia envia logo as TopSearches para o cliente
    public void UpdateTopWordsAfterReading() throws RemoteException{
        UpdateTopWords();
    }

    // Função que atualiza as palavras mais pesquisadas
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
        try {
            if (webserver != null)
                webserver.UpadateTopSearches(final_counter);
        } catch (RemoteException e) {
            System.out.println("Remote ex in SM.UpdateTopSearches " + e.getMessage());
        }

    }

    // Função que envia um link dado pelo utilizador para a thread ServerUrlList (Queue) por TCP
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

    // Função que envia as credências de login para os StorageBarrels e recebe a informação de que o utilizador fez login ou não
    public String log(String username, String password) throws RemoteException{
        String message = "";
        int aux;

        if (!activeBarrels.contains(1)){
            return "Currently there are no barrels available!";
        }

        while(true) {
            if (activeBarrels.get(chooseBarrel) == 1){
                aux = barrels.get(chooseBarrel).login(username, password);
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

        if (aux == 1) {
            return "Login successful!";
        } else {
            return "Login failed!";
        }
    }


    // Função que envia as credências de registo para os StorageBarrels e recebe a informação de que o utilizador fez registo ou não
    public String regist(String username, String password) throws RemoteException {
        String message = "";
        int aux = 0;

        if (!activeBarrels.contains(1)){
            return "Currently there are no barrels available!";
        }

        for (SBInterface sb : barrels) {
            aux = sb.registry(username, password);
        }

        if (aux == 1) {
            return "Registry successful!";
        } else {
            return "Registry failed!";
        }

    }

    public static void main(String[] args) {
        try {

            //Inicia a thread da Queue e cria os registrys necessários para a comunicação RMI
            ServerUrlList UrlList = new ServerUrlList();
            UrlList.start();

            SearchModule h = new SearchModule();

            Registry c = LocateRegistry.createRegistry(6666);
            c.rebind("Client", h);

            Registry b = LocateRegistry.createRegistry(7777);
            b.rebind("Barrel", h);

            Registry d = LocateRegistry.createRegistry(8888);
            d.rebind("Downloader", h);

            Registry s = LocateRegistry.createRegistry(9999);
            s.rebind("WebServer", h);

            System.out.println("Search Module Server ready!");

            // Thread que quando deteta SIGINT termina o Search Module, Downloaders e Barrels, e interrompe a thread da Queue
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {

                    try {
                        String path = "src/main/java/SearchModule/queue.obj";
                        //System.out.println(path);
                        File file = new File(path);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream fileOut = new FileOutputStream(file, false);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(UrlList.getUrls());
                        out.close();
                        fileOut.close();

                    } catch (FileNotFoundException e) {
                        System.out.println("FOS: " + e);
                    } catch (IOException e) {
                        System.out.println("OOS: " + e);
                    }
                    UrlList.interrupt();


                    try {


                        while (!downloaders.isEmpty()) {
                            downloaders.get(0).ExitDownloaders();
                            downloaders.remove(0);
                            activeDownloaders.remove(0);
                        }
                        while (!barrels.isEmpty()) {
                            barrels.get(0).ExitBarrels();
                            barrels.remove(0);
                            activeBarrels.remove(0);
                        }

                        try {
                            Thread.sleep(5000);
                        } catch (java.lang.InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                        UrlList.interrupt();

                    } catch (RemoteException e) {
                        System.out.println("Exception terminating SM: " + e.getMessage());;
                    }


                    System.out.println("Search Module is being terminated!");
                }
            });

        } catch (RemoteException re) {
            System.out.println("Exception in SM.main: " + re);
        }
    }

}
