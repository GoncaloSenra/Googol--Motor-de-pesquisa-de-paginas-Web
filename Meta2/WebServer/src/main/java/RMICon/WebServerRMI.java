package RMICon;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import SearchModule.SMInterface;

public class WebServerRMI extends UnicastRemoteObject {

    private SMInterface h;

    public WebServerRMI() throws RemoteException {
        try {
            this.h = (SMInterface) LocateRegistry.getRegistry(9999).lookup("WebServer");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    public String IndexLink(String url) {

        try {
            String response = h.IndexUrl(url);
            return response;

        } catch (RemoteException r) {
            r.printStackTrace();
        }
        return null;

    }

    public String Search(String url) {

        try {
            String[] tokens = url.split(" ");
            HashMap<Integer, ArrayList<String[]>> response= h.SearchLinks(tokens , -1);




            return null;
        } catch (RemoteException r) {
            r.printStackTrace();
        }
        return null;

    }


}

