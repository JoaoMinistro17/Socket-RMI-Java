package Server;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

interface SocketNotifyService extends Remote {

    void addSocketSubscriber(Socket socket) throws RemoteException;

    void removeSocketSubscriber(Socket socket) throws RemoteException;

    void notifySocketClients(String message) throws RemoteException;
}

//Implementar a interface SocketNotifyService
public class SocketNotifyServiceImpl implements SocketNotifyService, Serializable {

    private final List<Socket> socketSubscribers;

    public SocketNotifyServiceImpl() {
        this.socketSubscribers = new ArrayList<>();
    }

    public void addSocketSubscriber(Socket socket) {
        socketSubscribers.add(socket);
    }

    public void removeSocketSubscriber(Socket socket) {
        socketSubscribers.remove(socket);
    }

    @Override
    public void notifySocketClients(String message) throws RemoteException {

        System.out.println("size of socket subscribers: "+socketSubscribers.size());

        // Notificar clientes Socket
        for (Socket socket : socketSubscribers) {
            //System.out.println("size of socket subscribers: "+ socketSubscribers.size());
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
