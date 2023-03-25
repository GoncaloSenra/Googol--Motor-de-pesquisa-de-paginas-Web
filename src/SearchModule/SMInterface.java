
package SearchModule;

import java.rmi.*;
import java.util.HashSet;

import StorageBarrel.SBInterface;

public interface SMInterface extends Remote{
    public String IndexUrl(String url) throws java.rmi.RemoteException;

    public int NewBarrel(SBInterface Ibarrel) throws java.rmi.RemoteException;

    public void TerminateBarrel(int id) throws java.rmi.RemoteException;

    public String SearchLinks(String[] words) throws java.rmi.RemoteException;

    public String SearchPointers(String link) throws java.rmi.RemoteException;

}
