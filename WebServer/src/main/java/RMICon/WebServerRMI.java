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

public class WebServerRMI extends UnicastRemoteObject implements WSInterface {

    private SMInterface h;

    /**
     * SMInterface Getter
     * @return
     */
    public SMInterface getH() {
        return h;
    }

    private HashMap<Integer, String[]> infoDownloaders;
    private HashMap<Integer, String[]> infoBarrels;
    private ArrayList<String[]> topSearches;

    /**
     * InfoDownloaders Getter
     * @return
     */
    public HashMap<Integer, String[]> getInfoDownloaders() {
        return infoDownloaders;
    }

    /**
     * InfoBarrels Getter
     * @return
     */
    public HashMap<Integer, String[]> getInfoBarrels() {
        return infoBarrels;
    }

    /**
     * TopSearches Getter
     * @return
     */
    public ArrayList<String[]> getTopSearches() {
        return topSearches;
    }

    /**
     * Função chamada remotamente pelo SearchModule para atualizar a informação dos downloaders
     * @param info
     * @throws RemoteException
     */
    public void UpadateDownloaders(HashMap<Integer, String[]> info) throws RemoteException{
        this.infoDownloaders = info;
    }

    /**
     * Função chamada remotamente pelo SearchModule para atualizar a informação dos barrels
     * @param info
     * @throws RemoteException
     */
    public void UpadateBarrels(HashMap<Integer, String[]> info) throws RemoteException {
        this.infoBarrels = info;
    }

    /**
     * Função chamada remotamente pelo SearchModule para atualizar a informação das TopSearches
     * @param info
     * @throws RemoteException
     */
    public void UpadateTopSearches(ArrayList<String[]> info) throws RemoteException{
        this.topSearches = info;

        for (String[] str : info) {
            System.out.println(str[0] + " - " + str[1]);
        }
    }

    /**
     * Construtor
     * @throws RemoteException
     */
    public WebServerRMI() throws RemoteException {
        System.out.println("NEW REGISTRY");
        try {
            this.h = (SMInterface) LocateRegistry.getRegistry(9999).lookup("WebServer");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Função que comunica com o Search Module para indexar um link
     * @param url
     * @return
     */
    public String IndexLink(String url) {

        try {
            String response = h.IndexUrl(url);
            return response;

        } catch (RemoteException r) {
            r.printStackTrace();
        }
        return null;

    }

    /**
     * Função que comunica com o Search Module para fazer uma pesaquisa
     * @param url
     * @param page
     * @return
     */
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

    /**
     * Função que comunica com o Search Module para procurar links que apontem para um link
     * @param url
     * @return
     */
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

