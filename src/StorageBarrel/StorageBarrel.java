
package StorageBarrel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.HashSet;


public class StorageBarrel extends Thread implements Serializable{

    private HashMap<String, HashSet<String>> index;

    private int Id;

    public StorageBarrel(int id) {
        this.index = new HashMap<>();
        this.Id = id;
    }
    /*
    public static void main(String[] args) {
        StorageBarrel barrel = new StorageBarrel();
        barrel.start();
    }
    */
    public void run() {

        HashSet<String> auxset = new HashSet<>();
        auxset.add("https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos");
        auxset.add("https://inforestudante.uc.pt/nonio/security/login.do");
        index.put("Teste", auxset);

        try {
            File file = new File("src/StorageBarrel/hashmap"+this.Id+".obj");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this.index);
            out.close();
            fileOut.close();
            System.out.println("HashMap has been serialized and stored in hashmap.ser");

        } catch (FileNotFoundException e) {
            System.out.println("FOS: " + e);
        } catch (IOException e) {
            System.out.println("OOS: " + e);
        }

    }

    /*
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;

    public static void main(String[] args) {
        StorageBarrel barrel = new StorageBarrel();
        barrel.start();
    }


    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(PORT);  // create socket and bind it
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
    */

}
