package com.rsmart.sakai.webservices;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

/**
 * Post processes calls to SakaiReport.jws to remove soap envelope and set the proper mime type for get requests only.
 *
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 8/14/12
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReportFilter implements Filter {
    private FilterConfig filterConfig = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!isReportCall(request)) {
            chain.doFilter(request, response);
            return;
        }

        OutputStream out = response.getOutputStream();
        StringWrapper wrapper = new StringWrapper((HttpServletResponse) response);

        chain.doFilter(request, wrapper);

        String output = wrapper.toString();
        try {
            XPath executeQueryReturnPath = XPath.newInstance("//executeQueryReturn");
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(new StringReader(output));
            Element element = (Element) executeQueryReturnPath.selectSingleNode(doc);
            if (element != null) {
                if (request.getParameter("format").toUpperCase().startsWith("CSV")){
                    response.setContentType("text/csv");
                } else if (request.getParameter("format").toUpperCase().startsWith("JSON")){
                    response.setContentType("application/json");
                } else {
                    response.setContentType("text/xml");
                }
                out.write(element.getValue().getBytes());
                out.close();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        out.write(output.getBytes());
        out.close();


    }

    private boolean isReportCall(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if (!httpServletRequest.getMethod().equalsIgnoreCase("GET")) {
                return false;
            }
            return (httpServletRequest.getRequestURI().endsWith("/SakaiReport.jws"));
        }
        return false;
    }

    public void destroy() {
        this.filterConfig = null;
    }
}
