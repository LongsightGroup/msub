package com.rsmart.decryption.util;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 8/13/12
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class EncryptUtility {
    static boolean debug = false;

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher encipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");

        if (args.length < 3) {
            System.out.println("Usage: java com.rsmart.decryption.impl.EncryptUtility string_to_encrypt shared_key shared_vector debug");
            System.out.println("    debug is optional and expects true/false");

            System.exit(127);
        }


        String inputString = args[0];
        String sharedKey = args[1];
        String sharedVector_prop = args[2];

        if (args.length == 4) {
            if (Boolean.valueOf(args[3])) {
                debug = true;
            }
        }

        Key key2 = SecretKeyFactory.getInstance("DESede").generateSecret(
                new DESedeKeySpec(sharedKey.getBytes("UTF8")));
        encipher.init(Cipher.ENCRYPT_MODE, key2, new IvParameterSpec(convertStringToByteArray(sharedVector_prop)));
        decipher.init(Cipher.DECRYPT_MODE, key2, new IvParameterSpec(convertStringToByteArray(sharedVector_prop)));

        String urlEncodedValue = encrypt(inputString, encipher, decipher);

        if (debug) {
            System.out.print("securehash: " + secureHash(args[0]) + "\n");
        }

        System.out.println(urlEncodedValue);
    }

    private static String encrypt(String str, Cipher encipher, Cipher decipher) {
        try {
            String enc64a = new BASE64Encoder().encode(str.getBytes());
            if (debug) {
                System.out.println("input: " + str);
                enc64a = "sessionid:" + enc64a + "&";

                System.out.println("input base64'd plus 'sessionid:' and '&' at end: " + enc64a);

            }


            byte[] encBytes = encipher.doFinal(enc64a.getBytes("UTF8"));
            String encrypted64 = new BASE64Encoder().encode(encBytes);
            encrypted64 = encrypted64.replaceAll("\n", "");

            if (debug) {

                System.out.println("previous line encrypted & base64'd: " + encrypted64);
            }
            String urlEncoded = URLEncoder.encode(encrypted64, "UTF8");

            if (debug) {
                System.out.println("urlEncoded: " + urlEncoded);
            }
            // reverse the process
            String urlDecode = URLDecoder.decode(urlEncoded, "UTF8");
            if (debug) {

                System.out.println("url decoded:" + urlDecode);
            }
            byte[] decrypted64 = new BASE64Decoder().decodeBuffer(urlDecode);
            byte[] decryptedBytes = decipher.doFinal(decrypted64);
            enc64a = new String(decryptedBytes, "UTF8");
            if (debug) {

                System.out.println("decrypted: " + enc64a);
            }
            String replace = enc64a.replace("&", "").replace("sessionid:", "").trim();
            byte[] replace1 = new BASE64Decoder().decodeBuffer(replace);
            enc64a = new String(replace1, "UTF8");
            if (debug) {

                System.out.println("un-base64'd: " + enc64a);
            }


            return urlEncoded;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

    private static byte[] convertStringToByteArray(String str) {

        String[] spiltValue = str.split(",");
        byte[] data = new byte[spiltValue.length];
        for (int i = 0; i < spiltValue.length; i++) {
            String[] spiltAgain = spiltValue[i].split("x");
            int number = Integer.parseInt(spiltAgain[1], 16);
            String numString = String.valueOf(number);
            data[i] = Byte.valueOf(numString).byteValue();
        }
        return data;
    }

    public static String secureHash(String password) {
        try {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-512");
                if (debug) {

                    System.out.print(md);
                }
            } catch (NoSuchAlgorithmException e) {

                System.out.println("sha-512 not supported: " + e);
                try {
                    md = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e1) {
                    System.out.println("sha-1 not supported: " + e);
                    try {
                        md = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException e2) {
                        System.out.print("You have no Message Digest Algorightms intalled in this JVM, secure Hashes are not availalbe, encoding bytes :"
                                + e2.getMessage());
                        return "<encrypted>";//encode(StringUtils.leftPad(password, 10, '_').getBytes(UTF8));
                    }
                }
            }
            byte[] bytes = md.digest(password.getBytes("UTF-8"));
            return encode(bytes);
        } catch (UnsupportedEncodingException e3) {
            System.out.print("no UTF-8 Envoding, get a real JVM, nothing will work here. NPE to come");
            return null;
        }
    }

    public static String encode(byte[] hash) {
        return Base64.encodeBase64URLSafeString(hash);
    }
}
