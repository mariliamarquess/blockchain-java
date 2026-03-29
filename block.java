package noobchain;
import java.util.ArrayList;
import java.util.Date;

public class block {
	
// Esse é o Block, responsável por criar uma cadeia de hash.

	public String hash; //armazenará a assinatura digital.
	public String previousHash; //armazenará o hash do bloco anterior.
	public String merkleRoot;
	public ArrayList<transaction> Transactions = new ArrayList<transaction>();
	public long timeStamp; //como número de milisegundos desde 01/01/1970.
	public int nonce;
	
		
	// Aqui é a construção do Block (bloco).
	public block( String previousHash ) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash();
	}
	
	// Esse novo metodo calculará o hash de todas as partes do bloco que não devem ser adulteradas (previousHash, data e timeStamp).
			public String calculateHash() {
				String calculatedhash = stringUtil.applySha256(
						previousHash +
						Long.toString(timeStamp) +
						Integer.toString(nonce) + 
						merkleRoot
						);
				return calculatedhash;
			}
	
	// Incrementa o valor do nonce até que o hash alvo seja alcançado
	public void mineBlock(int difficulty) { //Representa a quantidade de zeros que o algoritmo precisa resolver.
		merkleRoot = stringUtil.getMerkleRoot(Transactions);
		String target = stringUtil.getDificultyString(difficulty); //Cria uma string com dificuldade * "0"
		while(!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}
		System.out.println("Block Mined!!! : " + hash);
	}
	
	//Adiciona transações para este bloco.
	public boolean addTransaction(transaction transaction) {
		// Processa a transação e verifica se é válida,
		// a menos que seja o bloco gênesis, caso em que é ignorada.
		if(transaction == null) return false;
		if((previousHash != "0")) {
			if((transaction.processTransaction() != true)) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		Transactions.add(transaction);
		System.out.println("Transaction Successfully added to Block");
		return true;
	}
}