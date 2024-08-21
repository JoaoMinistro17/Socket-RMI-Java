package Client;

import Server.StockUpdateThread;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.net.*;
import java.io.*;

public class Client implements Serializable{

	static final int DEFAULT_PORT=2000;
	static final String DEFAULT_HOST="127.0.0.1";

	private static PublicKey publicKey;

	public static void main(String[] args) throws Exception {
		String servidor = DEFAULT_HOST;
		int porto = DEFAULT_PORT;

		Scanner ler = new Scanner(System.in);

		try {
			if (args.length == 2) {
				servidor = args[0];
				porto = Integer.parseInt(args[1]);
			} else {
				System.out.println("(Vai usar ip e porta default. Ip 127.0.0.1 no porto 2000)");
			}
		} catch(Exception e){
			System.out.println("Erro!");
		}

		try {

			// Create a representation of the IP address of the Server.Server: API java.net.InetAddress
			InetAddress serverAddress = InetAddress.getByName(servidor);

			// Create a client sockets (also called just "sockets"). A socket is an endpoint for communication between two machines: API java.net.Socket
			Socket ligacao = new Socket(serverAddress, porto);

			// Create a client sockets (also called just "sockets"). A socket is an endpoint for communication between two machines: API java.net.Socket
			//Socket ligacao2 = new Socket(serverAddress, porto);

			System.out.println("Ligação estabelecida no servidor " + servidor + " no porto " + porto);

			// Create a java.io.BufferedReader for the Socket; Use java.io.Socket.getInputStream() to obtain the Socket input stream
			BufferedReader in = new BufferedReader(new InputStreamReader(ligacao.getInputStream()));
			// Create a java.io. PrintWriter for the Socket; Use java.io.Socket.etOutputStream() to obtain the Socket output stream
			PrintWriter out = new PrintWriter(ligacao.getOutputStream(), true);

			//pede a public key ao servidor para poder verificar a assinatura
			out.println("GET_PUBKEY");

			try {
				String publicKeyBase64 = in.readLine();
				publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64.trim())));
			} catch(Exception e){
				System.out.println(e);
			}

			// Thread para atualização dos stocks e STOCK_REQUEST
			//StockUpdateThread stockUpdateThread = new StockUpdateThread(ligacao2, publicKey);
			StockUpdateThread stockUpdateThread = new StockUpdateThread(ligacao, publicKey);
			stockUpdateThread.start();

			String pedido;
			String item;
			int quant;

			while (true) {
				try {
					//menu inicial
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
						}
						case 0 -> pedido = "SAIR";
						default -> pedido = "ERRO";
					}

					// write the request into the Socket
					out.println(pedido);

					if (pedido.equals("SAIR")) {
						// Close the Socket
						System.out.println("Terminou a ligacao!");
						ligacao.close();
						System.exit(1);
						break;
					} else {

						//recebe a mensagem assinada pelo servidor
						String stock_updated_signed = in.readLine();

						// split the line by ";"
						String[] parts = stock_updated_signed.split(";");

						//desconcatenar a mensagem


						String stock_updated = parts[0].trim();
						byte[] signature = Base64.getDecoder().decode(parts[1].trim());

						if (verifySignature(publicKey, stock_updated, signature)) {
							System.out.println("Signature verified");
						} else {
							System.out.println("Signature failed");
						}

						System.out.println("Stock atualizado com sucesso! " + stock_updated);
					}

				} catch (NumberFormatException n) {
					System.out.println("Caratere inválido");
				}
			}
		} catch (IOException e) {
			System.out.println("Erro ao comunicar com o servidor: " + e);
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

			return sign.verify(signature);

		} catch (Exception e){
			System.out.println("ERRO! "+e);
			return false;
		}
	}

}