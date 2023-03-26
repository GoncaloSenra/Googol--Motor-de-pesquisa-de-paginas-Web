package Client;

import java.rmi.Remote;
import java.util.HashMap;

public interface CInterface extends Remote {

    public void UpadateDownloaders(HashMap<Integer, String[]> info) throws java.rmi.RemoteException;
    public void UpadateBarrels(HashMap<Integer, String[]> info) throws java.rmi.RemoteException;
    public void UpadateTopSearches(HashMap<Integer, String[]> info) throws java.rmi.RemoteException;
}
