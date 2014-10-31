package org.sakaiproject.webservices;


import com.rsmart.sakai.mvel.api.MvelService;
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
 * SakaiMVEL.jws
 * <p/>
 * Web service that will evaluate MVEL scripts
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class SakaiMVEL extends AbstractWebService {

    private static final Log LOG = LogFactory.getLog(SakaiMVEL.class);

    private MvelService mvelService;

    /**
     * Evaluate the expression using the MvelService
     *
     * @param sessionid  the id of the session to retrieve
     * @param expression the expression to evaluate
     * @return the result of the expression
     */

    @WebMethod
    @Path("/evaluate")
    @Produces("text/plain")
    @GET
    public String evaluate(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "expression", partName = "expression") @QueryParam("expression") String expression) {
        Session session = establishSession(sessionid);

        return mvelService.evaluateAsString(expression);
    }

    /**
     * Evaluate the expression and paramters using the MvelService
     * Parameters must be expressed in JSON as a Map, for example:
     * {
     * "two": 2,
     * "three": 3,
     * "firstName": "Earle"
     * }
     * Only Simple data types can be mapped.
     *
     * @param sessionid  the id of the session to retrieve
     * @param expression the expression to evaluate
     * @param parameters paramters that are used in the evaluation. Paramters are expressed in JSON.
     * @return the result of the expression
     */

    @WebMethod
    @Path("/evaluateWithParameters")
    @Produces("text/plain")
    @GET
    public String evaluateWithParameters(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "expression", partName = "expression") @QueryParam("expression") String expression,
            @WebParam(name = "parameters", partName = "parameters") @QueryParam("parameters") String parameters) {
        Session session = establishSession(sessionid);

        return mvelService.evaluateAsString(expression, parameters);
    }

    @WebMethod(exclude = true)
    public void setMvelService(MvelService mvelService) {
        this.mvelService = mvelService;
    }
}
