
import java.rmi.registry.LocateRegistry;
public class Client {

    public static void main(String args[]) {
        try {
            SMInterface h = (SMInterface) LocateRegistry.getRegistry(6666).lookup("XPTO");

            String message = h.sayHello();
            System.out.println("HelloClient: " + message);
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
            e.printStackTrace();
        }
    }
}
