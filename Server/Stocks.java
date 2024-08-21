package Server;

import java.io.*;
import java.net.Socket;
import java.util.*;
public class Stocks {
    private static int cont = 0;

    public static String FilePath = "C:/Users/danma/OneDrive/Ambiente de Trabalho/write.txt";
    static File file;
    public HashMap<String, Integer> presentStocks;

    public Stocks(File file) {
        Stocks.file = file;
        presentStocks = leDoFicheiro(file);
    }
    private HashMap<String, Integer> getStocksList(){
        return presentStocks;
    }

    public static void escreveNoFicheiro(File file, HashMap<String, Integer> presentStocks){
        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter(file));
            // iterate map entries
            for (Map.Entry<String, Integer> entry : presentStocks.entrySet()) {

                // put key and value separated by a colon
                bf.write(entry.getKey() + ":" + entry.getValue());

                // new line
                bf.newLine();
            }
            bf.close();
        } catch (Exception e){
            System.out.println("ERRO! Não é possível escrever no ficheiro");
        }
    }

    public static HashMap<String, Integer> leDoFicheiro(File file){

        HashMap<String, Integer> presentStocks = new HashMap<>();
        try{

            // create BufferedReader object from the File
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line = null;

            // read file line by line
            while ((line = br.readLine()) != null) {

                // split the line by :
                String[] parts = line.split(":");

                // first part is name, second is number
                String name = parts[0].trim();
                Integer number = Integer.parseInt(parts[1]);

                presentStocks.put(name, number);
            }
            br.close();
        } catch (Exception e){
            System.out.println("ERRO! Não é possível ler o ficheiro: "+e);
        }
        return presentStocks;
    }

    public static String stock_update(String request){

        HashMap<String, Integer> stocks = Stocks.leDoFicheiro(file);

        String[] values = request.split(" ");
        String nome = values[1];
        int quant = Integer.parseInt(values[2]);

        if (quant != 0) {
            if (stocks.containsKey(nome)) { //verifica validade do produto
                if ((stocks.get(nome) + quant) >= 0) { //verifica se é possível remover a quantidade pretendida
                    stocks.put(nome, stocks.get(nome) + quant);
                    Stocks.escreveNoFicheiro(file, stocks);
                    return "STOCK_UPDATED " + stocks;
                } else return"STOCK_ERROR: Não existe stock suficiente.";
            } else return "STOCK_ERROR: Produto selecionado não existe.";
        } else return "STOCK_ERROR: Quantidade a inserir inválida.";
    }

    public static String stock_request(){

        HashMap<String, Integer> stocks = leDoFicheiro(file);

        return "STOCK_RESPONSE: "+stocks;
    }
}
