package fi.kotipalo.h.CryptoID;

public class User {
	private String pubkey;
	private String uname;
	private Boolean deleted=false;
	
	// hmm.. confusing.. this is actually bitcoin address, not public key
	public String getPubkey() {
		return pubkey;
	}
	public void setPubkey(String pubkey) {
		this.pubkey = pubkey;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public void deleteUser() {
		this.deleted = true;
	}
	public Boolean isDeleted() {
		return deleted;
	}
}
