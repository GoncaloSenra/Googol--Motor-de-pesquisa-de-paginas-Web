
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.StringTokenizer;

public class SearchModule extends UnicastRemoteObject implements SMInterface {

    public SearchModule() throws RemoteException {
        super();
    }

    public String sayHello() throws RemoteException {
        System.out.println("print do lado do servidor...!.");

        return "Hello, World!";
    }
    public static void main(String[] args) {
        try {
            SearchModule h = new SearchModule();
            Registry r = LocateRegistry.createRegistry(6666);
            r.rebind("XPTO", h);
            System.out.println("Hello Server ready.");
        } catch (RemoteException re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }

        //ServerUrlList UrlList = new ServerUrlList();
        //UrlList.start();
}
