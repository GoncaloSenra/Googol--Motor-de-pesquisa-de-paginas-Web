
package StorageBarrel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
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
    public HashMap<String, HashSet<IndexedURL>> pages_list;
    public HashMap<String, Integer> word_counter;
    public HashMap<String, String> users;
    private int Id;

    public StorageBarrel() throws RemoteException {
        super();
        this.index = new HashMap<>();
        this.pages_list = new HashMap<>();
        this.word_counter = new HashMap<>();
        this.users = new HashMap<>();
    }

    public HashMap<String, Integer> getWord_counter() throws RemoteException{
        return word_counter;
    }

    public HashSet<String[]> SearchPointerLinks(String url) throws RemoteException{
        HashSet<String[]> links = new HashSet<>();

        //System.out.println("---------- " + this.pages_list.size());
        HashMap<String, HashSet<IndexedURL>> copy = new HashMap<>(this.pages_list);

        for (Map.Entry<String, HashSet<IndexedURL>> map : copy.entrySet()) {
            if (map.getKey().equalsIgnoreCase(url)) {
                for (IndexedURL idx: map.getValue()) {
                    String[] aux = new String[3];
                    aux[0] = idx.getUrl();
                    aux[1] = idx.getTitle();
                    aux[2] = idx.getQuote();
                    links.add(aux);
                }
                return links;
            }
        }

        return null;
    }

    public ArrayList<String[]> SearchWords(String[] words) throws RemoteException{

        //Upadte Word Counter
        for (String word : words) {
            if (word_counter.get(word) == null) {
                word_counter.put(word, 1);
            } else {
                int count = word_counter.get(word);
                count ++;
                word_counter.put(word, count);
            }
        }


        HashMap<String, HashSet<IndexedURL>> copy = new HashMap<>(this.index);

        ArrayList<HashMap<String, String[]>> data = new ArrayList<>();

        for (String word : words) {
            boolean empty = true;
            for (Map.Entry<String, HashSet<IndexedURL>> map : copy.entrySet()) {
                if (map.getKey().equalsIgnoreCase(word)) {
                    empty = false;
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
            if (empty) {
                return null;
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

        ArrayList<String[]> sorted_urls = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : auxdata.entrySet()) {
            String[] aux = new String[3];
            aux[0] = entry.getKey();
            aux[1] = entry.getValue()[0];
            aux[2] = entry.getValue()[1];
            if (sorted_urls.isEmpty()){
                sorted_urls.add(aux);
            } else {
                boolean inserted = false;
                for (int i = 0; i < sorted_urls.size(); i++) {
                    int num_insertion;
                    if (pages_list.get(aux[0]) == null){
                        num_insertion = 0;
                    } else {
                        num_insertion = pages_list.get(aux[0]).size();
                    }
                    int num_list;
                    if (pages_list.get(sorted_urls.get(i)[0]) == null) {
                        num_list = 0;
                    } else {
                        num_list = pages_list.get(sorted_urls.get(i)[0]).size();
                    }
                    if (num_insertion > num_list) {
                        sorted_urls.add(i, aux);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) {
                    sorted_urls.add(aux);
                }
            }
        }

        data.clear();
        data.add(auxdata);

        return sorted_urls;
    }

    public static void main(String[] args) {

        try {

            SMInterface sm = (SMInterface) LocateRegistry.getRegistry(7777).lookup("Barrel");
            StorageBarrel barrel = new StorageBarrel();
            barrel.Id = sm.NewBarrel((SBInterface) barrel);


            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        sm.TerminateBarrel(barrel.Id);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Barrel is being terminated!");
                }
            });

            try {
                File file = new File("src/StorageBarrel/index" + barrel.Id + ".obj");
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    if (file.length() != 0){
                        FileInputStream fileIn = new FileInputStream(file);
                        ObjectInputStream in = new ObjectInputStream(fileIn);
                        barrel.index = (HashMap<String, HashSet<IndexedURL>>) in.readObject();
                        barrel.pages_list = (HashMap<String, HashSet<IndexedURL>>) in.readObject();
                        in.close();
                        fileIn.close();
                    }
                }

            } catch (FileNotFoundException e) {
                System.out.println("FOS: " + e);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("OOS: " + e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            MulticastClientBarrel mcb = new MulticastClientBarrel(barrel.index, barrel.pages_list,barrel.Id);
            mcb.start();

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
    public HashMap<String, HashSet<IndexedURL>> pages_list;

    public MulticastClientBarrel(HashMap<String, HashSet<IndexedURL>> idx, HashMap<String, HashSet<IndexedURL>> pl, int id) {
        this.index = idx;
        this.pages_list = pl;
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

                //######################################################


                DatagramPacket ackPacket = new DatagramPacket(new byte[4], 4);

                ByteBuffer buffer2 = ByteBuffer.allocate(128);
                buffer2.putInt(1);

                ackPacket.setData(buffer2.array());

                ackPacket.setSocketAddress(packet.getSocketAddress());

                socket.send(ackPacket);


                /*
                byte[] ack = new byte[128];


                String ackb = "ACKKKKKKKKKKKKKKKKKK";

                ack = ackb.getBytes(StandardCharsets.UTF_8);

                DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, group, PORT);
                socket.send(ackPacket);
                */

                /*
                byte[] ackb = new byte[1000];

                ByteArrayOutputStream bytes2 = new ByteArrayOutputStream();
                ObjectOutputStream outMulticast = new ObjectOutputStream(bytes2);


                String ack = "ACKKKKKKKKKKKKKKKKKK";

                ackb = bytes2.toByteArray();
                outMulticast.writeObject(ack);

                DatagramPacket ackPacket = new DatagramPacket(ackb, 1, group, PORT);
                socket.send(ackPacket);
                */

                //######################################################


                //System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                System.out.println("> " + data.getUrl() + " " + data.getTitle());

                for (String word : data.getWords()){
                    HashSet<IndexedURL> aux = this.index.get(word);
                    if (aux == null) {
                        aux = new HashSet<IndexedURL>();
                        aux.add(new IndexedURL(data.getUrl(), data.getTitle(), data.getUrls(), data.getQuote()));
                        //System.out.println("isNullllllllllllllllllll");
                    } else{
                        boolean contains = false;
                        for (IndexedURL idx: aux){
                            //System.out.println("-----> " + data.getUrl() + " | " + idx.getUrl());
                            if (idx.getUrl().equals(data.getUrl())){
                                //System.out.println("Entreiiiiiiiiiiiiiii");
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            aux.add(new IndexedURL(data.getUrl(), data.getTitle(), data.getUrls(), data.getQuote()));
                        }
                    }

                    this.index.put(word, aux);
                }

                for (String link: data.getUrls()) {
                    HashSet<IndexedURL> aux_pages;
                    if (pages_list == null) {
                        pages_list = new HashMap<String, HashSet<IndexedURL>>();
                    }
                    if ((aux_pages = pages_list.get(link)) == null){
                        aux_pages = new HashSet<IndexedURL>();
                        aux_pages.add(new IndexedURL(data.getUrl(), data.getTitle(), data.getUrls(), data.getQuote()));
                    } else {
                        boolean contains = false;
                        for (IndexedURL idx: aux_pages){
                            //System.out.println("-----> " + data.getUrl() + " | " + idx.getUrl());
                            if (idx.getUrl().equals(data.getUrl())){
                                //System.out.println("Entreiiiiiiiiiiiiiii");
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            aux_pages.add(new IndexedURL(data.getUrl(), data.getTitle(), data.getUrls(), data.getQuote()));
                        }
                    }
                    pages_list.put(link, aux_pages);
                }

                //System.out.println("+++++++" + index.size());

                try {
                    File file = new File("src/StorageBarrel/index" + this.barrelId + ".obj");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fileOut = new FileOutputStream(file, false);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(this.index);
                    out.writeObject(this.pages_list);
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

