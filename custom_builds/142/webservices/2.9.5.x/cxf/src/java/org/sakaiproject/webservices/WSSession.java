package org.sakaiproject.webservices;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)

public class WSSession extends AbstractWebService {

    private static final Log log = LogFactory.getLog(WSSession.class);

    /**
     * @deprecated see http://jira.sakaiproject.org/browse/SAK-18136
     */
    @WebMethod
    @Path("/getActiveUserCount")
    @Produces("text/plain")
    @GET
    public int getActiveUserCount(
            @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
            @WebParam(name = "elapsed", partName = "elapsed") @QueryParam("elapsed") int elapsed) {
        log.warn("WSSession.getActiveUserCount is deprecated and will be relocated in Sakai 2.8. See SAK-18136.");

        try {
            Session s = establishSession(sessionId);
            return SessionManager.getActiveUserCount(elapsed);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

}
