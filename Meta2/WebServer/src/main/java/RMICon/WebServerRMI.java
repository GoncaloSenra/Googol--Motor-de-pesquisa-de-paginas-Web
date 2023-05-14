package RMICon;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import SearchModule.SMInterface;

public class WebServerRMI extends UnicastRemoteObject {

    private SMInterface h;

    public WebServerRMI() throws RemoteException {
        System.out.println("NEW REGISTRY");
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

    public HashMap<Integer, ArrayList<String[]>> Search(String url, int page) {

        try {
            String[] tokens = url.split(" ");

            HashMap<Integer, ArrayList<String[]>> response;

            if (page == -1) {
                response = h.SearchLinks(tokens , page);
            } else {
                response = h.SearchLinks(tokens , page - 1);
            }

            return response;
        } catch (RemoteException r) {
            r.printStackTrace();
        }
        return null;

    }

    public HashSet<String[]> SearchPointers(String url) {

        try {
            HashSet<String[]> response = h.SearchPointers(url);

            for(String[] link : response) {
                if (link[1].equals("-1")) {
                    System.out.println("SP: " + link[0]);
                    return null;
                }
            }

            return response;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return null;
    }


}
