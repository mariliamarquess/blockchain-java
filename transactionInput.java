package noobchain;

public class transactionInput {

	public String transactionOutputID; //Referencia para transactionOutputs -> transactionID
	public transactionOutput UTXO; // Contém a saída de transação não gasta.
	
	// Essa classe é usada para referenciar transactionOutputs que ainda não foram gastos.
	// o transactionOutputID é usado para encontrar o transactionOutput relevante, permitindo que os mines verifiquem sua propriedade.
	public transactionInput(String transactionOutputID) {
		this.transactionOutputID = transactionOutputID;
	}
}
