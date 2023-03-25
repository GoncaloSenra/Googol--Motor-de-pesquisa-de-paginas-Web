package StorageBarrel;

import java.rmi.*;
import java.util.HashMap;
import java.util.HashSet;

public interface SBInterface extends Remote {

    public HashMap<String, String[]> SearchWords(String[] words) throws java.rmi.RemoteException;

    HashSet<String[]> SearchPointerLinks(String url) throws java.rmi.RemoteException;

}
