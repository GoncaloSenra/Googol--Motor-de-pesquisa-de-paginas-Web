
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

import StorageBarrel.StorageBarrel;


public class SearchModule extends UnicastRemoteObject implements SMInterface {

    private static int serversocket = 6000;

    private static int numBarrels = 2;

    public SearchModule() throws RemoteException {
        super();
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
            ArrayList<StorageBarrel> barrels = new ArrayList<>();
            ServerUrlList UrlList = new ServerUrlList();
            UrlList.start();

            for (int i = 0; i < numBarrels; i++) {
                StorageBarrel auxBarrel = new StorageBarrel(i);
                auxBarrel.start();
                barrels.add(auxBarrel);
            }

            SearchModule h = new SearchModule();
            Registry r = LocateRegistry.createRegistry(6666);
            r.rebind("XPTO", h);
            System.out.println("Search Module Server ready!");
        } catch (RemoteException re) {
            System.out.println("Exception in SM.main: " + re);
        }
    }

        //ServerUrlList UrlList = new ServerUrlList();
        //UrlList.start();
}
