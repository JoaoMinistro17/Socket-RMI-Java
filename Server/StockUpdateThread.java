package Server;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class StockUpdateThread extends Thread implements Serializable {
    private boolean isRunning;
    Socket ligacao;

    PublicKey publicKey;

    public StockUpdateThread(Socket ligacao, PublicKey publicKey) {
        this.ligacao = ligacao;
        this.isRunning = true;
        this.publicKey = publicKey;
    }

    public void run() {
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(ligacao.getInputStream()));
            PrintWriter out = new PrintWriter(ligacao.getOutputStream(), true);
            while (isRunning) {

                //Envia pedido STOCK_REQUEST para o servidor
                out.println("STOCK_REQUEST");

                //recebe a mensagem assinada pelo servidor
                String stock_response_signed = in.readLine();

                // split the line by ";"
                String[] parts = stock_response_signed.split(";");

                //desconcatenar a mensagem
                String stock_response = parts[0].trim();
                byte[] signature = Base64.getDecoder().decode(parts[1].trim());

                if (verifySignature(publicKey, stock_response, signature)) {
                    System.out.println("Signature verified");
                } else {
                    System.out.println("Signature failed");
                }

                System.out.println(stock_response);

                //para a thread quando a ligação fechar
                isRunning = ligacao.isConnected();

                //thread reinicia a cada 5 segundos
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.out.println("Erro! " + e);
            System.exit(1);
        }
    }

    public static boolean verifySignature(PublicKey publicKey, String stock_updated, byte[] signature) {

        try {

            //Creating a Signature object
            Signature sign = Signature.getInstance("SHA256withRSA");

            byte[] bytes = stock_updated.getBytes();
            //Initializing the signature
            sign.initVerify(publicKey);
            //Update the data to be verified
            sign.update(bytes);

            boolean bool = sign.verify(signature);

            return bool;

        } catch (Exception e){
            System.out.println("ERRO! "+e);
            return false;
        }
    }
}
