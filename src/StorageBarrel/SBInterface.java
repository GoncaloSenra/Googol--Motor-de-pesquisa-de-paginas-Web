package StorageBarrel;

import java.rmi.*;
import java.util.HashSet;

public interface SBInterface extends Remote {

    public HashSet<String> SearchWords(String[] words) throws java.rmi.RemoteException;

}
