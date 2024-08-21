package Server;

import ClientRMI.SecureDirectNotificationInterface;

import java.net.*;
import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server extends UnicastRemoteObject implements Serializable, StockServer {
	//final static String FilePath = "/workspaces/FSD/presences-java-sockets-source/write.txt";
	final static String FilePath = "/home/mini/Documents/write.txt";
	//public static String FilePath = "C:/Users/danma/OneDrive/Ambiente de Trabalho/write.txt";
	static final int DEFAULT_PORT_RMI = 1999;
	static final int DEFAULT_PORT_SOCKET = 2000;
	private final List<SecureDirectNotificationInterface> subscribers = new ArrayList<>();
	private final List<Socket> socketSubscribers = new ArrayList<>();
	private final SocketNotifyService notifyService;

	File file;
	Stocks stocks;

	KeyPair pair;

	// https://www.tutorialspoint.com/java_cryptography/java_cryptography_creating_signature.htm

	public Server() throws RemoteException, NoSuchAlgorithmException {
		file = new File(FilePath);
		stocks = new Stocks(file);

		// Initialize notifyService with the implementation
		this.notifyService = new SocketNotifyServiceImpl();

		//Creating KeyPair generator object
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

		//Initializing the KeyPairGenerator
		keyPairGen.initialize(2048);

		//Generate the pair of keys
		pair = keyPairGen.generateKeyPair();
	}

	public static void main(String[] args) {
		try {
			Server server = new Server();

			//SERVER RMI
			server.StartRMIServer(server);
			//SERVER SOCKET
			server.StartSocketServer(server);

		}catch(Exception e){
			System.out.println("Error: "+e);
		}
	}

	//Inicializar servidor RMI
	public void StartRMIServer(Server server) {
		try {

			// Registar o objeto remoto no registro RMI
			Registry registry = LocateRegistry.createRegistry(DEFAULT_PORT_RMI);
			registry.rebind("Server", server);

			System.out.println("RMI server is running on port "+ DEFAULT_PORT_RMI);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Inicializar servidor socket
	public void StartSocketServer(Server server){
		try {
			ServerSocket servidor = new ServerSocket(DEFAULT_PORT_SOCKET);
			System.out.println("Socket server is running on port " + DEFAULT_PORT_SOCKET);

			while (true) {

				// Listen for a connection to be made to the socket and accepts it: API java.net.ServerSocket
				Socket ligacao = servidor.accept();

				// Start a GetPresencesRequestHandler thread
				GetStocksRequestHandler atendedor = new GetStocksRequestHandler(ligacao, this.notifyService, server);
				atendedor.start();
			}
		} catch (IOException e) {
			System.out.println("Erro na execucao do servidor: " + e);
			System.exit(1);
		}
	}

	@Override
	public String stock_request() throws RemoteException {
		return Stocks.stock_request();
	}

	@Override
	public String stock_update(String request) throws RemoteException {

		try{
			//mensagem com o estado atualizado do sistema
			String stock_updated = Stocks.stock_update(request);

			//Creating a Signature object
			Signature sign = Signature.getInstance("SHA256withRSA");

			//Initialize the signature
			sign.initSign(pair.getPrivate());
			byte[] bytes = stock_updated.getBytes();

			//Adding data to the signature
			sign.update(bytes);

			//Calculating the signature
			byte[] signature = sign.sign();

			// Notificar clientes RMI
			for (SecureDirectNotificationInterface subscriber : subscribers) {
				//Notificar todos os clientes
				subscriber.stock_updated_signed(stock_updated, signature);
			}

			return stock_updated;

		} catch (Exception e) {
			// Handle Exception if necessary
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void unsubscribe(SecureDirectNotificationInterface d) throws RemoteException {
		subscribers.remove(d);
	}

	@Override
	public PublicKey get_pubKey() throws RemoteException {
		return pair.getPublic();
	}

	@Override
	public void subscribe(SecureDirectNotificationInterface d) throws RemoteException {
		subscribers.add(d);
	}
}
