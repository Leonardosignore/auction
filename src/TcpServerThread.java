import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class TcpServerThread extends Thread {
    private Socket socket;
    private Repository repository;

    String ipClient = "224.0.0.1";
    String ipServer = "224.0.1.1";
    int portClient = 5050;
    int portServer = 5000;

    public TcpServerThread(Socket socket, Repository repository) {
        this.socket = socket;
        this.repository = repository;
    }

    public void run() {

        System.out.println("Thread started!\n");
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            DataInputStream din = new DataInputStream(in);
            DataOutputStream dout = new DataOutputStream(out);

            // [0] send category
            ObjectOutputStream objOut = new ObjectOutputStream(dout);
            ArrayList<String> categories = repository.selectCategories();
            objOut.writeObject(categories);
            System.out.println("[0] send categories" + categories.toString());

            // [1] receive category
            String category = din.readUTF();
            System.out.println("[1] received category " + category);

            // [2] send Item by category
            ArrayList<Item> items = repository.selectItemsByCategory(category);
            objOut.writeObject(items);
            System.out.println("[2] send Item by category " + items.toString());

            // [3] receive Item
            Item item = null;
            try {
                ObjectInputStream objIn = new ObjectInputStream(din);
                item = (Item) objIn.readObject();
                System.out.println("[3] received item " + item.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            // [4] send ip multicast
            String ip = null;
            if (item != null) {
                ip = "224.0.0.1";
                // ip = repository.getIp(item);
                dout.writeUTF(ip);
                System.out.println("[4] send ip " + ip);
            }
            UdpServerThread udpServerListener = new UdpServerThread(
                    ipClient,
                    portServer,
                    ipServer,
                    portClient);
            udpServerListener.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}