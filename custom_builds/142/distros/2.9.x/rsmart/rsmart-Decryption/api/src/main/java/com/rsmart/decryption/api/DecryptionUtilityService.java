package com.rsmart.decryption.api;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 12/13/11
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DecryptionUtilityService {

    /**
     * This method decrypts strings, Using Initialization Vector. Decryption use DES, UrlEncode and UrlDecoded and Base64
     * @param str
     * @return
     */
    public String deEncryptionStringUtility(String str);

    /**
     * Retrieve EID internal Id.
     * @param eid
     * @return
     */
    public String getUserId(String eid);
}
