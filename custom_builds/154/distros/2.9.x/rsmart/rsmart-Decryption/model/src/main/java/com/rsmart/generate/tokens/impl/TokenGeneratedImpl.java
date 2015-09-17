package com.rsmart.generate.tokens.impl;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 1/6/12
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 */

import com.rsmart.generate.persistence.GeneratedTokens;
import com.rsmart.generate.tokens.api.TokenGeneratedService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


import java.sql.Timestamp;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 1/6/12
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class TokenGeneratedImpl extends HibernateDaoSupport implements TokenGeneratedService {
    private static final Log logger = LogFactory.getLog(TokenGeneratedImpl.class);
    private ServerConfigurationService serverConfigurationService;

    public void init() {
        logger.info("Starting TokenGeneratedService");

    }

    public String createUUID() {
        return UUID.randomUUID().toString();
    }

    public Long addGeneratedToken(final GeneratedTokens token) {
        {
            HibernateCallback
                    callback = new HibernateCallback() {
                public Object doInHibernate(Session session)
                        throws HibernateException {
                    Transaction tx = session.beginTransaction();
                    Long id = (Long) session.save(token);
                    tx.commit();
                    return id;
                }
            };

            return (Long) getHibernateTemplate().execute(callback);

        }
    }

    public String getUserId(final String token) {
        {
            HibernateCallback
                    callback = new HibernateCallback() {
                public Object doInHibernate(Session session)
                        throws HibernateException {

                    Iterator results = session.createQuery
                            ("select userId from GeneratedTokens where token =:token").
                            setParameter("token", token).list().iterator();

                    String userId = null;
                    while (results.hasNext()) {
                        String row = (String) results.next();
                        userId = row;

                    }
                    return userId;
                }
            };

            return (String) getHibernateTemplate().execute(callback);

        }
    }


    public String generateTokens(String loginId) {
        GeneratedTokens generatedTokens = new GeneratedTokens();
        String generatedUUID = createUUID();
        generatedTokens.setUserId(loginId);
        generatedTokens.setToken(generatedUUID);
        generatedTokens.setDateCreated(new Date());
        addGeneratedToken(generatedTokens);

        logger.debug("generated token: " + generatedUUID + " for loginId of " + loginId);
        return generatedUUID;
    }


    public String deleteTokens() {
        long milli = Calendar.getInstance().getTimeInMillis();
        //300000 = 5 minutes
        String milliSec = serverConfigurationService.getString("generated.token.table.cleanup", "300000");
        long timeMilli = new Long(milliSec);
        final Timestamp timeForDeleting = new Timestamp(milli - timeMilli);
        HibernateCallback
                callback = new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                Query results = session.createQuery
                        ("delete from GeneratedTokens where dateCreated < :dateCreated").setParameter("dateCreated", timeForDeleting);
                int resultsValue = results.executeUpdate();
                return String.valueOf(resultsValue);
            }
        };

        return (String) getHibernateTemplate().execute(callback);
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }


}
