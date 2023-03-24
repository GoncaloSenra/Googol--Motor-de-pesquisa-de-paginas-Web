
package StorageBarrel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import SearchModule.SMInterface;
import SearchModule.URL;

import javax.print.attribute.HashPrintServiceAttributeSet;


public class StorageBarrel extends UnicastRemoteObject implements SBInterface, Serializable {
    public HashMap<String, HashSet<IndexedURL>> index;
    private int Id;

    public StorageBarrel(int id) throws RemoteException {
        super();
        this.index = new HashMap<>();
        this.Id = id;
    }
    public HashMap<String, String[]> SearchWords(String[] words) {

        ArrayList<HashMap<String, String[]>> data = new ArrayList<>();

        for (String word : words) {
            for (Map.Entry<String, HashSet<IndexedURL>> map : this.index.entrySet()) {
                if (map.getKey().equals(word)) {
                    HashMap<String , String[]> auxmap = new HashMap<>();
                    for (IndexedURL auxidx: map.getValue()){
                        String[] auxinfo = new String[2];
                        auxinfo[0] = auxidx.getTitle();
                        auxinfo[1] = auxidx.getQuote();
                        auxmap.put(auxidx.getUrl(), auxinfo);
                    }
                    data.add(auxmap);
                    //set_links.add(map.getValue());
                }
            }
        }

        for (Map.Entry<String, String[]> map: data.get(0).entrySet()){
            for (HashMap<String, String[]> auxmap: data) {
                if(!auxmap.containsKey(map.getKey())){
                    for (HashMap<String, String[]> rem: data) {
                        rem.remove(map.getKey());
                    }
                }
            }
        }

        return data.get(0);

        /*
        ArrayList<HashSet<String>> set_links = new ArrayList<>();

        //System.out.println(this.index);

        for (String word : words) {
            for (Map.Entry<String, HashSet<String>> map : this.index.entrySet()) {
                if (map.getKey().equals(word)) {
                    set_links.add(map.getValue());
                }
            }
        }

        if (set_links.isEmpty()) {
            return null;
        }

        HashSet<String> common = new HashSet<>(set_links.get(0));
        for (HashSet<String> links : set_links) {
            common.retainAll(links);
        }

        return common;
        */
    }

    public static void main(String[] args) {

        try {

            SMInterface sm = (SMInterface) LocateRegistry.getRegistry(7777).lookup("Barrel");
            StorageBarrel barrel = new StorageBarrel(Integer.parseInt(args[0]));
            sm.NewBarrel((SBInterface) barrel, Integer.toString(barrel.Id));

            MulticastClientBarrel mcb = new MulticastClientBarrel(barrel.index, barrel.Id);
            mcb.start();

            HashSet<IndexedURL> auxset = new HashSet<>();
            auxset.add(new IndexedURL("https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos", "GOD ELISEU", null));
            auxset.add(new IndexedURL("https://inforestudante.uc.pt/nonio/security/login.do", "Inforestudante", null));
            barrel.index.put("Teste", auxset);

            HashSet<IndexedURL> auxset2 = new HashSet<>();
            auxset2.add(new IndexedURL("https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos", "GOD ELISEU", null));
            barrel.index.put("Eliseu", auxset2);

            //System.out.println(barrel.index);

            try {
                File file = new File("src/StorageBarrel/hashmap" + barrel.Id + ".obj");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fileOut = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(barrel.index);
                out.close();
                fileOut.close();

            } catch (FileNotFoundException e) {
                System.out.println("FOS: " + e);
            } catch (IOException e) {
                System.out.println("OOS: " + e);
            }


        } catch (RemoteException | NotBoundException e) {
            System.out.println("Exception in SB.main: " + e);
        }
    }
}

class MulticastClientBarrel extends Thread {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;

    private int barrelId;

    public HashMap<String, HashSet<IndexedURL>> index;
    public MulticastClientBarrel(HashMap<String, HashSet<IndexedURL>> idx, int id) {
        this.index = idx;
        this.barrelId = id;
    }
    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(PORT);  // create socket and bind it
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);

            while (true) {
                byte[] buffer = new byte[50000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                ByteArrayInputStream bytes = new ByteArrayInputStream(buffer);
                ObjectInputStream in = new ObjectInputStream(bytes);

                URL data = (URL) in.readObject();

                //System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");

                System.out.println("--------> " + data.getUrl() + " " + data.getTitle());



                bytes.close();
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            socket.close();
        }
    }

    public void insertIndex() {


    }
}

