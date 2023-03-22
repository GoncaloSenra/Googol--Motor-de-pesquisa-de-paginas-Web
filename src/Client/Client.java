
package Client;

import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import SearchModule.SMInterface;

//O SM tambem vai ter de ter um interfce do cliente para poder mandar as infos de administracao
//quando existem alteraçoes o SM chama uma funçao do cliente (RMI callback)

public class Client {

    public static void main(String args[]) {
        try {
            SMInterface h = (SMInterface) LocateRegistry.getRegistry(6666).lookup("Client");
            Scanner sc = new Scanner(System.in);
            String message;
            String response;
            String[] splited;
            while(true) {


                System.out.print("> ");
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
