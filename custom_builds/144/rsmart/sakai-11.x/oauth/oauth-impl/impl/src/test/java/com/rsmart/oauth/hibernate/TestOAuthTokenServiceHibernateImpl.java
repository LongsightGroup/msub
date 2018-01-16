package com.rsmart.oauth.hibernate;

import com.rsmart.oauth.api.OAuthSignatureMethod;
import com.rsmart.oauth.api.OAuthTokenService;
import com.rsmart.oauth.api.OAuthProvider;
import com.rsmart.oauth.api.OAuthToken;
import com.rsmart.persistence.PersistenceException;
import com.rsmart.persistence.NoSuchObjectException;
import com.rsmart.util.testing.spring.SpringUnitTest;
import org.junit.Test;
import org.junit.Assert;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
 * User: duffy
 * Date: Jan 21, 2010
 * Time: 3:53:13 PM
 */
public class TestOAuthTokenServiceHibernateImpl extends SpringUnitTest
{
    private OAuthTokenService getOAuthTokenService()
    {
        return (OAuthTokenService)getBeanFromContext("com.rsmart.oauth.hibernate.OAuthTokenServiceHibernateImpl");
    }

    private String getRsaSha1TestKey()
    {
        return (String)getBeanFromContext("rsasha1testkey");
    }

    private String getRsaSha1TestCert()
    {
        return (String)getBeanFromContext("rsasha1testcert");
    }

    private final void assertException (ExceptionCheck ec, Class type, String msg)
    {
        Throwable
            caught = null;

        try
        {
            ec.checkForException();
        }
        catch(Throwable t)
        {
            if (type.isAssignableFrom(t.getClass()))
                return;

            caught = t;
        }

        StringBuilder
            sb = new StringBuilder();

        sb.append ("Exception expected of type '").append(type.getName()).append("'");

        if (caught != null)
            sb.append (", caught '").append(caught.getClass().getName()).append("'");
        
        if (msg != null && msg.length() > 0)
            sb.append(": ").append(msg);

        Assert.fail (sb.toString());
    }

    private final void assertNoException (ExceptionCheck ec, String msg)
    {
        try
        {
            ec.checkForException();;
        }
        catch(Throwable t)
        {
            StringBuilder
                sb = new StringBuilder();

            sb.append ("No exception expected, but got '").append(t.getClass().getName()).append("'");

            if (msg != null && msg.length() > 0)
                sb.append (": ").append(msg);

            Assert.fail (sb.toString());
        }
    }

    interface ExceptionCheck
    {
        public void checkForException() throws Exception;
    }

    private final OAuthProviderHibernateImpl newExampleProvider() throws Exception
    {
        OAuthProviderHibernateImpl
            provider = new OAuthProviderHibernateImpl();

        provider.setConsumerKey("cle.rsmart.com");
        provider.setDescription("This is an example description");
        provider.setHmacSha1SharedSecret("consumer secret");
        provider.setRsaSha1Key(getRsaSha1TestKey());
        provider.setSignatureMethod(OAuthSignatureMethod.HMAC_SHA1);
        provider.setAccessTokenURL("google.com/testaccesstokenurl");
        provider.setRequestTokenURL("google.com/testrequesttokenurl");
        provider.setUserAuthorizationURL("google.com/testuserauthorizationurl");
        Map<String,String>
            properties = new HashMap<String,String>();

        properties.put ("test property", "test value");

        provider.setAdditionalHeaders(properties);
        provider.setRealm("google.com");
        provider.setProviderName("Google");
        provider.setEnabled(true);

        return provider;
    }

    private final OAuthProviderHibernateImpl createProvider (OAuthTokenService svc, OAuthProviderHibernateImpl provider)
        throws Exception
    {
        return (OAuthProviderHibernateImpl)
                svc.createOAuthProvider (provider.getProviderName(), provider.getDescription(), provider.getRequestTokenURL(),
                                         provider.getUserAuthorizationURL(), provider.getAccessTokenURL(),
                                         provider.getRealm(), provider.getConsumerKey(),
                                         provider.getHmacSha1SharedSecret(), provider.getRsaSha1Key(), provider.getSignatureMethod(),
                                         provider.getAdditionalHeaders(), provider.isEnabled());
    }

    private final OAuthTokenHibernateImpl newExampleToken(OAuthProviderHibernateImpl provider)
        throws Exception
    {
        OAuthTokenHibernateImpl
            token = new OAuthTokenHibernateImpl();

        token.setProvider(provider);
        token.setTokenSecret("tokenSecret");
        token.setTokenValue ("token");
        token.setUserId("userId");

        return token;
    }

    private final OAuthTokenHibernateImpl createToken (OAuthTokenService svc, OAuthTokenHibernateImpl token)
        throws Exception
    {
        return (OAuthTokenHibernateImpl)
                svc.createOAuthToken (token.getProvider().getUUID(), token.getTokenValue(),
                                      token.getTokenSecret(), token.getUserId());
    }

    @Test
    public void testCreateGetAndDeleteProviderSucceeds() throws Exception
    {
        final OAuthTokenService
            tokenService = getOAuthTokenService();

        OAuthProvider
            provider = createProvider(tokenService, newExampleProvider());

        Assert.assertNotNull(provider.getUUID());

        OAuthProvider
            providerResult = tokenService.getOAuthProvider(provider.getUUID());

        Assert.assertEquals(provider, providerResult);
        Assert.assertEquals(provider.getConsumerKey(), providerResult.getConsumerKey());
        Assert.assertEquals(provider.getConsumerSecret(), providerResult.getConsumerSecret());
        Assert.assertEquals(provider.getHmacSha1SharedSecret(), providerResult.getHmacSha1SharedSecret());
        Assert.assertEquals(provider.getRsaSha1Key(), providerResult.getRsaSha1Key());
        Assert.assertEquals(provider.getSignatureMethod(), providerResult.getSignatureMethod());
        Assert.assertEquals(provider.getAccessTokenURL(), providerResult.getAccessTokenURL());
        Assert.assertEquals(provider.getUserAuthorizationURL(), providerResult.getUserAuthorizationURL());
        Assert.assertEquals(provider.getRequestTokenURL(), providerResult.getRequestTokenURL());
        Assert.assertEquals(provider.getProviderName(), providerResult.getProviderName());
        Assert.assertEquals(provider.getDescription(), providerResult.getDescription());
        Assert.assertEquals(provider.getRealm(), providerResult.getRealm());

        Map<String, String>
            properties = provider.getAdditionalHeaders();

        Assert.assertNotNull(properties);
        Assert.assertEquals(properties.size(), 1);
        Assert.assertEquals(properties.get("test property"), "test value");


        final String
            uuid = providerResult.getUUID();

        tokenService.deleteOAuthProvider(providerResult.getUUID());

        assertException (
            new ExceptionCheck()
            {
                public void checkForException() throws Exception
                {
                    tokenService.getOAuthProvider(uuid);
                }
            },
            NoSuchObjectException.class,
            "Deleted provider still available via get");
    }

    @Test
    public void testCreateProviderRespectsUniqueConstraints() throws Exception
    {
        final OAuthTokenService
            tokenService = getOAuthTokenService();

        final OAuthProviderHibernateImpl
            provider = createProvider(tokenService, newExampleProvider());

        String
            uuid = provider.getUUID();

        provider.setUUID(null);

        assertException (
            new ExceptionCheck()
            {
                public void checkForException() throws Exception
                {
                    createProvider(tokenService, provider);
                }
            },
            PersistenceException.class,
            "duplicate provider");

        tokenService.deleteOAuthProvider(uuid);
    }

    @Test
    public void testCreateGetAndDeleteTokenSucceeds() throws Exception
    {
        final OAuthTokenService
            tokenService = getOAuthTokenService();

        OAuthProviderHibernateImpl
            provider = createProvider(tokenService, newExampleProvider());

        OAuthTokenHibernateImpl
            token = createToken(tokenService, newExampleToken (provider));

        Assert.assertNotNull(token.getUUID());

        OAuthToken
            tokenResult = tokenService.getOAuthToken(token.getUUID());

        Assert.assertEquals(token, tokenResult);
        Assert.assertEquals(token.getProvider(), tokenResult.getProvider());
        Assert.assertEquals(token.getTokenValue(), tokenResult.getTokenValue());
        Assert.assertEquals(token.getTokenSecret(), tokenResult.getTokenSecret());
        Assert.assertEquals(token.getUserId(), tokenResult.getUserId());

        tokenResult = tokenService.getOAuthToken(provider.getUUID(), "userId");

        Assert.assertEquals(token, tokenResult);
        Assert.assertEquals(token.getProvider(), tokenResult.getProvider());
        Assert.assertEquals(token.getTokenValue(), tokenResult.getTokenValue());
        Assert.assertEquals(token.getTokenSecret(), tokenResult.getTokenSecret());
        Assert.assertEquals(token.getUserId(), tokenResult.getUserId());

        final String
            uuid = tokenResult.getUUID();

        tokenService.deleteOAuthToken(uuid);

        assertException (
            new ExceptionCheck()
            {
                public void checkForException() throws Exception
                {
                    tokenService.getOAuthToken(uuid);
                }
            },
            NoSuchObjectException.class,
            "Deleted token still available via get");
    }

    @Test
    public void testCreateTokenRespectsUniqueConstraints() throws Exception
    {
        final OAuthTokenService
            tokenService = getOAuthTokenService();

        final OAuthProviderHibernateImpl
            provider = createProvider(tokenService, newExampleProvider());

        final OAuthTokenHibernateImpl
            token = createToken(tokenService, newExampleToken(provider));

        String
            uuid = token.getUUID();

        token.setUUID(null);

        assertException (
            new ExceptionCheck()
            {
                public void checkForException() throws Exception
                {
                    createToken(tokenService, token);
                }
            },
            PersistenceException.class,
            "duplicate token (no scope)");

        tokenService.deleteOAuthToken(uuid);
    }

    @Test
    public void testFindOAuthTokenProducesExpectedResults ()
        throws Exception
    {
        OAuthProviderHibernateImpl
            provider1 = new OAuthProviderHibernateImpl(),
            provider2 = new OAuthProviderHibernateImpl();

        final OAuthTokenService
            tokenService = getOAuthTokenService();

        provider1.setConsumerKey("cle.rsmart.com 1");
        provider1.setConsumerSecret("consumer secret 1");
        provider1.setAccessTokenURL("google.com/testaccesstokenurl 1");
        provider1.setRequestTokenURL("google.com/testrequesttokenurl 1");
        provider1.setUserAuthorizationURL("google.com/testuserauthorizationurl 1");
        Map<String,String>
            properties = new HashMap<String,String>();

        properties.put ("test property", "test value");

        provider1.setAdditionalHeaders(properties);
        provider1.setRealm("google.com 1");
        provider1.setProviderName("Google 1");

        provider1 = createProvider(tokenService, provider1);

        provider2.setConsumerKey("cle.rsmart.com 2");
        provider2.setConsumerSecret("consumer secret 2");
        provider2.setAccessTokenURL("google.com/testaccesstokenurl 2");
        provider2.setRequestTokenURL("google.com/testrequesttokenurl 2");
        provider2.setUserAuthorizationURL("google.com/testuserauthorizationurl 2");
        Map<String,String>
            properties2 = new HashMap<String,String>();

        properties.put ("test property", "test value");

        provider2.setAdditionalHeaders(properties2);
        provider2.setRealm("google.com 2");
        provider2.setProviderName("Google 2");

        provider2 = createProvider(tokenService, provider2);

        OAuthTokenHibernateImpl
            token1 = new OAuthTokenHibernateImpl(),
            token2 = new OAuthTokenHibernateImpl(),
            token3 = new OAuthTokenHibernateImpl();

        token1.setTokenValue("token 1");
        token1.setUserId("user 1");
        token1.setProvider(provider1);
        token1.setTokenSecret("token secret 1");

        token1 = createToken (tokenService, token1);

        token2.setTokenValue("token 2");
        token2.setUserId("user 1");
        token2.setProvider(provider2);
        token2.setTokenSecret("token secret 2");

        token2 = createToken (tokenService, token2);

        token3.setTokenValue("token 3");
        token3.setUserId("user 2");
        token3.setProvider(provider2);
        token3.setTokenSecret("token secret 3");

        token3 = createToken (tokenService, token3);

        Set<OAuthToken>
            tokens = null;

        //test - find all tokens for provider 1
        tokens = tokenService.findOAuthTokensForProvider(provider1.getUUID());

        Assert.assertTrue(tokens.size() == 1);
        Assert.assertTrue(tokens.contains(token1));

        //test - find all tokens for provider 2
        tokens = tokenService.findOAuthTokensForProvider(provider2.getUUID());

        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.contains(token2));
        Assert.assertTrue(tokens.contains(token3));

        //test - find all tokens for user1
        tokens = tokenService.findOAuthTokensForUser("user 1");

        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.contains(token1));
        Assert.assertTrue(tokens.contains(token2));

        //test - find token for user2
        tokens = tokenService.findOAuthTokensForUser("user 2");

        Assert.assertTrue(tokens.size() == 1);
        Assert.assertTrue(tokens.contains(token3));

        //test - non-existant provider
        tokens = tokenService.findOAuthTokensForProvider("foobar");

        Assert.assertTrue(tokens.size() == 0);

        //test - non-existant user
        tokens = tokenService.findOAuthTokensForUser("foobar");

        Assert.assertTrue(tokens.size() == 0);

        tokenService.deleteOAuthProvider(provider1.getUUID());
        tokenService.deleteOAuthProvider(provider2.getUUID());
    }

    @Test
    public void testGetAllOAuthProvidersReturnsFullSet()
        throws Exception
    {
        OAuthProviderHibernateImpl
             provider1 = new OAuthProviderHibernateImpl(),
             provider2 = new OAuthProviderHibernateImpl(),
             provider3 = new OAuthProviderHibernateImpl();

        final OAuthTokenService
             tokenService = getOAuthTokenService();

        provider1.setConsumerKey("cle.rsmart.com 1");
        provider1.setConsumerSecret("consumer secret 1");
        provider1.setAccessTokenURL("google.com/testaccesstokenurl 1");
        provider1.setRequestTokenURL("google.com/testrequesttokenurl 1");
        provider1.setUserAuthorizationURL("google.com/testuserauthorizationurl 1");
        Map<String,String>
             properties = new HashMap<String,String>();

        properties.put ("test property", "test value");

        provider1.setAdditionalHeaders(properties);
        provider1.setRealm("google.com 1");
        provider1.setProviderName("Google 1");

        provider1 = createProvider(tokenService, provider1);

        provider2.setConsumerKey("cle.rsmart.com 2");
        provider2.setConsumerSecret("consumer secret 2");
        provider2.setAccessTokenURL("google.com/testaccesstokenurl 2");
        provider2.setRequestTokenURL("google.com/testrequesttokenurl 2");
        provider2.setUserAuthorizationURL("google.com/testuserauthorizationurl 2");
        Map<String,String>
             properties2 = new HashMap<String,String>();

        properties.put ("test property", "test value");

        provider2.setAdditionalHeaders(properties2);
        provider2.setRealm("google.com 2");
        provider2.setProviderName("Google 2");

        provider2 = createProvider(tokenService, provider2);

        provider3.setConsumerKey("cle.rsmart.com 3");
        provider3.setConsumerSecret("consumer secret 3");
        provider3.setAccessTokenURL("google.com/testaccesstokenurl 3");
        provider3.setRequestTokenURL("google.com/testrequesttokenurl 3");
        provider3.setUserAuthorizationURL("google.com/testuserauthorizationurl 3");
        Map<String,String>
             properties3 = new HashMap<String,String>();

        properties.put ("test property", "test value");

        provider3.setAdditionalHeaders(properties3);
        provider3.setRealm("google.com 3");
        provider3.setProviderName("Google 3");

        provider3 = createProvider(tokenService, provider3);

        Set<OAuthProvider>
            results = tokenService.getAllOAuthProviders();

        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.size());
        Assert.assertTrue(results.contains(provider1));
        Assert.assertTrue(results.contains(provider2));
        Assert.assertTrue(results.contains(provider3));

        tokenService.deleteOAuthProvider(provider1.getUUID());
        tokenService.deleteOAuthProvider(provider2.getUUID());
        tokenService.deleteOAuthProvider(provider3.getUUID());
    }

    @Test
    public void testDeleteOAuthProvidersDeletesAppropriateObjects()
        throws Exception
    {
        OAuthProviderHibernateImpl
             provider1 = new OAuthProviderHibernateImpl(),
             provider2 = new OAuthProviderHibernateImpl(),
             provider3 = new OAuthProviderHibernateImpl();

        final OAuthTokenService
             tokenService = getOAuthTokenService();

        provider1.setConsumerKey("cle.rsmart.com 1");
        provider1.setConsumerSecret("consumer secret 1");
        provider1.setAccessTokenURL("google.com/testaccesstokenurl 1");
        provider1.setRequestTokenURL("google.com/testrequesttokenurl 1");
        provider1.setUserAuthorizationURL("google.com/testuserauthorizationurl 1");
        Map<String,String>
             properties = new HashMap<String,String>();

        properties.put ("test property", "test value");

        provider1.setAdditionalHeaders(properties);
        provider1.setRealm("google.com 1");
        provider1.setProviderName("Google 1");

        provider1 = createProvider(tokenService, provider1);

        provider2.setConsumerKey("cle.rsmart.com 2");
        provider2.setConsumerSecret("consumer secret 2");
        provider2.setAccessTokenURL("google.com/testaccesstokenurl 2");
        provider2.setRequestTokenURL("google.com/testrequesttokenurl 2");
        provider2.setUserAuthorizationURL("google.com/testuserauthorizationurl 2");
        Map<String,String>
             properties2 = new HashMap<String,String>();

        properties.put ("test property", "test value");

        provider2.setAdditionalHeaders(properties2);
        provider2.setRealm("google.com 2");
        provider2.setProviderName("Google 2");

        provider2 = createProvider(tokenService, provider2);

        provider3.setConsumerKey("cle.rsmart.com 3");
        provider3.setConsumerSecret("consumer secret 3");
        provider3.setAccessTokenURL("google.com/testaccesstokenurl 3");
        provider3.setRequestTokenURL("google.com/testrequesttokenurl 3");
        provider3.setUserAuthorizationURL("google.com/testuserauthorizationurl 3");
        Map<String,String>
             properties3 = new HashMap<String,String>();

        properties.put ("test property", "test value");

        provider3.setAdditionalHeaders(properties3);
        provider3.setRealm("google.com 3");
        provider3.setProviderName("Google 3");

        provider3 = createProvider(tokenService, provider3);

        HashSet<String>
            uuids = new HashSet<String>();

        uuids.add(provider2.getUUID());
        uuids.add(provider3.getUUID());

        tokenService.deleteOAuthProviders(uuids);

        Set<OAuthProvider>
            remaining = tokenService.getAllOAuthProviders();

        Assert.assertNotNull (remaining);
        Assert.assertEquals (1, remaining.size());
        Assert.assertTrue (remaining.contains(provider1));
        
        tokenService.deleteOAuthProvider(provider1.getUUID());
    }

    @Test
    public void testUpdateProviderAltersValuesAppropriately()
        throws Exception
    {
        OAuthTokenService
            tokenService = getOAuthTokenService();

        OAuthProviderHibernateImpl
            provider = createProvider (tokenService, newExampleProvider());
        String
            uuid = provider.getUUID();

        provider.setAccessTokenURL("changed value");
        provider.getAdditionalHeaders().clear();
        provider.getAdditionalHeaders().put("new header key", "new header value");

        tokenService.updateOAuthProvider(provider);
        
        provider = (OAuthProviderHibernateImpl)tokenService.getOAuthProvider (uuid);

        Assert.assertNotNull(provider);
        Assert.assertEquals ("changed value", provider.getAccessTokenURL());

        Map<String, String>
            headers = provider.getAdditionalHeaders();

        Assert.assertNotNull (headers);

        Assert.assertEquals (1, headers.size());
        Assert.assertTrue (headers.containsKey("new header key"));

        tokenService.deleteOAuthProvider(uuid);
    }

    @Test
    public void testRemoveHeader()
        throws Exception
    {
        OAuthTokenService
            tokenService = getOAuthTokenService();

        OAuthProviderHibernateImpl
            provider = createProvider (tokenService, newExampleProvider());

        tokenService.removeAdditionalHeader(provider.getUUID(), "test property");

        provider = (OAuthProviderHibernateImpl)tokenService.getOAuthProvider(provider.getUUID());

        Assert.assertNotNull(provider.getAdditionalHeaders());
        Assert.assertNull(provider.getAdditionalHeaders().get("test property"));

        tokenService.deleteOAuthProvider(provider.getUUID());
    }

    @Test
    public void testRemoveAllHeaders()
        throws Exception
    {
        OAuthTokenService
            tokenService = getOAuthTokenService();

        OAuthProviderHibernateImpl
            provider = createProvider (tokenService, newExampleProvider());

        provider.getAdditionalHeaders().put("another key", "another value");
        tokenService.updateOAuthProvider(provider);

        tokenService.removeAdditionalHeaders(provider.getUUID());

        provider = (OAuthProviderHibernateImpl)tokenService.getOAuthProvider(provider.getUUID());

        Assert.assertNotNull(provider.getAdditionalHeaders());
        Assert.assertTrue(provider.getAdditionalHeaders().isEmpty());

        tokenService.deleteOAuthProvider(provider.getUUID());
    }

    @Test
    public void testSetHeaderAddsOrReplacesValue ()
        throws Exception
    {
        OAuthTokenService
            tokenService = getOAuthTokenService();

        OAuthProviderHibernateImpl
            provider = createProvider (tokenService, newExampleProvider());

        tokenService.setAdditionalHeader(provider.getUUID(), "test property", "new value");
        tokenService.setAdditionalHeader(provider.getUUID(), "another property", "another value");

        OAuthProvider
            result = tokenService.getOAuthProvider(provider.getUUID());

        Map<String, String>
            headers = result.getAdditionalHeaders();

        Assert.assertNotNull (headers);
        Assert.assertEquals (2, headers.size());
        Assert.assertEquals ("new value", headers.get("test property"));
        Assert.assertEquals ("another value", headers.get("another property"));

        tokenService.deleteOAuthProvider(provider.getUUID());
    }

    @Test
    public void testSetHeadersUpdatesValues ()
        throws Exception
    {
        OAuthTokenService
            tokenService = getOAuthTokenService();

        OAuthProviderHibernateImpl
            provider = createProvider (tokenService, newExampleProvider());

        Map<String, String>
            newHeaders = new HashMap<String, String>();

        newHeaders.put ("new key 1", "new val 1");
        newHeaders.put ("new key 2", "new val 2");

        tokenService.setAdditionalHeaders(provider.getUUID(), newHeaders);

        OAuthProvider
            result = tokenService.getOAuthProvider(provider.getUUID());

        Map<String, String>
            headers = result.getAdditionalHeaders();

        Assert.assertNotNull (headers);
        Assert.assertEquals (2, headers.size());
        Assert.assertEquals ("new val 1", headers.get("new key 1"));
        Assert.assertEquals ("new val 2", headers.get("new key 2"));

        tokenService.deleteOAuthProvider(provider.getUUID());
    }

    @Test
    public void testSetAndGetProviderStatus()
        throws Exception
    {
        final OAuthTokenService
            tokenService = getOAuthTokenService();

        final OAuthProviderHibernateImpl
            provider = createProvider (tokenService, newExampleProvider());

        Assert.assertTrue(tokenService.getProviderStatus(provider.getProviderName()));

        tokenService.setProviderStatus(provider.getProviderName(), false);

        Assert.assertFalse(tokenService.getProviderStatus(provider.getProviderName()));

        tokenService.deleteOAuthProvider(provider.getUUID());

        assertException (
            new ExceptionCheck()
            {
                public void checkForException() throws Exception
                {
                    tokenService.getProviderStatus(provider.getProviderName());
                }
            },
            NoSuchObjectException.class,
            "no provider with the given provider name");

        assertException (
            new ExceptionCheck()
            {
                public void checkForException() throws Exception
                {
                    tokenService.setProviderStatus(provider.getProviderName(), false);
                }
            },
            NoSuchObjectException.class,
            "no provider with the given provider name");
        
    }
}
