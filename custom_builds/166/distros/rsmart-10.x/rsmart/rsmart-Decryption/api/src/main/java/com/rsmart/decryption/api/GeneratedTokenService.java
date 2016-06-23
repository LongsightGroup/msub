package com.rsmart.decryption.api;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 1/6/12
 * Time: 11:18 AM
 * To change this template use File | Settings | File Templates.
 */
public interface GeneratedTokenService {

      /**
     * Creates an UUID token and saves token and userId in the database
     * @param loginId
     * @return
     */
    public String generateToken(String loginId);

    /**
     * Retrieves the userId associated with the token
     * @param token
     * @return
     */
    public String getUserId(String token);
}
