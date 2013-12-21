package fi.kotipalo.h.CryptoID;

import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Base58;
import com.google.bitcoin.core.ECKey;

public class BitcoinKeyPair {
	
	private ECKey myBitcoinKey;
		
	public BitcoinKeyPair() {
		myBitcoinKey = new ECKey();
	}
	
	public BitcoinKeyPair(String privkey, String pubkey) {
		byte[] decodedPublicKey;
		byte[] decodedPrivKey;
		try {
			decodedPrivKey = Base58.decode(privkey);
			if (pubkey == null) {
				myBitcoinKey = new ECKey(decodedPrivKey,null);
			} else {
				decodedPublicKey = Base58.decode(pubkey);
				myBitcoinKey = new ECKey(decodedPrivKey, decodedPublicKey);
			}
		} catch (AddressFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String generateSignaTure(String message) {
		return myBitcoinKey.signMessage(message);
	}

	public String getBitcoinPubKey() {
		return Base58.encode(myBitcoinKey.getPubKey());
	}
	
	public String getBitcoinPrivKey() {
		return Base58.encode(myBitcoinKey.getPrivKeyBytes());
	}
	
	public String getAddress() {
		return myBitcoinKey.toAddress().toString();
	}
	
}
