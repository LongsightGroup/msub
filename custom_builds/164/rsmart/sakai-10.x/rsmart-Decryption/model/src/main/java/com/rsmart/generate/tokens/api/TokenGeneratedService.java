package com.rsmart.generate.tokens.api;

import com.rsmart.generate.persistence.GeneratedTokens;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 1/6/12
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
public interface TokenGeneratedService {

    /**
     * Deletes generated tokens and userId to database "generated_tokens"
     */
    public String deleteTokens();

    /**
     * creates a token and saves it to the database
     * @param loginId
     * @return
     */
    public String generateTokens(String loginId);

    /**
     *  Retrieves userId from token
     * @param token
     * @return
     */
    public String getUserId(final String token);

    /**
     * Add tokens to database
     * @param token
     * @return
     */
    public Long addGeneratedToken(final GeneratedTokens token);
}
