/*
 * Copyright 2011 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): duffy
 */

package com.rsmart.oauth.provider.tool;

import com.rsmart.oauth.provider.tool.api.OAuthProviderService;
import com.rsmart.oauth.provider.tool.util.OAuthAdminToolState;
import com.rsmart.oauth.provider.tool.util.ProviderErrors;
import com.rsmart.oauth.api.BaseOAuthProvider;
import com.rsmart.oauth.api.OAuthProvider;
import com.rsmart.oauth.api.OAuthSignatureMethod;
import com.rsmart.persistence.PersistenceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * This class control all the functionality for the Oauth tool CRUD operations using MultiActionController
 * <p/>
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Apr 8, 2010
 * Time: 6:41:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class OAuthProviderAdminController
    extends MultiActionController
{
    private static final Log
        log = LogFactory.getLog(OAuthProviderAdminController.class);

    private static final String
        CRUD_KEY = "crud",
        STATE_KEY = "oAuthAdminToolState",
        VIEW_EDIT = "editProvider",
        VIEW_ADD = "addProvider",
        VIEW_PROVIDERS = "oauthDisplayProviders";

    private OAuthProviderService
        oAuthProviderServiceImpl;

    private boolean
        requiredFieldsCheck = false;

    private static ResourceBundle
        rb = ResourceBundle.getBundle("com.rsmart.oauth.provider.Messages");

    public OAuthProviderAdminController()
    {
        //no code here
    }

    public OAuthProviderService getoAuthProviderServiceImpl()
    {
        return oAuthProviderServiceImpl;
    }

    public void setoAuthProviderServiceImpl(OAuthProviderService oAuthProviderServiceImpl)
    {
        this.oAuthProviderServiceImpl = oAuthProviderServiceImpl;
    }

    /**
     * Displays the provider list from the menu
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView oauthDisplayProviders(HttpServletRequest request,
                                              HttpServletResponse response)
        throws Exception
    {
        OAuthAdminToolState
            state = OAuthAdminToolState.getState();

        //CLE-6788 - need to make sure no providers are selected after a refresh
        state.reset();

        state.setProviders(oAuthProviderServiceImpl.getProviders());

        return new ModelAndView(VIEW_PROVIDERS, STATE_KEY, state);
    }

    /**
     * This method edits the provider
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView beginEdit(HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        OAuthAdminToolState
            state = OAuthAdminToolState.getState();
        
        String
            queryValue = null;

        queryValue = request.getParameter("uuid");
        if ("none".equals(queryValue))
        {
            state.getProviderErrors().setCheckBoxCheckedErrorMessage(rb.getString("please.click.on.a.provider.to.edit.information"));
            return new ModelAndView (VIEW_PROVIDERS, STATE_KEY, state);
        }
        else
        {
            OAuthProvider
                provider = null;

            try
            {
                provider = oAuthProviderServiceImpl.getProviderByUUID(queryValue);
            }
            catch (PersistenceException e)
            {
            }

            if (provider == null)
            {
                state.getProviderErrors().setCheckBoxCheckedErrorMessage(rb.getString("error.loading.provider"));
                return new ModelAndView (VIEW_PROVIDERS, STATE_KEY, state);
            }

            state.setCurrentProvider(provider);
        }

        return new ModelAndView(VIEW_EDIT, STATE_KEY, state);
    }

    /**
     * This method show all the information of provider including the additional parameters
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView showAllDataForProvider(HttpServletRequest request, HttpServletResponse response)
    {
        OAuthAdminToolState
            state = OAuthAdminToolState.getState();

        String queryValue = request.getParameter("uuid");

        if (queryValue == null || queryValue.equals("")) {
            try
            {
                state.getProviderErrors().setCheckBoxCheckedErrorMessage(rb.getString("please.click.on.a.provider.to.view.information"));
            } catch (Exception e) {
            }
        }
        else
        {
            try {
                state.setCurrentProvider(oAuthProviderServiceImpl.getProviderByUUID(queryValue));

            } catch (Exception e) {
            }
        }

        return new ModelAndView("showAllDataForProvider", STATE_KEY, state);
    }

    private void updateStateFromCommandObject (OAuthAdminToolState newState)
    {
        OAuthAdminToolState
            state = OAuthAdminToolState.getState();

        state.getProviderErrors().clear();

        BaseOAuthProvider
            currentProvider = (BaseOAuthProvider) state.getCurrentProvider();
        OAuthProvider
            provider = newState.getCurrentProvider();

        currentProvider.setAccessTokenURL(provider.getAccessTokenURL());
        currentProvider.setConsumerKey(provider.getConsumerKey());
        currentProvider.setDescription(provider.getDescription());
        currentProvider.setEnabled(provider.isEnabled());
        currentProvider.setHmacSha1SharedSecret(provider.getHmacSha1SharedSecret());
        currentProvider.setProviderName(provider.getProviderName());
        currentProvider.setRealm(provider.getRealm());
        currentProvider.setRequestTokenURL(provider.getRequestTokenURL());
        currentProvider.setRsaSha1Key(provider.getRsaSha1Key());
        currentProvider.setSignatureMethod(provider.getSignatureMethod());
        currentProvider.setUserAuthorizationURL(provider.getUserAuthorizationURL());
        currentProvider.setUUID(provider.getUUID());
        // DO NOT set the additional headers from the command object - the values
        //   in the additional header map are not reflected in the form, so this
        //   would effectively delete all of the additional header values
        //
        // currentProvider.setAdditionalHeaders(provider.getAdditionalHeaders());

        state.setNewAdditionalHeaderKey(newState.getNewAdditionalHeaderKey());
        state.setNewAdditionalHeaderValue(newState.getNewAdditionalHeaderValue());
    }

    /**
     * This method displays provider that was checked from the providers' list page to edit information
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView editProvider(HttpServletRequest request, HttpServletResponse response, OAuthAdminToolState newState)
        throws Exception
    {
        updateStateFromCommandObject (newState);

        OAuthAdminToolState
            state = OAuthAdminToolState.getState();

        final String
            add = request.getParameter("add"),
            delete = request.getParameter("delete");

        // This is code for when a user clicks the add button to add an additional Parameter
        if (add != null)
        {
            return addAdditionalHeader(VIEW_EDIT);
        }
        else if (delete != null)
        {
            return deleteAdditionalHeader(VIEW_EDIT);
        }
        // Update was clicked - store all the values for this provider
        else
        {
            try
            {
                // this code is Provider has been validated but a parameter hasn't been modified-meaning edit and add are false
                validateRequiredFields(state);
                if ( requiredFieldsCheck )
                {
                    requiredFieldsCheck = false;
                    return new ModelAndView(VIEW_EDIT, STATE_KEY, state) ;
                }

                oAuthProviderServiceImpl.updateOAuthProvider(state.getCurrentProvider());
                state.reset();
            }
            catch (Exception e)
            {
                log.debug("Exception in " + this.getClass().getName() + "Method:editProvider() " + e.getMessage());
            }

            return oauthDisplayProviders(request, response);
        }

    }

    public ModelAndView newProvider(HttpServletRequest request, HttpServletResponse response)
    {
        return new ModelAndView (VIEW_ADD, STATE_KEY, OAuthAdminToolState.getState());    
    }

    private ModelAndView addAdditionalHeader(String view)
        throws Exception
    {
        OAuthAdminToolState
            state = OAuthAdminToolState.getState();

        String
            key = state.getNewAdditionalHeaderKey(),
            value = state.getNewAdditionalHeaderValue();

        state.setNewAdditionalHeaderKey(null);
        state.setNewAdditionalHeaderValue(null);

        //checks to see if anyone of the parameter values are blank
        if ( key == null || key.trim().length() < 1 || value == null || value.trim().length() < 1)
        {
            state.getProviderErrors().setParameterValuesNull(rb.getString("when.adding.parameters.both.fields.need.values"));
        }
        else
        {
            Map<String, String>
                addtlHeaders = state.getCurrentProvider().getAdditionalHeaders();

            //Checks to see if values exist in MAP....If not doing check the value from the key will be changed so this keeps it from doing that
            if (addtlHeaders.containsKey(key))
            {
                state.getProviderErrors().setParameterNameAlreadyExists(rb.getString("parameter.name.already.exist"));
            }
            else
            {
                state.getCurrentProvider().getAdditionalHeaders().put(key, value);
            }
        }
        return new ModelAndView (view, STATE_KEY, state);
    }

    private ModelAndView deleteAdditionalHeader(String view)
    {
        OAuthAdminToolState
            state = OAuthAdminToolState.getState();

        state.getCurrentProvider().getAdditionalHeaders().remove(state.getNewAdditionalHeaderKey());

        state.setNewAdditionalHeaderKey(null);
        state.setNewAdditionalHeaderValue(null);

        return new ModelAndView(view, STATE_KEY, state);
    }

    /**
     * This method saves a provider
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView addProvider(HttpServletRequest request, HttpServletResponse response, OAuthAdminToolState newState)
        throws Exception
    {
        updateStateFromCommandObject (newState);

        OAuthAdminToolState
            state = OAuthAdminToolState.getState();

        final String
            delete = request.getParameter("delete"),
            add = request.getParameter ("add");

        if (request.getParameter("cancel") != null)
        {
            OAuthAdminToolState.clear();
        }
        else if (delete != null)
        {
            return deleteAdditionalHeader(VIEW_ADD);
        }
        else if (add != null)
        {
            return addAdditionalHeader(VIEW_ADD);
        }
        else
        {

            //checks to validate if names exists
            if ( checkIfNameAlreadyExits(state) )
            {
                state.getProviderErrors().setProviderdNameExistMessageError(rb.getString("error.provider.name.already.exist"));
                return new ModelAndView(VIEW_ADD, STATE_KEY, state);
            }

            //Inside validateRequiredFields Method requiredFieldsCheck is set
            validateRequiredFields(state);

            if ( requiredFieldsCheck ) {
                requiredFieldsCheck= false;
                return new ModelAndView(VIEW_ADD, STATE_KEY, state);
            }

            //if isAdd is true means there are parameters that will be added as well as the provider
            try
            {
                oAuthProviderServiceImpl.addProvider(state.getCurrentProvider());

                state.reset();
            } catch (Exception e2) {
                log.error("Exception in OAuthProviderAdminController:addProvider()" + e2.getMessage());
            }

        }
        return oauthDisplayProviders (request, response);

/*
            else if ("edit".equals(crud))
            {
                Map<String, String>
                    addtlHeaders = state.getCurrentProvider().getAdditionalHeaders();

                //Checks to see if values exist in MAP....If not doing check the value from the key will be changed so this keeps it from doing that
                if (addtlHeaders.containsKey(state.getNewAdditionalHeaderKey()))
                {
                    state.getProviderErrors().setParameterNameAlreadyExists(rb.getString("parameter.name.already.exist"));
                    return new ModelAndView(VIEW_ADD, STATE_KEY, state);
                }
            }
            */

     //   return new ModelAndView(VIEW_PROVIDERS, STATE_KEY, state);
    }


    /**
     * This method check if the Additional parameter key is unique, if this code is not checked value will get overridden
     * @param nameKey
     * @param oauthParameters
     * @return
     */
    private boolean checkIfParameterNamesExist(String nameKey, Map oauthParameters) {
        boolean isExist = false;
        if (oauthParameters == null) {
            isExist = false;
        } else if (oauthParameters.containsKey(nameKey)) {
            isExist = true;
        }
        return isExist;
    }

    /**
     * This deletes a provider
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView deleteProvider(HttpServletRequest request, HttpServletResponse response)
    {
        OAuthAdminToolState
            state = OAuthAdminToolState.getState();
        String
            uuid = request.getParameter("uuid");

        //if there is only one box checked
        if ("none".equals(uuid))
        {
            try
            {
                state.getProviderErrors().setCheckBoxCheckedErrorMessage(rb.getString("error.message.delete.checked"));
            }
            catch (Exception eN)
            {
               logger.debug("Exception in OAuthProviderAdminController:deleteProvider" + eN.getMessage());
            }
        }
        else
        {
            try {
                oAuthProviderServiceImpl.deleteProviders(addValuesToSet(uuid));
            } catch (Exception e) {
                logger.debug("Exception in OAuthProviderAdminController:deleteProvider" + e.getMessage());
            }
        }

        try
        {
            state.setProviders(oAuthProviderServiceImpl.getProviders());
        } catch (PersistenceException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return new ModelAndView(VIEW_PROVIDERS, STATE_KEY, state);

    }

    /**
     * valiates if a providers Name has already been used
     *
     * @return
     */
    private boolean checkIfNameAlreadyExits(OAuthAdminToolState state) {
        boolean providerNameExists = false;

        String
            name = state.getCurrentProvider().getProviderName();

        if (name != null && !name.equals("")) {
            providerNameExists = oAuthProviderServiceImpl.providerNameExists(name);
            if (providerNameExists) {
                state.getProviderErrors().setDoesProviderNameExits(providerNameExists);
            }

        }
        return providerNameExists;
    }

    public void validateRequiredFields (OAuthAdminToolState state)
    {
        OAuthProvider
            provider = state.getCurrentProvider();
        ProviderErrors
            requiredErrors = state.getProviderErrors();
        if (provider.getProviderName() != null && provider.getProviderName().equals("")) {
            requiredFieldsCheck = true;
            requiredErrors.setProvidersNameMessageError(rb.getString("error.required.field"));
        }
        if (provider.getDescription() != null && provider.getDescription().equals("")){
            requiredFieldsCheck = true;
            requiredErrors.setProviderDescriptionMessageError(rb.getString("error.required.field"));
        }

        if (provider.getConsumerKey() != null && provider.getConsumerKey().equals("")) {
            requiredFieldsCheck = true;
            requiredErrors.setProviderConsumerKeyMessageError(rb.getString("error.required.field"));
        }
        if (OAuthSignatureMethod.HMAC_SHA1.equals(provider.getSignatureMethod()) &&
                provider.getHmacSha1SharedSecret() != null && provider.getHmacSha1SharedSecret().equals(""))
        {
            requiredFieldsCheck = true;
            requiredErrors.setProviderHmacSha1SharedSecretMessageError (rb.getString("error.required.field"));
        }
        if (OAuthSignatureMethod.RSA_SHA1.equals(provider.getSignatureMethod()) &&
                provider.getRsaSha1Key() != null && provider.getRsaSha1Key().equals(""))
        {
            requiredFieldsCheck = true;
            requiredErrors.setProviderRsaSha1KeyMessageError (rb.getString("error.required.field"));
        }
        if (provider.getAccessTokenURL() != null && provider.getAccessTokenURL().equals("")) {
            requiredFieldsCheck = true;
            requiredErrors.setProviderAccessTokenUrlMessageError(rb.getString("error.required.field"));
        }
        if (provider.getRequestTokenURL() != null && provider.getRequestTokenURL().equals("")) {
            requiredFieldsCheck = true;
            requiredErrors.setProviderRequestTokenUrlMessageError(rb.getString("error.required.field"));
        }
        if (provider.getUserAuthorizationURL() != null && provider.getUserAuthorizationURL().equals("")) {
            requiredFieldsCheck = true;
            requiredErrors.setProviderUserAuthorizationUrlMessageError(rb.getString("error.required.field"));
        }
        state.setProviderErrors(requiredErrors);
    }

    /**
     * Parsing the string to delete from List
     *
     * @param uuids
     * @return
     */
    private Set addValuesToSet(String uuids) {
        Set parsedValues = new HashSet();
        String[] spiltValues = uuids.split(",");
        for (int i = 0; i < spiltValues.length; i++) {
            if (spiltValues[i].length() > 0) {
                parsedValues.add(spiltValues[i]);
            }
        }

        return parsedValues;
    }

}