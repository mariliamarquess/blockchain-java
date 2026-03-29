package noobchain;

import java.security.*;
import java.util.ArrayList;

public class transaction {

	public String transactionId; // hash das transações.
	public PublicKey sender; // endereço dos remetentes/chave pública.
	public PublicKey recipient; // endereço dos destinatários/chave pública.
	public float value; //valor/montante
	public byte[] signature; // assinatura criptografada: previne que  ninguem gaste os fundos da carteira do usuário.
	
	public ArrayList<transactionInput> inputs = new ArrayList<transactionInput>();
	public ArrayList<transactionOutput> outputs = new ArrayList<transactionOutput>();
	
	public static int sequence = 0; //uma contagem aproximada de quantas transações foram geradas.
	
	//Construtor.
	public transaction(PublicKey from, PublicKey to, float value, ArrayList<transactionInput> inputs) {
		this.sender = from;
		this.recipient = to;
		this.value = value;
		this.inputs = inputs;	
	}
	
	//Calcula o hash da transação (que será usado como ID)
	private String calculateHash() {
		sequence++; //aumenta a sequência para evitar 2 transações idênticas com o mesmo retorno hash.
		return stringUtil.applySha256(
				stringUtil.getStringFromKey(sender) +
				stringUtil.getStringFromKey(recipient) +
				Float.toString(value) + sequence
		);
	}
	
	//Assina todos os dados que não podem ser adulterados.
	public void generateSignature(PrivateKey privateKey) {
		String data = stringUtil.getStringFromKey(sender) + stringUtil.getStringFromKey(recipient) + Float.toString(value);
		signature = stringUtil.applyECDSASig(privateKey, data);
	}
	
	// Verifica se os dados assinados não foram adulterados.
	public boolean verifySignature() {
		String data = stringUtil.getStringFromKey(sender) + stringUtil.getStringFromKey(recipient) + Float.toString(value);
		return stringUtil.verifyECDSASig(sender, data, signature);
	}
	
	//Retornar verdadeiro se a nova transação poderia ser criada.
	public boolean processTransaction() {
		
		if(verifySignature() ==  false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
		
		// Coleta as entradas da transação (certifique-se de que não foram gastas).
		for(transactionInput i : inputs) {
			i.UTXO = blockchain.UTXOs.get(i.transactionOutputID);
		}
		
		//Checa se a transação é válida.
		if(getInputsValue() < blockchain.minimumTransaction) {
			System.out.println("#Transaction Inputs to small: " + getInputsValue());
			return false;
		}
		
		//Gera transações de saídas (outputs).
		float leftOver = getInputsValue() - value; //Obtém o valor das entradas e depois o troco restante. 
		transactionId = calculateHash();
		outputs.add(new transactionOutput(this.recipient, value, transactionId)); //Envia o valor para o destinatário.
		outputs.add(new transactionOutput( this.sender, leftOver, transactionId)); // Envia o troco restante de volta ao remetente.
		
		//Adiciona as saídas a lista dos valores não gastos.
		for (transactionOutput o : outputs) {
			blockchain.UTXOs.put(o.id, o);
		}
		
		//Remove as entradas da transação da lista de UTXOs como gastas.
		for(transactionInput i : inputs) {
			if(i.UTXO == null) continue;
			blockchain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	//Retona com a soma dos valores das entradas(UTXOs).
	public float getInputsValue() {
		float total = 0;
		for(transactionInput i : inputs) {
			if(i.UTXO == null) continue;
			total += i.UTXO.value;
		}
		
		return total;
	}
	
	//Retona com a soma dos valores de saída.
	public float getOutputsValue() {
		float total = 0;
		for (transactionOutput o : outputs) {
			total += o.value;
		}
		
		return total;
	}
}