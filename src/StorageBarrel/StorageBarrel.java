
package StorageBarrel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import SearchModule.SMInterface;

import javax.print.attribute.HashPrintServiceAttributeSet;


public class StorageBarrel extends Thread implements SBInterface, Serializable{

    private HashMap<String, HashSet<String>> index;

    private int Id;

    public StorageBarrel(int id) throws RemoteException{
        super();
        this.index = new HashMap<>();
        this.Id = id;
    }

    public HashSet<String> SearchWords(String[] words){
        ArrayList<HashSet<String>> set_links = new ArrayList<>();
        for (String word : words) {
            for (Map.Entry<String, HashSet<String>> map : index.entrySet()) {
                if (map.getKey().equals(word)) {
                    set_links.add(map.getValue());
                }
            }
        }

        HashSet<String> common = new HashSet<>(set_links.get(0));
        for (HashSet<String> links: set_links){
            common.retainAll(links);
        }

        return common;
    }

    public static void main(String[] args) {

        try {
            SMInterface sm = (SMInterface) LocateRegistry.getRegistry(7777).lookup("Barrel");
            StorageBarrel barrel = new StorageBarrel(Integer.parseInt(args[0]));
            sm.NewBarrel((SBInterface) barrel, Integer.toString(barrel.Id));
            barrel.start();
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Exception in SB.main: " + e);
        }
    }

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
            //System.out.println("HashMap has been serialized and stored in hashmap.ser");

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
