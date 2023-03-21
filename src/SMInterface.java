
import java.rmi.*;

public interface SMInterface extends Remote{
    public String IndexUrl(String url) throws java.rmi.RemoteException;
}
