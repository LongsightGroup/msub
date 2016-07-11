package com.rsmart.decryption.impl;

import com.rsmart.decryption.api.GeneratedTokenService;
import com.rsmart.generate.tokens.api.TokenGeneratedService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 1/6/12
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenerateTokenImpl implements GeneratedTokenService{
    private static final Log logger = LogFactory.getLog(GenerateTokenImpl.class);
    private TokenGeneratedService tokenGeneratedService;

    public  void init(){
     logger.debug("Executing the GenerateTokenImpl");
    }

    /**
     *
     * @param loginId
     * @return
     */
    public String generateToken(String loginId) {
        String createdToken = tokenGeneratedService.generateTokens(loginId);
        return createdToken;
    }

    public String getUserId(String token) {
        return  tokenGeneratedService.getUserId(token);
    }

    // IOC Setters
    public void setTokenGeneratedService(TokenGeneratedService tokenGeneratedService) {
        this.tokenGeneratedService = tokenGeneratedService;
    }
}
