
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



public class StorageBarrel extends UnicastRemoteObject implements SBInterface, Serializable {
    public HashMap<String, HashSet<IndexedURL>> index;
    private int Id;

    public StorageBarrel(int id) throws RemoteException {
        super();
        this.index = new HashMap<>();
        this.Id = id;
    }
    public HashMap<String, String[]> SearchWords(String[] words) {

        HashMap<String, HashSet<IndexedURL>> copy = new HashMap<>(this.index);

        System.out.println("============\n" + copy.size());

        ArrayList<HashMap<String, String[]>> data = new ArrayList<>();

        for (String word : words) {
            for (Map.Entry<String, HashSet<IndexedURL>> map : copy.entrySet()) {
                if (map.getKey().equalsIgnoreCase(word)) {
                    System.out.println("Existe");
                    HashMap<String , String[]> auxmap = new HashMap<>();
                    for (IndexedURL auxidx: map.getValue()){
                        String[] auxinfo = new String[2];
                        auxinfo[0] = auxidx.getTitle();
                        auxinfo[1] = auxidx.getQuote();
                        auxmap.put(auxidx.getUrl(), auxinfo);
                    }
                    data.add(auxmap);
                }
            }
        }

        if (data.isEmpty()){
            return null;
        }

        HashMap<String, String[]> auxdata = new HashMap<>();

        for (Map.Entry<String, String[]> entry : data.get(0).entrySet()) {
            String key = entry.getKey();
            boolean exists = true;

            for (int i = 1; i < data.size(); i++) {
                if (!data.get(i).containsKey(key)) {
                    exists = false;
                    break;
                }
            }

            if (exists) {
                auxdata.put(key, entry.getValue());
            }
        }

        data.clear();
        data.add(auxdata);

        return data.get(0);
    }

    public static void main(String[] args) {

        try {

            SMInterface sm = (SMInterface) LocateRegistry.getRegistry(7777).lookup("Barrel");
            StorageBarrel barrel = new StorageBarrel(Integer.parseInt(args[0]));
            sm.NewBarrel((SBInterface) barrel, Integer.toString(barrel.Id));

            MulticastClientBarrel mcb = new MulticastClientBarrel(barrel.index, barrel.Id);
            mcb.start();

            try {
                File file = new File("src/StorageBarrel/index" + barrel.Id + ".obj");
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    FileInputStream fileIn = new FileInputStream(file);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    barrel.index = (HashMap<String, HashSet<IndexedURL>>) in.readObject();
                    in.close();
                    fileIn.close();
                }

            } catch (FileNotFoundException e) {
                System.out.println("FOS: " + e);
            } catch (IOException e) {
                System.out.println("OOS: " + e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            /*
            HashSet<IndexedURL> auxset = new HashSet<>();
            auxset.add(new IndexedURL("https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos", "GOD ELISEU", null, "LENDA DAS LENDAS"));
            auxset.add(new IndexedURL("https://inforestudante.uc.pt/nonio/security/login.do", "Inforestudante", null, "Site manhoso"));
            barrel.index.put("Teste", auxset);

            HashSet<IndexedURL> auxset2 = new HashSet<>();
            auxset2.add(new IndexedURL("https://pt.wikipedia.org/wiki/Eliseu_Pereira_dos_Santos", "GOD ELISEU", null, "LENDA DAS LENDAS"));
            barrel.index.put("Eliseu", auxset2);

            //System.out.println(barrel.index);

            try {
                File file = new File("src/StorageBarrel/index" + barrel.Id + ".obj");
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
            */


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
                byte[] buffer = new byte[100000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                ByteArrayInputStream bytes = new ByteArrayInputStream(buffer);
                ObjectInputStream in = new ObjectInputStream(bytes);

                URL data = (URL) in.readObject();

                //System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                System.out.println("> " + data.getUrl() + " " + data.getTitle());

                for (String word : data.getWords()){
                    HashSet<IndexedURL> aux = this.index.get(word);
                    if (aux == null) {
                        aux = new HashSet<IndexedURL>();
                    }
                    aux.add(new IndexedURL(data.getUrl(), data.getTitle(), null, ""));
                    this.index.put(word, aux);
                }
                System.out.println("+++++++" + index.size());

                try {
                    File file = new File("src/StorageBarrel/index" + this.barrelId + ".obj");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fileOut = new FileOutputStream(file, false);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(this.index);
                    out.close();
                    fileOut.close();

                } catch (FileNotFoundException e) {
                    System.out.println("FOS: " + e);
                } catch (IOException e) {
                    System.out.println("OOS: " + e);
                }


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

}

