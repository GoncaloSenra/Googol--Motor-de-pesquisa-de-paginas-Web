package StorageBarrel;

import java.rmi.*;
import java.util.HashMap;
import java.util.HashSet;

public interface SBInterface extends Remote {

    public HashSet<String> SearchWords(String[] words) throws java.rmi.RemoteException;

    public HashMap<String, HashSet<String>> getIndex() throws java.rmi.RemoteException;

}
