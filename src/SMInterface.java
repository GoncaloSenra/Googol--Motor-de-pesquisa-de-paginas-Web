
import java.rmi.*;

public interface SMInterface extends Remote{
    public String sayHello() throws java.rmi.RemoteException;
}
