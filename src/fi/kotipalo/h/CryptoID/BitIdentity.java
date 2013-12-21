/*
 * Copyright 2012-2013 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fi.kotipalo.h.CryptoID;

import java.security.SignatureException;
import java.util.Date;
import java.util.List;

import com.google.bitcoin.core.ECKey;

public class BitIdentity {
	// ToDo: should I add email address and/or realname?
	
	private String uname = null;
	private String serverPubKey;
	private BitcoinKeyPair signingKeys;
	private Date createTime = new Date();
	
	private boolean deleted = false;

	public BitIdentity () {
		signingKeys = new BitcoinKeyPair(); 
	}

	public BitIdentity (String serverPubKey, String uname) {
		this();
		this.serverPubKey = serverPubKey;
		this.uname = uname;
	}

	public BitIdentity (List<String> lines) {
		if (lines.isEmpty()) return;
		int s = lines.get(0).indexOf('[');
		int e = lines.get(0).indexOf(']');
		if ((s>=0) && (e>s)) {
			//String addressString = lines[0].substring(s+1, e);
			String publicKeyString = null;
			String privKeyString = null;
			for (String line:lines) {
				String [] param = line.split("=");
				String paramname = param[0].trim();
				String paramvalue = null;
				if (param.length > 1) paramvalue = param[1].trim();
				if ("label".equalsIgnoreCase(paramname)) {
					uname = paramvalue;
				} else if ("privkey".equalsIgnoreCase(paramname)) {
					privKeyString = paramvalue;
				} else if ("pubkey".equalsIgnoreCase(paramname)) {
					publicKeyString = paramvalue;
				} else if ("serverpubkey".equalsIgnoreCase(paramname)) {
					serverPubKey = paramvalue;
				}
			}
			if (privKeyString != null)
				signingKeys = new BitcoinKeyPair(privKeyString, publicKeyString);
		}
	}
	
	public String generateSignaTure(String message) {
		return signingKeys.generateSignaTure(message);
	}
	
	public static boolean verifyMessage(String message, String signatureBase64, String assumedSignerAddress) {
		try {
			ECKey realSigner = ECKey.signedMessageToKey(message, signatureBase64);
			String realSignerAddress = realSigner.toAddress().toString();
			return assumedSignerAddress.equals(realSignerAddress);
		} catch (SignatureException e) {
			return false;
		}
	}

	public static String getMessageSigner(String message, String signatureBase64) {
		try {
			ECKey realSigner = ECKey.signedMessageToKey(message, signatureBase64);
			String realSignerAddress = realSigner.toAddress().toString();
			return realSignerAddress;
		} catch (SignatureException e) {
			return "";
		}
	}


	public boolean verifyMessage(String message, String signatureBase64) {
		return verifyMessage(message, signatureBase64, this.getAddress());
	}	
	
	public String getUname() {
		return uname;
	}
	
	public void setUname(String uname) {
		this.uname = uname;
	}
	
	public String getServerPubKey() {
		return serverPubKey;
	}
	
	public void setServerPubKey(String serverPubKey) {
		this.serverPubKey = serverPubKey;
	}

	// We are actually using bitcoin addresses, don't need this, it just confuses
	private String getPubKey() {
		return signingKeys.getBitcoinPubKey();
	}

	public String getAddress() {
		return signingKeys.getAddress();
	}

	public String getPrivKey() {
		return signingKeys.getBitcoinPrivKey();
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void deleted() {
		this.deleted = true;
	}
	
	public String getSerialized() {
		return 	"["+ getAddress() +"]\n"
				+ "label = " + getUname() + "\n"
				+ "privkey = " + getPrivKey() + "\n"
				+ "pubkey = " + getPubKey() + "\n"
				+ "serverpubkey = " + getServerPubKey() + "\n";
	}

	public Date getCreateTime() {
		return createTime;
	}

}
