package StorageBarrel;

import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public interface SBInterface extends Remote {

    public ArrayList<String[]> SearchWords(String[] words) throws java.rmi.RemoteException;
    public HashSet<String[]> SearchPointerLinks(String url) throws java.rmi.RemoteException;
    public HashMap<String, Integer> getWord_counter() throws java.rmi.RemoteException;
    public int login(String username, String password) throws java.rmi.RemoteException;
    public int registry(String username, String password) throws  java.rmi.RemoteException;
}
