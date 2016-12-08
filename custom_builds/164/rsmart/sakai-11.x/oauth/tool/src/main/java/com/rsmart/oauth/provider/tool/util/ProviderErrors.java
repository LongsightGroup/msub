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

package com.rsmart.oauth.provider.tool.util;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Apr 8, 2010
 * Time: 2:26:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProviderErrors {

    private boolean doesProviderNameExits;
    private boolean requiredErrors;
    private String providerdNameExistMessageError;
    private String providersNameMessageError;
    private String providerClientIdMessageError;
    private String providerClientSecretMessageError;
    private String providerAuthUrlMessageError;
    private String providerTokenUrlMessageError;
    private String providerConsumerKeyMessageError;
    private String providerConsumerSecretMessageError;
    private String providerAccessTokenUrlMessageError;
    private String providerRequestTokenUrlMessageError;
    private String providerUserAuthorizationUrlMessageError;
    private String checkBoxCheckedErrorMessage;
    private String parameterNameAlreadyExists;
    private String parameterValuesNull;
    private String providerDescriptionMessageError;
    private String providerHmacSha1SharedSecretMessageError;
    private String providerRsaSha1KeyMessageError;

    public boolean isDoesProviderNameExits() {
        return doesProviderNameExits;
    }

    public void setDoesProviderNameExits(boolean doesProviderNameExits) {
        this.doesProviderNameExits = doesProviderNameExits;
    }

    public String getProviderClientIdMessageError() {
        return providerClientIdMessageError;
    }

    public void setProviderClientIdMessageError(String providerClientIdMessageError) {
        this.providerClientIdMessageError = providerClientIdMessageError;
    }

    public String getProviderClientSecretMessageError() {
        return providerClientSecretMessageError;
    }

    public void setProviderClientSecretMessageError(String providerClientSecretMessageError) {
        this.providerClientSecretMessageError = providerClientSecretMessageError;
    }

    public String getProviderAuthUrlMessageError() {
        return providerAuthUrlMessageError;
    }

    public void setProviderAuthUrlMessageError(String providerAuthUrlMessageError) {
        this.providerAuthUrlMessageError = providerAuthUrlMessageError;
    }

    public String getProviderTokenUrlMessageError() {
        return providerTokenUrlMessageError;
    }

    public void setProviderTokenUrlMessageError(String providerTokenUrlMessageError) {
        this.providerTokenUrlMessageError = providerTokenUrlMessageError;
    }

    public String getProviderdNameExistMessageError() {
        return providerdNameExistMessageError;
    }

    public void setProviderdNameExistMessageError(String providerdNameExistMessageError) {
        this.providerdNameExistMessageError = providerdNameExistMessageError;
    }

    public String getProviderConsumerKeyMessageError() {
        return providerConsumerKeyMessageError;
    }

    public void setProviderConsumerKeyMessageError(String providerConsumerKeyMessageError) {
        this.providerConsumerKeyMessageError = providerConsumerKeyMessageError;
    }

    public String getProviderConsumerSecretMessageError() {
        return providerConsumerSecretMessageError;
    }

    public void setProviderConsumerSecretMessageError(String providerConsumerSecretMessageError) {
        this.providerConsumerSecretMessageError = providerConsumerSecretMessageError;
    }

    public String getProviderAccessTokenUrlMessageError() {
        return providerAccessTokenUrlMessageError;
    }

    public void setProviderAccessTokenUrlMessageError(String providerAccessTokenUrlMessageError) {
        this.providerAccessTokenUrlMessageError = providerAccessTokenUrlMessageError;
    }

    public String getProviderRequestTokenUrlMessageError() {
        return providerRequestTokenUrlMessageError;
    }

    public void setProviderRequestTokenUrlMessageError(String providerRequestTokenUrlMessageError) {
        this.providerRequestTokenUrlMessageError = providerRequestTokenUrlMessageError;
    }

    public String getProviderUserAuthorizationUrlMessageError() {
        return providerUserAuthorizationUrlMessageError;
    }

    public void setProviderUserAuthorizationUrlMessageError(String providerUserAuthorizationUrlMessageError) {
        this.providerUserAuthorizationUrlMessageError = providerUserAuthorizationUrlMessageError;
    }

    public boolean isRequiredErrors() {
        return requiredErrors;
    }

    public void setRequiredErrors(boolean requiredErrors) {
        this.requiredErrors = requiredErrors;
    }

    public String getProvidersNameMessageError() {
        return providersNameMessageError;
    }

    public void setProvidersNameMessageError(String providersNameMessageError) {
        this.providersNameMessageError = providersNameMessageError;
    }

    public String getCheckBoxCheckedErrorMessage() {
        return checkBoxCheckedErrorMessage;
    }

    public void setCheckBoxCheckedErrorMessage(String checkBoxCheckedErrorMessage) {
        this.checkBoxCheckedErrorMessage = checkBoxCheckedErrorMessage;
    }

    public String getParameterNameAlreadyExists() {
        return parameterNameAlreadyExists;
    }

    public void setParameterNameAlreadyExists(String parameterNameAlreadyExists) {
        this.parameterNameAlreadyExists = parameterNameAlreadyExists;
    }

    public String getParameterValuesNull() {
        return parameterValuesNull;
    }

    public void setParameterValuesNull(String parameterValuesNull) {
        this.parameterValuesNull = parameterValuesNull;
    }

    public String getProviderDescriptionMessageError() {
        return providerDescriptionMessageError;
    }

    public void setProviderDescriptionMessageError(String providerDescriptionMessageError) {
        this.providerDescriptionMessageError = providerDescriptionMessageError;
    }

    public String getProviderHmacSha1SharedSecretMessageError() {
        return providerHmacSha1SharedSecretMessageError;
    }

    public void setProviderHmacSha1SharedSecretMessageError(String providerHmacSha1SharedSecretMessageError) {
        this.providerHmacSha1SharedSecretMessageError = providerHmacSha1SharedSecretMessageError;
    }

    public String getProviderRsaSha1KeyMessageError() {
        return providerRsaSha1KeyMessageError;
    }

    public void setProviderRsaSha1KeyMessageError(String providerRsaSha1KeyMessageError) {
        this.providerRsaSha1KeyMessageError = providerRsaSha1KeyMessageError;
    }

    public void clear() {
        doesProviderNameExits = false;
        requiredErrors = false;
        providerdNameExistMessageError = null;
        providersNameMessageError = null;
        providerClientIdMessageError = null;
        providerClientSecretMessageError = null;
        providerAuthUrlMessageError = null;
        providerTokenUrlMessageError = null;
        providerConsumerKeyMessageError = null;
        providerConsumerSecretMessageError = null;
        providerAccessTokenUrlMessageError = null;
        providerRequestTokenUrlMessageError = null;
        providerUserAuthorizationUrlMessageError = null;
        checkBoxCheckedErrorMessage = null;
        parameterNameAlreadyExists = null;
        parameterValuesNull = null;
        providerDescriptionMessageError = null;
        providerHmacSha1SharedSecretMessageError = null;
        providerRsaSha1KeyMessageError = null;
    }
}
