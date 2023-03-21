
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class Client {

    public static void main(String args[]) {
        try {
            SMInterface h = (SMInterface) LocateRegistry.getRegistry(6666).lookup("XPTO");
            Scanner sc = new Scanner(System.in);
            String message;
            String response;
            String[] splited;
            while(true) {
                message = sc.nextLine();
                splited = message.split(" ");
                if (splited[0].equals("index")) {
                    response = h.IndexUrl(splited[1]);
                } else {
                    response = "Command not found";
                }


                System.out.println(response);

            }
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
            e.printStackTrace();
        }
    }
}
