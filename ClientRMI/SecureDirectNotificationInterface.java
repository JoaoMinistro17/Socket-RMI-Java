package ClientRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface SecureDirectNotificationInterface extends Remote
{
    void stock_updated(String message) throws RemoteException;
    void stock_updated_signed(String message, byte[] signature) throws RemoteException;
}
