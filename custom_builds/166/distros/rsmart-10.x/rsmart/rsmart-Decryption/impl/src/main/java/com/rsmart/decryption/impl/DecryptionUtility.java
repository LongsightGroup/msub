package com.rsmart.decryption.impl;

import com.rsmart.decryption.api.DecryptionUtilityService;
import com.rsmart.generate.util.QueryUtilityService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.spec.InvalidKeySpecException;


/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 12/13/11
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class DecryptionUtility implements DecryptionUtilityService {

    private Cipher decipher;
    private static Log M_log = LogFactory.getLog(DecryptionUtility.class);
    private ServerConfigurationService serverConfigurationService;
    private String sharedVector_prop;
    private QueryUtilityService queryUtilityService;

    public DecryptionUtility(){

    }

    public void init() {
        try {
            //retrieve properties
            sharedVector_prop = serverConfigurationService.getString("sakai.session.decryption.sharedVector","");
            String sharedKey = serverConfigurationService.getString("sakai.session.decryption.sharedKey", "");


            if (sharedVector_prop.equals("")) {
                M_log.debug("The initialization key is not in properties file");
                return;
            }

            if (sharedKey.equals("")) {
                M_log.debug("The Shared key is not in properties file");
                return;
            }

            decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");

            Key key2 = SecretKeyFactory.getInstance("DESede").generateSecret(
                    new DESedeKeySpec(sharedKey.getBytes("UTF8")));
            decipher.init(Cipher.DECRYPT_MODE, key2, new IvParameterSpec(convertStringToByteArray()));


        } catch (javax.crypto.NoSuchPaddingException e) {
            M_log.debug("Exception in DecryptionUtility class in constructor" + e);
        } catch (java.security.NoSuchAlgorithmException e) {
            M_log.debug("Exception in DecryptionUtility class in constructor" + e);
        } catch (java.security.InvalidKeyException e) {
            M_log.debug("Exception in DecryptionUtility class in constructor" + e);
        } catch (InvalidAlgorithmParameterException e) {
            M_log.debug("Exception in DecryptionUtility class in constructor" + e);
        } catch (InvalidKeySpecException e) {
            M_log.debug("Exception in DecryptionUtility class in constructor" + e);
        } catch (UnsupportedEncodingException e) {
            M_log.debug("Exception in DecryptionUtility class in constructor" + e);
        }
    }

    /**
     * Decrypting the encrypted session id
     *
     * @param urlDecoder
     * @return
     */
    public String deEncryptionStringUtility(String urlDecoder) {
        try {
            if (urlDecoder == null) return null;

            byte[] decrypt = new BASE64Decoder().decodeBuffer(urlDecoder);
            M_log.debug("incoming parm: " + urlDecoder);

            byte[] encBytes = decipher.doFinal(decrypt);
            String value = new String(encBytes, "UTF8");
             M_log.debug("decrypted parm: " + value);

            String replace = value.replace("&", "").replace("sessionid:", "").trim();
            byte[] replace1 = Base64.decodeBase64(replace);
            String recoveredSessionId = new String(replace1, "UTF8");
             M_log.debug("recovered session id: " + recoveredSessionId);
            return recoveredSessionId;

        } catch (Exception e) {
            M_log.debug("Exception in "+ this.getClass().getName() + ":deEncryptionStringUtility", e);
        }
        return null;
    }

    /**
     * Retrieve EID internal Id.
     * @param eid
     * @return
     */
    public String getUserId(String eid) {
        return queryUtilityService.retrieveInternalId(eid);
    }


    /**
     * converting the string vector initialization to a Initialization byte []
     *
     * @return
     */
    private byte[] convertStringToByteArray() {
        String[] spiltValue = sharedVector_prop.split(",");
        byte[] data = new byte[spiltValue.length];
        for (int i = 0; i < spiltValue.length; i++) {
            String[] spiltAgain = spiltValue[i].split("x");
            int number = Integer.parseInt(spiltAgain[1], 16);
            String numString = String.valueOf(number);
            data[i] = Byte.valueOf(numString).byteValue();
        }
        return data;
    }


    //IOC Setters


    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setQueryUtilityService(QueryUtilityService queryUtilityService) {
        this.queryUtilityService = queryUtilityService;
    }
}
