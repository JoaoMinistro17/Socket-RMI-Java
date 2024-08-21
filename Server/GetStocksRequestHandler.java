package Server;

import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.*;

public class GetStocksRequestHandler extends Thread implements Serializable{
	Socket ligacao;
	BufferedReader in;
	PrintWriter out;
	Server server;
	private final SocketNotifyService notifyService;

	public GetStocksRequestHandler(Socket ligacao, SocketNotifyService notifyService, Server server) throws RemoteException {
		this.ligacao = ligacao;
		this.notifyService = notifyService;
		this.server = server;


		// Adiciona o cliente Socket a uma lista de inscritos socket
		notifyService.addSocketSubscriber(ligacao);

		try {
			this.in = new BufferedReader (new InputStreamReader(ligacao.getInputStream()));
			this.out = new PrintWriter(ligacao.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("Erro na execucao do servidor: " + e);
			System.exit(1);
		}
	}

	@Override
	public void run() {
		try {
			System.out.println("Aceitou ligacao de cliente no endereco " + ligacao.getInetAddress() + " na porta " + ligacao.getPort());

			while(true) {
				try {

					//lê o pedido do cliente
					String request = in.readLine();
					System.out.println("Request=" + request);

					//quando o cliente se desconecta do servidor
					if (Objects.equals(request, null) || request.equals("SAIR")) {
						out.println("SAIR");
						System.out.println("Cliente " + ligacao.getInetAddress() + " desconectou-se.");
						ligacao.close();
						break;
					}

					try {
						//envia estado(stock) para o cliente
						if (request.equals("STOCK_REQUEST")) {
							String stock_response = Stocks.stock_request();

							//assinar a mensagem
							String stock_response_signed = sign_message(stock_response);

							System.out.println("stock_response_signed "+stock_response);

							//enviar para o cliente a mensagem que contém o stock_response+ assinatura
							out.println(stock_response_signed);
						}

						if (request.startsWith("STOCK_UPDATE")) {

							//utiliza o método remoto para notificar os clientes RMI
							String stock_updated = server.stock_update(request);

							//assinar a mensagem
							String stock_updated_signed = sign_message(stock_updated);

							//enviar para o cliente a mensagem que contém o stock_updated + assinatura
							out.println(stock_updated_signed);

							//descartar os stock errors
							if (stock_updated.startsWith("STOCK_UPDATED")) {

								// Notificar clientes Socket sobre atualização de stock
								notifyService.notifySocketClients(stock_updated_signed);
							}
						}

						//quando o cliente necessitar da chave publica
						if(request.startsWith("GET_PUBKEY")){
							// Obtenha a chave pública do par de chaves
							PublicKey publicKey = server.pair.getPublic();

							// Converte a chave pública em bytes
							byte[] publicKeyBytes = publicKey.getEncoded();

							// Codifica os bytes para Base64 para envio
							String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);

							out.println(publicKeyBase64);
						}

					} catch (Exception e){
						System.out.println("Erro na assinatura. "+e);
					}

					if (request.equals("ERRO")) {
						out.println("ERRO");
					}

					out.flush();
				} catch(SocketException se){
					System.out.println("Cliente " + ligacao.getInetAddress() + "desconectou-se inesperadamente." + se);
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("Erro na execucao do servidor: " + e);
			System.exit(1);
		} finally {
			// Remover o cliente Socket da lista de inscritos ao terminar sessão
			((SocketNotifyServiceImpl)notifyService).removeSocketSubscriber(ligacao);
		}
	}

	public String sign_message(String message){
		try {

			//Creating a Signature object
			Signature sign = Signature.getInstance("SHA256withRSA");

			//Initialize the signature
			sign.initSign(server.pair.getPrivate());

			byte[] bytes = message.getBytes();

			//Adding data to the signature
			sign.update(bytes);

			//Calculating the signature
			byte[] signature = sign.sign();

			return message + ";" + Base64.getEncoder().encodeToString(signature);

		}catch(Exception e){
			System.out.println("Erro na assinatura. "+e);
			return null;
		}
	}

}