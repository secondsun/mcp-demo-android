package org.feedhenry.mcp.mcp_demo.helper;


import android.app.Activity;

import org.jboss.aerogear.android.authorization.AuthorizationManager;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthorizationConfiguration;
import org.jboss.aerogear.android.authorization.oauth2.OAuthWebViewDialog;
import org.jboss.aerogear.android.core.Callback;

import java.net.URL;

/**
 * Created by summers on 11/10/17.
 */

public class KeycloakHelper {


    private final String moduleName;

    private KeycloakHelper(Builder builder) {
        this.moduleName = builder.getModuleName();
        try {
            AuthorizationManager.config(builder.getModuleName(), OAuth2AuthorizationConfiguration.class)
                    .setBaseURL(new URL(builder.getAuthzUrl()))
                    .setAuthzEndpoint(builder.getAuthzEndpoint())
                    .setAccessTokenEndpoint(builder.getAccessTokenEndpoint())
                    .setRefreshEndpoint(builder.getRefreshTokenEndpoint())
                    .setAccountId(builder.getAccountId())
                    .setClientId(builder.getClientId())
                    .setRedirectURL(builder.getRedirectURL())
                    .asModule();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public  void connect(final Activity activity, final Callback callback) {
        try {
            final AuthzModule authzModule = AuthorizationManager.getModule(moduleName);

            authzModule.requestAccess(activity, new Callback<String>() {
                @SuppressWarnings("unchecked")
                @Override
                public void onSuccess(String s) {
                    callback.onSuccess(s);
                }

                @Override
                public void onFailure(Exception e) {
                    if (!e.getMessage().matches(OAuthWebViewDialog.OAuthReceiver.DISMISS_ERROR)) {
                        authzModule.deleteAccount();
                    }
                    callback.onFailure(e);
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public  boolean isConnected() {
        return AuthorizationManager.getModule(moduleName).isAuthorized();
    }

    public String getBearerToken() {
        if (isConnected()) {
            return AuthorizationManager.getModule(moduleName).loadModule(null,null,null).getHeaders().get(0).second;
        }

        return null;
    }

    public static  class Builder {

        private String moduleName= "KeyCloakAuthz";
        private String authzUrl ="http://10.0.2.2:8080/auth";
        private String accountId= "keycloak-token";
        private String clientId = "android";
        private String redirectURL= "http://oauth2callback";
        private String realm;

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public String getAuthzUrl() {
            return authzUrl;
        }

        public void setAuthzUrl(String authzUrl) {
            this.authzUrl = authzUrl;
        }

        public String getAuthzEndpoint() {
            return "/realms/" + realm + "/protocol/openid-connect/auth";
        }

        public String getAccessTokenEndpoint() {
            return "/realms/" + realm + "/protocol/openid-connect/token";
        }

        public String getRefreshTokenEndpoint() {
            return "/realms/" + realm + "/protocol/openid-connect/token";
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getRedirectURL() {
            return redirectURL;
        }

        public void setRedirectURL(String redirectURL) {
            this.redirectURL = redirectURL;
        }

        public KeycloakHelper build() {
            return new KeycloakHelper(this);
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public String getRealm() {
            return realm;
        }
    }
}
