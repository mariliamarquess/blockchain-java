package noobchain;
// obtem acesso ao algortimo SHA256.
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class stringUtil {

	// Aplica o SHA256 a uma string e retorna o resultado.
	public static String applySha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			//Aplica o SHA256 ao input.
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer(); // Isto vai conter um hash do tipo hexadecimal.
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString(); // retorna a assinatura gerada como uma string.
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//Aplica ECDSA Signature e retorna com o resultado (em bytes).
	//Recebe a chave privada do remetente e uma string como entrada, assina o resultado e retorna um array de bytes.
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
	Signature dsa;
	byte[] output = new byte[0];
	try {
		dsa = Signature.getInstance("ECDSA", "BC");
		dsa.initSign(privateKey);
		byte[] strByte = input.getBytes();
		dsa.update(strByte);
		byte[] realSig = dsa.sign();
		output = realSig;
	} catch (Exception e) {
		throw new RuntimeException(e);
	} return output;
	}
	
	//Verifica a String signature
	//Recebe a assinatura, a chave pública e uma string como entrada e retorna verdadeiro ou falso se a assinatura for válida. 
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//Returns difficulty string target, to compare to hash. eg difficulty of 5 will return "00000"  
		public static String getDificultyString(int difficulty) {
			return new String(new char[difficulty]).replace('\0', '0');
		}
		
	//Retorna a string codificada a partir de qualquer chave.
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	// Recebe uma lista de transações e retorna a merkle root.
	public static String getMerkleRoot(ArrayList<transaction> Transactions) {
		int count = Transactions.size();
		ArrayList<String> previousTreeLayer = new ArrayList<String>();
		for(transaction transaction : Transactions) {
			previousTreeLayer.add(transaction.transactionId);
		}
		
		ArrayList<String> treeLayer = previousTreeLayer;
		while(count > 1) {
			treeLayer = new ArrayList<String>();
			for(int i=1; i < previousTreeLayer.size(); i++) {
				treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
			}
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}
		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
	}

}