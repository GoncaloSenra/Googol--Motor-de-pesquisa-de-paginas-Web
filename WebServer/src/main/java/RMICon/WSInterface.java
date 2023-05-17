package RMICon;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;

public interface WSInterface extends Remote {

    public void UpadateDownloaders(HashMap<Integer, String[]> info) throws java.rmi.RemoteException;
    public void UpadateBarrels(HashMap<Integer, String[]> info) throws java.rmi.RemoteException;
    public void UpadateTopSearches(ArrayList<String[]> info) throws java.rmi.RemoteException;

}
