package com.rsmart.preauth.client.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Hex;

public class HexMac {

	private final byte[] hmac;
	
	public HexMac(final byte[] key) {
		this.hmac = key;
	}
	
	public String getHmac(final String data) {
        try {
            ByteKey bk = new ByteKey(this.hmac);
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(bk);
            return new String(Hex.encodeHex(mac.doFinal(data.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("fatal error", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("fatal error", e);
        }
    }

    static class ByteKey implements SecretKey {
        private static final long serialVersionUID = -7217091296729145624L;
        private byte[] mKey;

        ByteKey(byte[] key) {
            mKey = key.clone();
        }

        public byte[] getEncoded() {
            return mKey;
        }

        public String getAlgorithm() {
            return "HmacSHA1";
        }

        public String getFormat() {
            return "RAW";
        }

    }
}
