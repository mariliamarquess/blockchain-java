package noobchain;

import java.security.PublicKey;

public class transactionOutput {
	
	public String id;
	public PublicKey recipient; //também conhecido como o novo dono dessas moedas (destinatário).
	public float value; // o montante/valor de moedas que possui.
	public String parentTransactionID; //o ID da transação em que esta saída foi criada.
	
	//Construtor
	public transactionOutput(PublicKey recipient, float value, String parentTransactionID) {
		this.recipient = recipient;
		this.value = value;
		this.parentTransactionID = parentTransactionID;
		this.id = stringUtil.applySha256(stringUtil.getStringFromKey(recipient)+Float.toString(value)+parentTransactionID);
	}
	
	//Checa se a moeda pertence ao destinatário. 
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == recipient);
	}
}
