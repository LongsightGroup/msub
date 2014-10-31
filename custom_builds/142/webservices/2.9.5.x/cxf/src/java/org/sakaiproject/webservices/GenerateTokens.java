package org.sakaiproject.webservices;

import com.rsmart.decryption.api.GeneratedTokenService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Session;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * GenerateTokens.jws
 * <p/>
 * The main authentication web service, performs remote login and logout functions for Sakai.
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class GenerateTokens extends AbstractWebService {

    private static final Log LOG = LogFactory.getLog("org.sakaiproject.axis.GenerateTokens");

    private GeneratedTokenService generatedTokenService;

    /**
     * Login with the supplied credentials and return the session string which can be used in subsequent web service calls, ie via SakaiScript
     *
     * @param sessionId
     * @param eid
     * @return session string
     */
    @WebMethod
    @Path("/generateToken")
    @Produces("text/plain")
    @GET
    public String generateToken(
    @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
    @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {
        Session session = establishSession(sessionId);
        if (!securityService.isSuperUser()) {
            LOG.warn("NonSuperUser trying to add accounts: " + session.getUserId());
            throw new RuntimeException("NonSuperUser trying to add accounts: " + session.getUserId());
        } else {
            String uuidToken = generatedTokenService.generateToken(eid);
            return uuidToken;
        }

    }

    @WebMethod(exclude = true)
    public void setGeneratedTokenService(GeneratedTokenService generatedTokenService) {
        this.generatedTokenService = generatedTokenService;
    }

}




