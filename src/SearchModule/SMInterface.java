
package SearchModule;

import java.rmi.*;
import java.util.HashMap;
import java.util.HashSet;

import StorageBarrel.SBInterface;
import Downloader.DInterface;
import Client.CInterface;

public interface SMInterface extends Remote{
    public String IndexUrl(String url) throws java.rmi.RemoteException;
    public int NewBarrel(SBInterface Ibarrel) throws java.rmi.RemoteException;
    public int NewDownloader(DInterface Idownloader) throws java.rmi.RemoteException;
    public int NewClient(CInterface Iclient) throws java.rmi.RemoteException;
    public void TerminateBarrel(int id) throws java.rmi.RemoteException;
    public void TerminateDownloader(int id) throws java.rmi.RemoteException;
    public void TerminateClient(int id) throws java.rmi.RemoteException;
    public HashMap<Integer, String> SearchLinks(String[] words, int group) throws java.rmi.RemoteException;
    public String SearchPointers(String link) throws java.rmi.RemoteException;
    public String log(String username, String password) throws java.rmi.RemoteException;
    public String regist(String username, String password) throws java.rmi.RemoteException;
    public int getPacket() throws java.rmi.RemoteException;
    public void increasePacket(URL link) throws java.rmi.RemoteException;
    public void UpdateTopWordsAfterReading() throws java.rmi.RemoteException;
    public void BarrelBackup(URL temp) throws java.rmi.RemoteException;

}
