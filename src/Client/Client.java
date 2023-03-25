
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
                System.out.println("1 - Index link (1 <url>)\n" +
                        "2 - Search (2 <words>)\n" +
                        "3 - Search pages (3 <url>)"
                );
                System.out.print("> ");
                message = sc.nextLine();
                splited = message.split(" ");
                if (splited[0].equals("1")) {
                    response = h.IndexUrl(splited[1]);
                } else if (splited[0].equals("2")) {
                    String[] aux = new String[splited.length - 1];
                    for (int i = 1; i < splited.length; i++) {
                        aux[i-1] = splited[i];
                    }
                    response = h.SearchLinks(aux);
                } else if (splited[0].equals("3")) {
                    response = h.SearchPointers(splited[1]);
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
