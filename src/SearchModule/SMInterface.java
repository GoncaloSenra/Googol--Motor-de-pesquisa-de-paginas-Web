
package SearchModule;

import java.rmi.*;
import java.util.HashSet;

import StorageBarrel.SBInterface;

public interface SMInterface extends Remote{
    public String IndexUrl(String url) throws java.rmi.RemoteException;

    public void NewBarrel(SBInterface Ibarrel, String name) throws java.rmi.RemoteException;

    public String SearchLinks(String[] words) throws java.rmi.RemoteException;

}
