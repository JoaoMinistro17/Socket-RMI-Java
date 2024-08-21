package Server;

import ClientRMI.SecureDirectNotificationInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface StockServer extends Remote {

    String stock_request() throws RemoteException;

    String stock_update(String request) throws RemoteException;

    void subscribe(SecureDirectNotificationInterface d) throws RemoteException;

    void unsubscribe(SecureDirectNotificationInterface d) throws RemoteException;

    //metodo a ser invocado pelos clientes para obterem a chave publica
    PublicKey get_pubKey()throws RemoteException;
}
