/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pawelz.pl.googledrivepersonalaccount.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pawelz.pl.googledrivepersonalaccount.config.DriveHelper;

/**
 *
 * @author pawelz
 */
@RestController
@RequestMapping("/api/google")
@ConfigurationProperties(prefix = "drive")
public class AccountController {
    
    private String baseUri;
    
    private String redirectTo;
    
    private String appName;
    
    @Inject
    private GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;
    
    private String sessionHeaderName;
    private String errorPage;

    private static final Logger LOGGER = LogManager.getLogger(AccountController.class);
    
    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public String getAccount(HttpServletRequest request) {
        String user = null;
        String sessionId = request.getHeader(sessionHeaderName);
        if(StringUtils.hasText(sessionId))
            user = getDriveEmail(sessionId);
        return user;
    }
    
    /**
     *
     * @param sessionId
     * @param res
     * @return
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public List<String> authenticate(HttpServletResponse res) {
        return Arrays.asList(googleAuthorizationCodeFlow.newAuthorizationUrl().setRedirectUri(redirectTo).build());
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity logout(HttpServletRequest request) throws IOException, GeneralSecurityException {
        googleAuthorizationCodeFlow.getCredentialDataStore().delete(request.getHeader(sessionHeaderName));
        return new ResponseEntity(new HashMap<>().put("state", "login"), HttpStatus.OK);
    }

    @RequestMapping(value = "/session", method = RequestMethod.GET)
    public ResponseEntity saveSession(@RequestParam("code") String code, HttpServletResponse res) throws IOException {
        SecureRandom random = new SecureRandom();
        String h = null;
        Credential credential = null;
        try {
            do {
                h = new BigInteger(130, random).toString(32);
            }
            while(googleAuthorizationCodeFlow.loadCredential(h) != null);
            
            credential = exchangeCode(code, h);
            LOGGER.debug("Credentials saved for id: " + h + ", expires in " + credential.getExpiresInSeconds() + " seconds");
        }
        catch (TokenResponseException ex){
            LOGGER.error(ex.getLocalizedMessage());
            ex.printStackTrace(System.err);
            res.sendRedirect(errorPage);
        } 
        catch (IOException | GeneralSecurityException ex) {
            LOGGER.error(ex.getLocalizedMessage());
            ex.printStackTrace(System.err);
            res.sendRedirect(errorPage);
        }
        res.setStatus(HttpStatus.NO_CONTENT.value());
        res.setHeader(sessionHeaderName, h);
        HttpHeaders headers = new HttpHeaders();
        headers.add(sessionHeaderName, h);
        return new ResponseEntity<>(null, headers, HttpStatus.OK);
    }
    
    /**
     * Exchange an authorization code for OAuth 2.0 credentials.
     *
     * @param authorizationCode Authorization code to exchange for OAuth 2.0
     * credentials.
     * @return OAuth 2.0 credentials.
     * @throws CodeExchangeException An error occurred.
     */
    private Credential exchangeCode(String authorizationCode, String sId) throws GeneralSecurityException //      throws CodeExchangeException 
    {
        try {
            GoogleTokenResponse response = googleAuthorizationCodeFlow
                    .newTokenRequest(authorizationCode)
                    .setRedirectUri(redirectTo)
                    .setGrantType("authorization_code")
                    .execute();
            LOGGER.debug("Saving credentials for id: " + sId);
            return googleAuthorizationCodeFlow.createAndStoreCredential(response, sId);
        } catch (IOException e) {
            LOGGER.error("An error occurred: " + e.getLocalizedMessage());
            return null;
        }
    }
    
    
    
    private String getDriveEmail(String sessionId) {
        String result= null;
        try {
            
            result = new DriveHelper(appName, googleAuthorizationCodeFlow, sessionId).getDrive().about().get().execute().getUser().getEmailAddress();
        } catch (IOException ex) {
        } finally {
        }

        return result;
    }

    public void setBaseUri(String uri) {
        this.baseUri = uri;
    }

    public void setSessionHeaderName(String sessionHeaderName) {
        this.sessionHeaderName = sessionHeaderName;
    }

    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    
    
}
