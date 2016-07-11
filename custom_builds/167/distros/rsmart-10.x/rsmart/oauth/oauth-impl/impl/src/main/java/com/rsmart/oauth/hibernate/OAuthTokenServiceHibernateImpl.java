package com.rsmart.oauth.hibernate;

import com.rsmart.oauth.api.OAuthProvider;
import com.rsmart.oauth.api.OAuthSignatureMethod;
import com.rsmart.oauth.api.OAuthToken;
import com.rsmart.oauth.api.OAuthTokenService;

import com.rsmart.persistence.AmbiguousResultException;
import com.rsmart.persistence.NoSuchObjectException;
import com.rsmart.persistence.PersistenceException;
import com.rsmart.persistence.DataIntegrityException;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;

import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.UncategorizedDataAccessException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Query;

/**
 * User: duffy
 * Date: Jan 21, 2010
 * Time: 10:12:26 AM
 */
public class OAuthTokenServiceHibernateImpl
    extends HibernateDaoSupport
    implements OAuthTokenService
{
    private static Log
        LOG = LogFactory.getLog(OAuthTokenServiceHibernateImpl.class);

    // these constants map to queries named in the hibernate mapping files
    private static final String
        QUERY_TOKEN_BY_PROVIDER_USER            = "query.tokenByProviderUser",
        QUERY_PROVIDER_BY_NAME                  = "query.providerByName",
        QUERY_TOKENS_BY_USER                    = "query.tokensByUser",
        QUERY_TOKENS_BY_PROVIDER                = "query.tokensByProvider",
        QUERY_DELETE_TOKEN_BY_UUID              = "query.deleteTokenById",
        QUERY_DELETE_TOKENS_BY_USERID           = "query.deleteTokensByUserId",
        QUERY_DELETE_TOKENS_BY_PROVIDER         = "query.deleteTokenByProviderID",
        QUERY_GETALLPROVIDERS                   = "query.getAllProviders",
        QUERY_DELETE_TOKENS_LIST                = "query.deleteTokensById",
        QUERY_GET_PROVIDER_STATUS               = "query.getProviderStatus",
        QUERY_SET_PROVIDER_STATUS               = "query.setProviderStatus",
        QUERY_REMOVE_ADDITIONAL_HEADERS         = "query.emptyAdditionalHeadersByProviderId";

    private static final int
        MODE_DEFAULT                            = 0,
        MODE_CREATE                             = 1,
        MODE_SELECT                             = 2,
        MODE_DELETE                             = 3;

    /**
     * Validates that a List contains only one result, throwing a NoSuchObjectException or
     * an AmbiguousResultException if no or several results are in the list.  The single result
     * is returned.
     *
     * @param l
     * @return
     * @throws NoSuchObjectException
     * @throws AmbiguousResultException
     */
    protected final Object getSingleResult (final List l)
        throws NoSuchObjectException, AmbiguousResultException
    {
        long sz;

        if (l == null || (sz = l.size()) == 0)
        {
            throw new NoSuchObjectException();
        }
        else if (sz > 1)
        {
            throw new AmbiguousResultException();
        }

        return l.get(0);
    }

    /**
     * Instead of writing the same catch statements for all operations, this method wraps exceptions we are interested
     * in as PersistenceExceptions before throwing, and throws all others "as is"
     *
     * @param message
     * @param re
     * @throws Throwable
     */
    protected final PersistenceException catchAllPersistenceExceptions (String message, RuntimeException re)
    {
        return catchAllPersistenceExceptions (MODE_DEFAULT, message, null, null, null, re);
    }

    /**
     * Wraps exceptions into PersistenceExceptions or its subclasses as needed.  The mode flag may be one of
     * MODE_DEFAULT, MODE_CREATE, MODE_SELECT, or MODE_DELETE.  This allows the RuntimeException to be properly
     * interpretted correctly.  The type argument gives the type of object which was being manipulated.  keys[]
     * and keyTypes[] provide a list of the keys and their interpretation which were used in the failed query.
     *
     * @param mode
     * @param message
     * @param type
     * @param keys
     * @param keyTypes
     * @param re
     * @return
     */
    protected final PersistenceException catchAllPersistenceExceptions (int mode, String message, String type,
                                                                        Object keys[], String keyTypes[],
                                                                        RuntimeException re)
    {
        switch (mode)
        {
            case MODE_DEFAULT:
                break;
            case MODE_SELECT:
            {
                if (re instanceof HibernateObjectRetrievalFailureException ||
                    re instanceof ObjectNotFoundException)
                {
                    NoSuchObjectException
                        nsoe = ((message == null)?
                                new NoSuchObjectException(re):
                                new NoSuchObjectException(message, re));

                    nsoe.setObjectType(type);
                    nsoe.setKeys (keys);
                    nsoe.setKeyTypes (keyTypes);

                    return nsoe;
                }
                break;
            }
        }

        if (re instanceof HibernateException)
        {
            return new PersistenceException (message, re);
        }
        if (re instanceof UncategorizedDataAccessException)
        {
            return new PersistenceException (message, re);
        }

        throw re;
    }

    /**
     * Handles any RuntimeException which should be wrapped in a NoSuchObjectException.
     *
     * @param type
     * @param keys
     * @param keyTypes
     * @param re
     * @return
     */
    protected final NoSuchObjectException catchAllNoSuchObjectExceptions (String type, Object keys[], String keyTypes[],
                                                                          RuntimeException re)
    {
        if (re instanceof ObjectNotFoundException ||
            re instanceof HibernateObjectRetrievalFailureException)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(re);

            nsoe.setObjectType(type);

            nsoe.setKeys (keys);
            nsoe.setKeyTypes (keyTypes);

            return nsoe;
        }

        throw re;
    }

    /**
     * Internal method to commit an OAuthProvider object to the database.
     *
     * @param provider
     * @return
     * @throws DataIntegrityException
     * @throws PersistenceException
     */
    private String addOAuthProvider(final OAuthProvider provider)
        throws DataIntegrityException, PersistenceException
    {
        if (provider == null)
            throw new PersistenceException ("Cannot add a null OAuthProvider object");

        try
        {
            return (String) getHibernateTemplate().save(provider);
        }
        catch (DataIntegrityViolationException dive)
        {
            DataIntegrityException
               doe = new DataIntegrityException(dive);

            doe.setObjectType(OAuthProviderHibernateImpl.class.getSimpleName());

            doe.setKeys
                (new Object[]
                 {
                     provider.getProviderName()
                 });
            doe.setKeyTypes
                (new String[]
                 {
                     "Provider Name (String)"
                 });
            throw doe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception adding OAuthProvider", re);
        }

    }

    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String authUrl,
                                              String tokenUrl,
                                              String clientId,
                                              String clientSecret)
        throws DataIntegrityException, PersistenceException
    {
        return createOAuthProvider(providerName, description, authUrl, tokenUrl, clientId, clientSecret, null, true);
    }

    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String authUrl,
                                              String tokenUrl,
                                              String clientId,
                                              String clientSecret,
                                              boolean enabled)
        throws DataIntegrityException, PersistenceException
    {
        return createOAuthProvider(providerName, description, authUrl, tokenUrl, clientId, clientSecret, null, enabled);
    }

    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String authUrl,
                                              String tokenUrl,
                                              String clientId,
                                              String clientSecret,
                                              Map<String, String> additionalHeaders)
        throws DataIntegrityException, PersistenceException
    {
        return createOAuthProvider(providerName, description, authUrl, tokenUrl, clientId, clientSecret, additionalHeaders, true);
    }

    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String requestTokenURL,
                                              String userAuthorizationURL,
                                              String accessTokenURL,
                                              String realm,
                                              String consumerKey,
                                              String hmacSha1Key,
                                              String rsaSha1Key,
                                              final OAuthSignatureMethod signingMethod,
                                              Map<String, String> additionalHeaders,
                                              boolean enabled)
            throws DataIntegrityException, PersistenceException
    {
        assertSignatureMethodHasValidKey (signingMethod, hmacSha1Key, rsaSha1Key);

        OAuthProviderHibernateImpl
                provider = new OAuthProviderHibernateImpl();

        provider.setProviderName(providerName);
        provider.setDescription(description);
        provider.setRequestTokenURL(requestTokenURL);
        provider.setUserAuthorizationURL(userAuthorizationURL);
        provider.setAccessTokenURL(accessTokenURL);
        provider.setRealm(realm);
        provider.setConsumerKey(consumerKey);
        provider.setHmacSha1SharedSecret(hmacSha1Key);
        provider.setRsaSha1Key(rsaSha1Key);
        provider.setSignatureMethod(signingMethod);
        provider.setAdditionalHeaders(additionalHeaders);
        provider.setEnabled(enabled);

        addOAuthProvider(provider);
        LOG.debug("Provider created with UUID: " + provider.getUUID());

        return provider;
    }

    private static final void assertSignatureMethodHasValidKey (final OAuthSignatureMethod method,
                                                                final String hmacSha1Key,
                                                                final String rsaSha1Key)
            throws DataIntegrityException
    {
        switch (method)
        {
            case PLAINTEXT:
            {
                break;
            }
            case RSA_SHA1:
            {
                if (rsaSha1Key == null)
                {
                    throw new DataIntegrityException ("rsaSha1Key cannot be null if the RSA-SHA1 signing method is specified");
                }
                break;
            }
            default:
            case HMAC_SHA1:
            {
                if (hmacSha1Key == null)
                {
                    throw new DataIntegrityException ("hmacSha1Key cannot be null if the HMAC-SHA1 signing method is specified");
                }
                break;
            }
        }
    }

    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String tokenUrl,
                                              String authUrl,
                                              String clientId,
                                              String clientSecret,
                                              Map<String, String> additionalHeaders,
                                              boolean enabled)
        throws DataIntegrityException, PersistenceException
    {

        OAuthProviderHibernateImpl
            provider = new OAuthProviderHibernateImpl();

        provider.setProviderName(providerName);
        provider.setDescription(description);
        provider.setTokenUrl(tokenUrl);
        provider.setAuthUrl(authUrl);
        provider.setClientId(clientId);
        provider.setClientSecret(clientSecret);
        provider.setAdditionalHeaders(additionalHeaders);
        provider.setEnabled(enabled);

        addOAuthProvider(provider);
        LOG.debug("Provider created with UUID: " + provider.getUUID());

        return provider;
    }
    public Set<OAuthProvider> getAllOAuthProviders()
        throws PersistenceException
    {
        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            List
                results = template.findByNamedQuery (QUERY_GETALLPROVIDERS);

            HashSet<OAuthProvider>
                resultSet = new HashSet<OAuthProvider>();

            resultSet.addAll(results);

            return resultSet;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions
                (
                    MODE_SELECT,
                    "exception getting all providers",
                    OAuthProviderHibernateImpl.class.getSimpleName(),
                    new Object[] {},
                    new String[] {},
                    re
                );
        }
    }

    public OAuthProvider getOAuthProvider(String uuid)
        throws NoSuchObjectException, PersistenceException
    {
        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            return (OAuthProvider) template.load(OAuthProviderHibernateImpl.class, uuid);
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions
                    (
                        MODE_SELECT,
                        "exception getting provider",
                        OAuthProviderHibernateImpl.class.getSimpleName(),
                        new Object[]
                            {
                                uuid
                            },
                        new String[]
                            {
                                "String"
                            },
                        re
                    );
        }

    }
                   
    public boolean providerExists(String providerName)
        throws PersistenceException
    {
        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            List
                results = template.findByNamedQuery
                            (QUERY_PROVIDER_BY_NAME,
                             new Object[]
                             {
                                providerName
                             });

            return ( results != null && results.size() >0);
        }
        catch (ObjectNotFoundException onfe)
        {
            return false;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions
                    (
                        MODE_SELECT,
                        "exception checking for provider",
                        OAuthProviderHibernateImpl.class.getSimpleName(),
                        new Object[]
                            {
                                providerName
                            },
                        new String[]
                            {
                                "provider name"        
                            },
                        re
                    );
        }
    }
   

    public OAuthProvider getOAuthProviderByName (String providerName)
        throws NoSuchObjectException, PersistenceException
    {
        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            List
                results = template.findByNamedQuery
                            (QUERY_PROVIDER_BY_NAME,
                             new Object[]
                             {
                                providerName
                             });

            return (OAuthProvider) getSingleResult (results);
        }
        catch (AmbiguousResultException are)
        {
            are.setObjectType(OAuthTokenHibernateImpl.class.getSimpleName());

            are.setKeys
                (new Object[]
                 {
                     providerName
                 });
            are.setKeyTypes
                (new String[]
                 {
                     "OAuthProvider Name",
                 });

            throw are;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions
                (
                    MODE_SELECT,
                    "exception getting provider",
                    OAuthProviderHibernateImpl.class.getSimpleName(),
                    new Object[]
                        {
                            providerName
                        },
                    new String[]
                        {
                            "OAuthProvider Name"
                        },
                    re
                );
        }
    }

    public void updateOAuthProvider(OAuthProvider provider)
        throws NoSuchObjectException, PersistenceException
    {
        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            template.update(provider);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthProviderHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     provider
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "OAuthProvider"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception updatng OAuthProvider", re);
        }

    }

    public void updateOAuthProvider(String uuid,
                                    String providerName,
                                    String description,
                                    String authUrl,
                                    String tokenUrl,
                                    String clientId,
                                    String clientSecret,
                                    Map<String, String> additionalHeaders)
        throws NoSuchObjectException, PersistenceException
    {
        boolean
            enabled = getProviderStatus(providerName);

        updateOAuthProvider (uuid, providerName, description, authUrl, tokenUrl, clientId, clientSecret, additionalHeaders, enabled);

    }

    public void updateOAuthProvider(String uuid,
                                    String providerName,
                                    String description,
                                    String requestTokenURL,
                                    String userAuthorizationURL,
                                    String accessTokenURL,
                                    String realm,
                                    String consumerKey,
                                    String hmacSha1Key,
                                    String rsaSha1Key,
                                    OAuthSignatureMethod signingMethod,
                                    Map<String, String> additionalHeaders,
                                    boolean enabled)
            throws NoSuchObjectException, PersistenceException
    {
        assertSignatureMethodHasValidKey (signingMethod, hmacSha1Key, rsaSha1Key);

        OAuthProviderHibernateImpl
                provider = new OAuthProviderHibernateImpl();
        provider.setUUID(uuid);
        provider.setProviderName(providerName);
        provider.setDescription(description);
        provider.setRequestTokenURL(requestTokenURL);
        provider.setUserAuthorizationURL(userAuthorizationURL);
        provider.setAccessTokenURL(accessTokenURL);
        provider.setRealm(realm);
        provider.setConsumerKey(consumerKey);
        provider.setHmacSha1SharedSecret(hmacSha1Key);
        provider.setRsaSha1Key(rsaSha1Key);
        provider.setSignatureMethod(signingMethod);
        provider.setAdditionalHeaders(additionalHeaders);
        provider.setEnabled(enabled);

        updateOAuthProvider(provider);
        LOG.debug("Provider updated: " + provider.getUUID());

    }

    public void updateOAuthProvider(String uuid,
                                    String providerName,
                                    String description,
                                    String authUrl,
                                    String tokenUrl,
                                    String clientId,
                                    String clientSecret,
                                    Map<String, String> additionalHeaders,
                                    boolean enabled)
        throws NoSuchObjectException, PersistenceException
    {
        OAuthProviderHibernateImpl
            provider = new OAuthProviderHibernateImpl();
        provider.setUUID(uuid);
        provider.setProviderName(providerName);
        provider.setDescription(description);
        provider.setAuthUrl(authUrl);
        provider.setTokenUrl(tokenUrl);
        provider.setClientId(clientId);
        provider.setClientSecret(clientSecret);
        provider.setAdditionalHeaders(additionalHeaders);
        provider.setEnabled(enabled);

        updateOAuthProvider(provider);
        LOG.debug("Provider updated: " + provider.getUUID());

    }

    public void setAdditionalHeader(final String providerUUID, final String key, final String value)
         throws NoSuchObjectException, PersistenceException
    {
        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                OAuthProviderHibernateImpl
                    provider = (OAuthProviderHibernateImpl) session.load(OAuthProviderHibernateImpl.class,
                                                                         providerUUID);

                provider.getAdditionalHeaders().put(key, value);

                session.update(provider);

                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthProviderHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     providerUUID,
                     key,
                     value
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "UUID",
                     "header key",
                     "header value"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception setting additional header", re);
        }
    }

    public void setAdditionalHeaders(final String providerUUID, final Map<String, String> headers)
        throws NoSuchObjectException, PersistenceException
    {
        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                OAuthProviderHibernateImpl
                    provider = (OAuthProviderHibernateImpl) session.load(OAuthProviderHibernateImpl.class,
                                                                         providerUUID);

                Map<String, String>
                    oldHeaders = provider.getAdditionalHeaders();

                if (oldHeaders == null)
                {
                    oldHeaders = new HashMap<String, String>();
                    provider.setAdditionalHeaders(oldHeaders);
                }
                else
                {
                    oldHeaders.clear();
                }

                oldHeaders.putAll(headers);

                session.update(provider);

                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthProviderHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     providerUUID,
                     headers
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "UUID",
                     "headers"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception setting additional headers", re);
        }
    }

    public void deleteOAuthProvider(final String uuid)
        throws NoSuchObjectException, PersistenceException
    {
        if (uuid == null || uuid.length() < 1)
            throw new NullPointerException ("uuid cannot be null in deleteOAuthProvider(...)");

        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                try
                {
                    deleteOAuthTokensForProvider(uuid);
                }
                catch (PersistenceException e)
                {
                    throw (RuntimeException)e.getCause();
                }

                final OAuthProviderHibernateImpl
                    provider = (OAuthProviderHibernateImpl) session.load(OAuthProviderHibernateImpl.class, uuid);

                /* Clean up the dependent entities (additional headers) which have a foreign key relationship to this provider */
                try
                {
                    removeAdditionalHeaders(uuid);
                }
                catch (PersistenceException e)
                {
                    throw (RuntimeException)e.getCause();
                }

                session.delete(provider);
                /*
                Query
                    query = session.getNamedQuery(QUERY_DELETE_PROVIDER_BY_UUID);

                query.setString(0, uuid);


                query.executeUpdate();
                */
                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthProviderHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     uuid
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "OAuthProvider UUID"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception deleting OAuthProvider", re);
        }
    }

    public void removeAdditionalHeader(final String providerUUID, final String key)
        throws NoSuchObjectException, PersistenceException
    {
        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                OAuthProviderHibernateImpl
                    provider = (OAuthProviderHibernateImpl) session.load(OAuthProviderHibernateImpl.class,
                                                                         providerUUID);

                provider.getAdditionalHeaders().remove(key);

                session.update(provider);

                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthProviderHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     providerUUID,
                     key
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "UUID",
                     "header key"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception removing additional header", re);
        }
    }

    public void removeAdditionalHeaders(final String uuid)
        throws NoSuchObjectException, PersistenceException
    {
        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                Transaction
                    newTransaction = session.beginTransaction();

                OAuthProviderHibernateImpl

                    provider = (OAuthProviderHibernateImpl) session.load(OAuthProviderHibernateImpl.class,
                                                                         uuid);

                provider.getAdditionalHeaders().clear();

                session.update(provider);

                newTransaction.commit();
                
                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthProviderHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     uuid
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "UUID"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception removing additional headers", re);
        }
    }

    public void deleteOAuthProviders(final Set<String> uuids)
        throws NoSuchObjectException, PersistenceException
    {
        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                StringBuffer
                    sb = new StringBuffer();
                sb.append ("delete from OAuthProviderHibernateImpl as provider where provider.UUID in (");

                String
                    delim = "";

                for (String uuid : uuids)
                {
                    try
                    {
                        deleteOAuthTokensForProvider(uuid);
                    }
                    catch (PersistenceException e)
                    {
                        throw (RuntimeException)e.getCause();
                    }
                    
                    //since Hibernate will not automatically delete the additional headers we must do the work ourselves
                    try
                    {
                        removeAdditionalHeaders(uuid);
                    }
                    catch (PersistenceException e)
                    {
                        throw (RuntimeException)e.getCause();
                    }

                    sb.append(delim).append('\'').append(uuid).append('\'');
                    delim = ",";
                }

                sb.append(")");

                Query
                    query = session.createQuery(sb.toString());

                query.executeUpdate();

                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthProviderHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     uuids
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "UUID Set"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception deleting OAuthProviders", re);
        }
    }

    public OAuthToken createOAuthToken (final String providerUUID, final String tokenString, final String tokenSecret,
                                        final String userId)
        throws DataIntegrityException, PersistenceException
    {
        final HibernateCallback
            callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                    throws HibernateException, SQLException
                {
                    OAuthProviderHibernateImpl
                        provider = (OAuthProviderHibernateImpl)
                                   session.load(OAuthProviderHibernateImpl.class, providerUUID);

                    OAuthTokenHibernateImpl
                        token = new OAuthTokenHibernateImpl();

                    token.setProvider(provider);
                    token.setTokenValue(tokenString);
                    token.setTokenSecret(tokenSecret);
                    token.setUserId(userId);
                    token.setProtocolVersion("1.0");

                    session.save(token);

                    return token;
                }
            };

        try
        {
            return (OAuthToken)getHibernateTemplate().execute(callback);
        }
        catch (IllegalArgumentException iae)
        {
            throw new DataIntegrityException (iae.getMessage());
        }
        catch (DataIntegrityViolationException dive)
        {
            DataIntegrityException
               doe = new DataIntegrityException(dive);

            doe.setObjectType(OAuthTokenHibernateImpl.class.getSimpleName());

            doe.setKeys
                (new Object[]
                 {
                     providerUUID,
                     tokenString,
                     tokenSecret
                 });
            doe.setKeyTypes
                (new String[]
                 {
                     "provider UUID",
                     "token",
                     "token secret"
                 });
            throw doe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception creating OAuthToken", re);
        }
    }

    public OAuthToken createOAuth2Token (final String providerUUID, final String tokenString, final String userId)
            throws DataIntegrityException, PersistenceException
    {
        final HibernateCallback
                callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException
            {
                OAuthProviderHibernateImpl
                        provider = (OAuthProviderHibernateImpl)
                        session.load(OAuthProviderHibernateImpl.class, providerUUID);

                OAuthTokenHibernateImpl
                        token = new OAuthTokenHibernateImpl();

                token.setProvider(provider);
                token.setTokenValue(tokenString);
                token.setUserId(userId);
                token.setProtocolVersion("2.0");

                session.save(token);

                return token;
            }
        };

        try
        {
            return (OAuthToken)getHibernateTemplate().execute(callback);
        }
        catch (IllegalArgumentException iae)
        {
            throw new DataIntegrityException (iae.getMessage());
        }
        catch (DataIntegrityViolationException dive)
        {
            DataIntegrityException
                    doe = new DataIntegrityException(dive);

            doe.setObjectType(OAuthTokenHibernateImpl.class.getSimpleName());

            doe.setKeys
                    (new Object[]
                            {
                                    providerUUID,
                                    tokenString,
                            });
            doe.setKeyTypes
                    (new String[]
                            {
                                    "provider UUID",
                                    "token"
                            });
            throw doe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception creating OAuthToken", re);
        }
    }

    public OAuthToken getOAuthToken(String tokenUUID)
        throws NoSuchObjectException, PersistenceException
    {
        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            return (OAuthToken) template.load(OAuthTokenHibernateImpl.class, tokenUUID);
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions
                    (
                        MODE_SELECT,
                        "exception getting token",
                        OAuthTokenHibernateImpl.class.getSimpleName(),
                        new Object[]
                            {
                                tokenUUID
                            },
                        new String[]
                            {
                                "String"
                            },
                        re
                    );
        }

    }

    public OAuthToken getOAuthToken(String providerUUID, String userId)
        throws NoSuchObjectException, AmbiguousResultException, PersistenceException
    {
        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            List
                results = null;

            results = template.findByNamedQuery
                        (QUERY_TOKEN_BY_PROVIDER_USER,
                         new Object[]
                         {
                            providerUUID,
                            userId
                         });

            return (OAuthToken) getSingleResult (results);
        }
        catch (AmbiguousResultException are)
        {
            are.setObjectType(OAuthTokenHibernateImpl.class.getSimpleName());

            are.setKeys
                (new Object[]
                 {
                     providerUUID,
                     userId
                 });
            are.setKeyTypes
                (new String[]
                 {
                     "OAuthProvider UUID",
                     "User ID"
                 });

            throw are;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions
                    (
                        MODE_SELECT,
                        "exception getting provider",
                        OAuthProviderHibernateImpl.class.getSimpleName(),
                        new Object[]
                            {
                                providerUUID,
                                userId
                            },
                        new String[]
                            {
                                    "OAuthProvider UUID",
                                    "User ID"
                            },
                        re
                    );
        }
    }

    public Set<OAuthToken> findOAuthTokensForProvider(String providerUUID)
        throws NoSuchObjectException, PersistenceException
    {
        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            List
                results = null;

            results = template.findByNamedQuery
                        (QUERY_TOKENS_BY_PROVIDER,
                         new Object[]
                         {
                            providerUUID
                         });

            HashSet<OAuthToken>
                set = new HashSet<OAuthToken>();

            set.addAll(results);

            return set;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions
                    (
                        MODE_SELECT,
                        "exception getting provider",
                        OAuthProviderHibernateImpl.class.getSimpleName(),
                        new Object[]
                            {
                                providerUUID
                            },
                        new String[]
                            {
                                    "OAuthProvider UUID"
                            },
                        re
                    );
        }
    }

    public Set<OAuthToken> findOAuthTokensForUser(String userId)
        throws NoSuchObjectException, PersistenceException
    {
        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            List
                results = null;

            results = template.findByNamedQuery
                        (QUERY_TOKENS_BY_USER,
                         new Object[]
                         {
                            userId
                         });

            HashSet<OAuthToken>
                set = new HashSet<OAuthToken>();

            set.addAll(results);
            return set;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions
                    (
                        MODE_SELECT,
                        "exception getting tokens for user",
                        OAuthProviderHibernateImpl.class.getSimpleName(),
                        new Object[]
                            {
                                userId
                            },
                        new String[]
                            {
                                "User ID"
                            },
                        re
                    );
        }
    }

    public Set<OAuthToken> findOAuthTokens(String providerUUID, String scopeUUID, String userId) 
        throws PersistenceException
    {
        if (providerUUID == null && scopeUUID == null && userId == null)
            return null;

        ArrayList
            keyValues = new ArrayList();

        try
        {
            final HibernateTemplate
                template = getHibernateTemplate();

            final StringBuilder
                sb = new StringBuilder("from ").append(OAuthTokenHibernateImpl.class.getName()).append(" as token where ");

            if (providerUUID != null && providerUUID.length() > 0)
            {
                sb.append ("token.OAuthProvider.UUID = ?");
                keyValues.add(providerUUID);
            }
            if (sb.length() > 0)
            {
                sb.append(" and ");
            }
            sb.append ("token.OAuthProviderScope.UUID ");
            if (scopeUUID != null && scopeUUID.length() > 0)
            {
                sb.append ("= ?");
                keyValues.add(scopeUUID);
            }
            else
            {
                sb.append ("is null");
            }
            if (userId != null && userId.length() > 0)
            {
                if (sb.length() > 0)
                {
                    sb.append(" and ");
                }
                sb.append ("token.UserId = ?");
                keyValues.add(userId);
            }

            System.err.println ("find query: " + sb.toString());
            LOG.debug("find query: " + sb.toString());

            final List
                results = template.find(sb.toString(), keyValues.toArray());

            return new HashSet<OAuthToken>(results);
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception finding OAuthToken", re);
        }
    }

    public void deleteOAuthToken(final String tokenUUID)
        throws NoSuchObjectException, PersistenceException
    {
        if (tokenUUID == null || tokenUUID.length() < 1)
            throw new NullPointerException ("uuid cannot be null in deleteOAuthToken(...)");

        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                Query
                    query = session.getNamedQuery(QUERY_DELETE_TOKEN_BY_UUID);

                query.setString(0, tokenUUID);

                query.executeUpdate();

                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthTokenHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     tokenUUID
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "OAuthToken UUID"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception deleting OAuthToken", re);
        }
    }

    public void deleteOAuthTokensForUser(final String userId)
        throws NoSuchObjectException, PersistenceException
    {
        if (userId == null || userId.length() < 1)
            throw new NullPointerException ("userId cannot be null in deleteOAuthToken(...)");

        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                Query
                    query = session.getNamedQuery(QUERY_DELETE_TOKENS_BY_USERID);

                query.setString(0, userId);

                query.executeUpdate();

                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthTokenHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     userId
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "User ID"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception deleting OAuthToken", re);
        }
    }

    public void deleteOAuthTokensForProvider(final String providerUUID)
        throws NoSuchObjectException, PersistenceException
    {
        if (providerUUID == null || providerUUID.length() < 1)
            throw new NullPointerException ("providerUUID cannot be null in deleteOAuthTokensForProvider(...)");

        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                Query
                    query = session.getNamedQuery(QUERY_DELETE_TOKENS_BY_PROVIDER);

                query.setString(0, providerUUID);

                query.executeUpdate();

                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthTokenHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     providerUUID
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "Provider UUID"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception deleting OAuthToken", re);
        }
    }

    public void deleteOAuthTokens (final Set<String> listOfTokens)
        throws NoSuchObjectException, PersistenceException
    {

        final HibernateCallback
            callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
                throws HibernateException, SQLException
            {
                Query
                    query = session.getNamedQuery(QUERY_DELETE_TOKENS_LIST);

                query.setParameterList("list" , listOfTokens);

                query.executeUpdate();

                return null;
            }
        };

        try
        {
            getHibernateTemplate().execute(callback);
        }
        catch (ObjectNotFoundException onfe)
        {
            NoSuchObjectException nsoe = new NoSuchObjectException(onfe);

            nsoe.setObjectType(OAuthTokenHibernateImpl.class.getSimpleName());

            nsoe.setKeys
                (new Object[]
                 {
                     listOfTokens
                 });
            nsoe.setKeyTypes
                (new String[]
                 {
                     "Token UUIDs"
                 });

            throw nsoe;
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception deleting OAuthTokens", re);
        }

    }

    public boolean getProviderStatus(final String providerName)
        throws NoSuchObjectException, PersistenceException
    {
        try
        {
            List<Boolean>
                results = getHibernateTemplate().findByNamedQuery(QUERY_GET_PROVIDER_STATUS, new Object[] { providerName });

            if (results == null || results.size() < 1)
            {
                NoSuchObjectException
                    nsoe = new NoSuchObjectException("Provider not found in getProviderStatus(...)");

                nsoe.setObjectType (OAuthProviderHibernateImpl.class.toString());
                nsoe.setKeys (new String [] { providerName });
                nsoe.setKeyTypes (new String [] { "Provider Name (String)" } );

                throw nsoe;
            }

            Boolean
               value = results.get(0);

            return value.booleanValue();
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception getting status of OAuth Provider", re);
        }
    }

    public void setProviderStatus(final String providerName, final boolean status)
        throws NoSuchObjectException, PersistenceException
    {
        try
        {
            Query
                query = getSession().getNamedQuery(QUERY_SET_PROVIDER_STATUS);

            int
                updated = getHibernateTemplate().bulkUpdate(query.getQueryString(),
                                                            new Object[]
                                                                {
                                                                    Boolean.valueOf(status),
                                                                    providerName
                                                                });

            if (updated == 0)
            {
                NoSuchObjectException nsoe = new NoSuchObjectException();

                nsoe.setObjectType(OAuthTokenHibernateImpl.class.getSimpleName());

                nsoe.setKeys
                    (new Object[]
                     {
                         providerName
                     });
                nsoe.setKeyTypes
                    (new String[]
                     {
                         "Provider Name (String)"
                     });

                throw nsoe;
            }
        }
        catch (RuntimeException re)
        {
            throw catchAllPersistenceExceptions("Unexpected exception setting status of OAuth Provider", re);
        }
    }
}
