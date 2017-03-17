/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pawelz.pl.googledrivepersonalaccount.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author pawelz
 */
@Configuration
@ConfigurationProperties(prefix = "drive")
public class GoogleAuthCodeFlow {
    
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static HttpTransport HTTP_TRANSPORT;
    private String jsonPath;
    private String credentialsPath;
    private java.io.File DATA_STORE_DIR;
    
    /**
     * Build an authorization flow and store it as a static class attribute.
     *
     * @return GoogleAuthorizationCodeFlow instance.
     * @throws IOException Unable to load client_secret.json.
     */
    @Bean
    public GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow() throws IOException, GeneralSecurityException {
        GoogleClientSecrets clientSecret = null;
        GoogleAuthorizationCodeFlow flow = null;
        DATA_STORE_DIR = new java.io.File(credentialsPath);
        DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        clientSecret
                = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(new java.io.File(jsonPath))));
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        flow = new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecret,
            Arrays.asList(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE))
            .setDataStoreFactory(DATA_STORE_FACTORY)
            .setAccessType("offline")
//                .setMethod(BearerToken.authorizationHeaderAccessMethod())
//              .setApprovalPrompt("force")

                .build();

        
        return flow;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    
}
