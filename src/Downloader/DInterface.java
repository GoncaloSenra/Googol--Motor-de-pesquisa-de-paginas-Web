package Downloader;

import java.rmi.Remote;

public interface DInterface extends Remote {
    public void UpdateNumBarrels(int num) throws java.rmi.RemoteException;
    public void ExitDownloaders() throws java.rmi.RemoteException;

}
