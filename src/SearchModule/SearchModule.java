
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
import StorageBarrel.IndexedURL;


public class SearchModule extends UnicastRemoteObject implements SMInterface {

    private static int serversocket = 6000;

    //private static int numBarrels = 2;
    public static ArrayList<SBInterface> barrels = new ArrayList<>();

    public static ArrayList<String> names = new ArrayList<>();

    public SearchModule() throws RemoteException {
        super();
    }

    public void NewBarrel(SBInterface Ibarrel, String name) throws RemoteException {
        names.add(name);
        barrels.add(Ibarrel);

        System.out.println("NEW BARREL -> " + name);
    }

    public String SearchLinks(String[] words) throws RemoteException {

        String message = "";
        HashMap<String, String[]> aux = barrels.get(0).SearchWords(words);

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
