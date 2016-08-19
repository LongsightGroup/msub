package org.sakaiproject.login.springframework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ContextLoaderListener;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/29/13
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class SakaiHomeContextLoaderListener extends ContextLoaderListener {
    private static final Log log = LogFactory.getLog(SakaiHomeContextLoaderListener.class);

    protected org.springframework.web.context.ContextLoader createContextLoader()
   	{
    		return new SakaiHomeContextLoader();
   	}

}
