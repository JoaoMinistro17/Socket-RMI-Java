package ClientRMI;
import Server.StockServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Signature;
import java.util.Scanner;

public class ClientRMI extends UnicastRemoteObject implements SecureDirectNotificationInterface {
    static final int DEFAULT_PORT=1999;
    static final String DEFAULT_HOST="127.0.0.1";
    private StockServer serverStub;

    public ClientRMI() throws RemoteException {
        super();
    }

    //Notifica os stock updates de outros clientes
    @Override
    public void stock_updated(String message) throws RemoteException {
        //não pode dar notify em caso de stock error
        if(message.startsWith("STOCK_UPDATE")) {
            System.out.println("Received stock update: " + message);
        }
    }

    @Override
    public void stock_updated_signed(String message, byte[] signature) throws RemoteException{

        try {
            //Caso a mensagem seja de erro apenas o cliente receberá a mensagem de erro
            if (message.startsWith("STOCK_UPDATE")) {

                //Creating a Signature object
                Signature sign = Signature.getInstance("SHA256withRSA");

                byte[] bytes = message.getBytes();

                //Initializing the signature
                sign.initVerify(serverStub.get_pubKey());

                //Update the data to be verified
                sign.update(bytes);

                boolean bool = sign.verify(signature);

                if (bool) {
                    System.out.println("Signature verified");
                } else {
                    System.out.println("Signature failed");
                }
                System.out.println("stock updated and signed. "+ message);
            }
        }catch(Exception e){
            System.out.println("Erro! " + e);
        }
    }

    public void subscribeToStockUpdates() {
        try {
            // Subscribe to stock updates
            serverStub.subscribe(this);

            System.out.println("Subscribed to stock updates.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeToStockUpdates() {
        try {
            // Unsubscribe to stock updates
            serverStub.unsubscribe(this);

            System.out.println("Unbscribed to stock updates.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String servidor=DEFAULT_HOST;
        int porto=DEFAULT_PORT;

        try {
            Scanner ler = new Scanner(System.in);
            //caso não especifique o servidor e a porta, usa os valores default
            if (args.length == 2) {
                servidor = args[0];
                porto = Integer.parseInt(args[1]);

            } else {
                System.out.println("(Vai usar ip e porta default. Ip "+servidor+" no porto "+ porto+")");
            }

            // Locate the RMI Registry
            Registry registry = LocateRegistry.getRegistry(servidor, porto);

            // Get the reference to the remote object
            StockServer stub = (StockServer) registry.lookup("Server");

            // Create an instance of the client
            ClientRMI client = new ClientRMI();

            // Set the reference to the server stub
            client.serverStub = stub;

            // Subscribe to stock updates
            client.subscribeToStockUpdates();

            System.out.println("Stock disponível "+ stub.stock_request());

            while(true) {
                try {
                    String pedido;
                    String item;
                    int quant;

                    System.out.println("1) Stock Update");
                    System.out.println("0) Sair");
                    int opt = Integer.parseInt(ler.next());

                    switch (opt) {
                        case 1 -> {
                            System.out.println("Nome do item a alterar: ");
                            item = ler.next();
                            System.out.println("Quantidade: ");
                            quant = Integer.parseInt(ler.next());
                            pedido = "STOCK_UPDATE " + item + " " + quant;

                            stub.stock_update(pedido);

                        }
                        case 0 -> {
                            client.unsubscribeToStockUpdates();
                            System.out.println("Terminou a ligacao!");
                            System.exit(1);
                        }
                        default -> System.out.println("ERRO");
                    }

                } catch (NumberFormatException n) {
                    System.out.println("Caratere inválido");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
