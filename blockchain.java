package noobchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap; //os HashMaps permitem usar uma chave para encontrar um valor.
import java.util.Scanner;

import org.bouncycastle.*;
import com.google.gson.GsonBuilder;

public class blockchain {
	
	//Armazena os blocos em um Array List e faz o uso do Gson para visualização em Json.
	public static ArrayList<block> blockchain = new ArrayList<block>();
	public static HashMap<String,transactionOutput> UTXOs = new HashMap<String,transactionOutput>(); //lista de todas transações não gastas.
	
	public static int difficulty = 3;
	public static wallet walletA;
	public static wallet walletB;
	public static float minimumTransaction = 0.1f;
	public static transaction genesisTransaction;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// Configura o Bouncy Castle como provedor de segurança
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		// Cria as novas carteiras (wallets).
		walletA = new wallet();
		walletB = new wallet();
		wallet coinbase = new wallet();
		
		// Cria a transação gênesis, que envia 100 coin para a walletA.
		genesisTransaction = new transaction(coinbase.publicKey, walletA.publicKey, 200f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);
		genesisTransaction.transactionId = "0";
		genesisTransaction.outputs.add(new transactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		System.out.println("Creating and Mining Genesis block...");
		block genesis = new block("0");
		genesis.addTransaction(genesisTransaction);
		addblock(genesis);
		
		//Testando.
		block block1 = new block(genesis.hash);
		Scanner scan = new Scanner(System.in);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("Enter the amount to be transferred from walletA to walletB: ");
		float sendValue = scan.nextFloat();
		System.out.println("\nWalletA is Attempting to send funds " + sendValue + "to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, sendValue));
		addblock(block1);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		block block2 = new block(block1.hash);
		System.out.println("Enter the amount to be transferred from walletA to walletB: ");
		float sendValue2 = scan.nextFloat();
		System.out.println("\nWalletA Attempting to send more funds " + sendValue2 + "than it has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, sendValue2));
		addblock(block2);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		block block3 = new block(block2.hash);
		System.out.println("Enter the amount to be transferred from walletB to walletA: ");
		float sendValue3 = scan.nextFloat();
		System.out.println("\nWalletB is Attempting to send funds " + sendValue3 + "to WalletA...");
		block3.addTransaction(walletB.sendFunds( walletA.publicKey, sendValue3));
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		
		isChainValid();
		
	}
	
	public static Boolean isChainValid() {
		block currentBlock;
		block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,transactionOutput> tempUTXOs = new HashMap<String, transactionOutput>();
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
		
		//loop para o blockchain checar os hashes.
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compara o hash registrado e o hash calculado:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ) {
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compara o previous hash e o previous hash registrado.
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			 //checa se o hash está resolvido
			if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			// Percorre as transações da blockchain.
			transactionOutput tempOutput;
			for(int t=0; t <currentBlock.Transactions.size();t++) {
				transaction currentTransaction = currentBlock.Transactions.get(t);
				
				if(!currentTransaction.verifySignature()) {
					System.out.println("#Signature on transaction(" + t + ")");
					return false;
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false;
				}
				
				for(transactionInput input: currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputID);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputID);
					
				}
				
				for(transactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id,  output);
				}
				
				if( currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
					
				}
				if(currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
			}
		}	
		System.out.println("Blockchain is valid");
		return true;
	}
	
	public static void addblock(block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
	
}