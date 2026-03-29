package noobchain;
import java.security.*; //Utiliza o .KeyPairGenerator para gerar um par de chaves de curva elíptica.
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class wallet {

	public PrivateKey privateKey; // assina transações. permite que apenas o proprietário da chave possa gastar as criptomoedas. 
	public PublicKey publicKey; //funciona como endereço, logo você compartilha com outras pessoas.
	
	//Resumo: A chave privada é usada para assinar os dados que não queremos que sejam adulterados. 
	//A chave pública é usada para verificar a assinatura.
	
	public HashMap<String, transactionOutput> UTXOs = new HashMap<String, transactionOutput>();
	
	public wallet() {
		generateKeyPair();
	}
	
	public void generateKeyPair() {
		try {
			//Esse método usa o `java.security.KeyPairGenerator` para gerar um par de chaves de curva elíptica. 
			
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			//Inicializa o gerador de chaves e gera a KeyPair (chaves pares).
			keyGen.initialize(ecSpec, random); //256 bytes para fornecer um nível de segurança aceitável.
			KeyPair keyPair = keyGen.generateKeyPair();
			// chave pública e privada do keyPair.
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic(); 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//Remove as entradas da transação da lista de UTXOs, marcando-as como gastas.
	public float getBalance() {
		float total = 0;
	for (Map.Entry<String, transactionOutput> item: blockchain.UTXOs.entrySet()) {
		transactionOutput UTXO = item.getValue();
		if(UTXO.isMine(publicKey)) {
			UTXOs.put(UTXO.id, UTXO);
			total += UTXO.value;
		}
	}
	
		return total;
		
	}
	
	//Gera e retorna a nova transação desta carteira.
	public transaction sendFunds(PublicKey _recipient, float value) {
		if(getBalance() < value) {
			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}
		
		//Cria um array list de entradas.
		ArrayList<transactionInput> inputs = new ArrayList<transactionInput>();
		
		float total = 0;
		for (Map.Entry<String, transactionOutput> item: UTXOs.entrySet()) {
			transactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new transactionInput(UTXO.id));
			if(total > value) break;
		}
		
		transaction newTransaction = new transaction(publicKey, _recipient, value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(transactionInput input : inputs) {
			UTXOs.remove(input.transactionOutputID);
		}
		
		return newTransaction;
	}
}