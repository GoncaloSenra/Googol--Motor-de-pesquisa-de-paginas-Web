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
import com.googol.WebServer.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

public class WebServerRMI extends UnicastRemoteObject implements WSInterface {

    private SMInterface h;

    public SMInterface getH() {
        return h;
    }

    private HashMap<Integer, String[]> infoDownloaders;
    private HashMap<Integer, String[]> infoBarrels;
    private ArrayList<String[]> topSearches;

    public HashMap<Integer, String[]> getInfoDownloaders() {
        return infoDownloaders;
    }

    public HashMap<Integer, String[]> getInfoBarrels() {
        return infoBarrels;
    }

    public ArrayList<String[]> getTopSearches() {
        return topSearches;
    }

    /*
     *   Funções para dar update de todas as informações do painel de administração
     * */

    public void UpadateDownloaders(HashMap<Integer, String[]> info) throws RemoteException{
        this.infoDownloaders = info;
        System.out.println("BURROS E LIMITADOS");
    }

    public void UpadateBarrels(HashMap<Integer, String[]> info) throws RemoteException {
        this.infoBarrels = info;
        System.out.println("BURROS E LIMITADOS");

    }
    public void UpadateTopSearches(ArrayList<String[]> info) throws RemoteException{
        this.topSearches = info;

        for (String[] str : info) {
            System.out.println(str[0] + " - " + str[1]);
        }
        System.out.println("BURROS E LIMITADOS");
    }

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

