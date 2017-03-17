/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pawelz.pl.googledrivepersonalaccount.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Permissions.Create;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author pawelz
 */
public class DriveHelper {
    private Drive drive;
    private final String folderMimeType = "application/vnd.google-apps.folder";
    private static final Logger LOGGER = LogManager.getLogger(DriveHelper.class);
    
    /**
     * Creates Drive to manipulate user files
     * @param googleAuthorizationCodeFlow GoogleAuthorizationCodeFlow object
     * @param appName Name of the app
     * @param sessionId Id of a stored credentials
     */
    public DriveHelper (String appName, GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow, String sessionId){
        LOGGER.debug("Initianting DriveHelper object");
        Drive result = null;
        try {
            Credential c = googleAuthorizationCodeFlow.loadCredential(sessionId);
            result = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), 
                                        JacksonFactory.getDefaultInstance(), 
                                        googleAuthorizationCodeFlow.loadCredential(sessionId))
                    .setApplicationName(appName)
                    .build();
            this.drive = result;
        } catch (IOException | GeneralSecurityException ex) {
        } finally {
        };
        LOGGER.debug("DriveHelper object created");
    }
    
    /**
     * Gets file with given id
     * @param id Id of file to get
     * @param fields Fields to get, if null fields are id,webContentLink,name,webViewLink,parents,permissions
     * @return Founded file
     */
    public File getFile(String id, String fields) throws IOException {
        LOGGER.debug("Getting file for id: " + id);
        File result = null;
        Drive.Files.Get get;
        if(fields == null){
            fields = "id,webContentLink,name,webViewLink,parents,permissions";
        }
        try {
            get = drive.files().get(id);
            get.setFields(fields);
            result = get.execute();
        }
         catch (GoogleJsonResponseException e){
            if(e.getStatusCode() == 404){
                 LOGGER.debug("File not found: " + id);
            }
            else if(e.getStatusCode() == 401){
                 LOGGER.debug("Unauthorized: " + id);
            }
            else {
                 LOGGER.error("Google sends response error: " + e.getMessage());
                 e.printStackTrace(System.err);
            }
        }
        LOGGER.debug("Returning file with id: " + id);
        return result;
    }
    
    /**
     * Gets child list for a parent with given id, this finds all files and folders located directly in the folder
     * @param id Id of file to get
     * @param pageSize Max number of elements to get from sinlge request
     * @param pageToken Google page token indicating page to take
     * @return Map with keys: 'content' and 'nextPageToken' if exists
     */
    public Map<String, Object> listFiles(String id, int pageSize, String pageToken) throws IOException {
        LOGGER.debug("Listing files for parentId: " + id + ", page size: " + pageSize + ", pageToken: " + pageToken);
        Map<String, Object> result = new HashMap<>();

        Drive.Files.List request = drive.files().list();
        request.setQ("\'" + id+"\' in parents and trashed = false");
        request.setPageSize(pageSize);
        if(StringUtils.hasText(pageToken))
            request.setPageToken(pageToken);
        FileList files = request.execute();
        result.put("content", files);
        if(request.getPageToken() != null && request.getPageToken().length() > 0)
            result.put("nextPageToken", files.getNextPageToken());
        LOGGER.debug("Returning files, size: " + files.size());
        return result;
    }
    
    /**
     * Deletes file from Google Drive
     * @param fileId File id to remove
     * @return 0 on success
     */
    public int deleteFile(String fileId) {
        int result = -1;
        try {
            LOGGER.debug("Deleting file id: " + fileId);
            drive.files().delete(fileId).execute();
            LOGGER.debug("File deleted successfully: " + fileId);
            result = 0;
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                LOGGER.warn("No files found for " + fileId);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            LOGGER.error("Error occurred: " + e);
        }
        return result;
    }
    
    /**
     * Shares a file with given user
     * @param fileId
     * @param email E-mail address
     * @param role Role, one of writer, reader, owner is not applicable
     * @param type Type one of user - group - domain - anyone
     * @param sendEmail Should user get notification e-mail
     * @param message E-mail message
     * @throws IOException IOException
     */
    public void shareFile(String fileId, String email, String role, String type, boolean sendEmail, String message) throws IOException{
        LOGGER.debug("Sharing file " + fileId + " for " + email);
        Permission permission = new Permission();
        permission.setEmailAddress(email);
        permission.set("value", email);
        permission.setRole(role);
        permission.setType(type);
        Create create = drive.permissions().create(fileId, permission);
        create.setSendNotificationEmail(sendEmail);
        if(StringUtils.hasText(message))
            create.setEmailMessage(message);
        create.execute();
        LOGGER.debug("File " + fileId + " for " + email + " shared");
    }
    
    /**
     * 
     * @param name Name of folder to create
     * @return File object
     */
    public File createFolder(String name, String parentId) throws IOException {
        LOGGER.debug("Creating folder " + name);
        File fileMetadata = new File();
        File file = null;
        fileMetadata.setName(name);
        fileMetadata.setMimeType(folderMimeType);
        
        try {
            fileMetadata.setParents(Arrays.asList(parentId));
            file = drive.files().create(fileMetadata).execute();
        } 
        catch (SocketTimeoutException ex) {
            LOGGER.info("Socket timeout while creating folder " + name);
            return null;
        }

        LOGGER.debug("Folder " + name + " created! ID: " + file.getId());
        return file;
    }
    
    /**
     * Uploads file to Google Drive
     * @param useDirectUpload useDirectUpload
     * @param multipartFile Multupart file from browser
     * @param folderId Parent folder id
     * @param desc Description for file
     * @return Google file object of uploaded MultiparFile
     * @throws IOException IOException
     */
    public File uploadFileOnly(boolean useDirectUpload, 
                            MultipartFile multipartFile,
                            String folderId,
                            String desc) throws IOException {
        LOGGER.debug("Uploading file to Drive");
        File fileMetadata = new File();
        fileMetadata.setName(multipartFile.getName());
        fileMetadata.setMimeType(multipartFile.getContentType());
        fileMetadata.setOriginalFilename(multipartFile.getOriginalFilename());
        
        InputStreamContent mediaContent = new InputStreamContent(multipartFile.getContentType(),
                                                                new BufferedInputStream(multipartFile.getInputStream()));

        File p = new File();
        p.setId(folderId);
        fileMetadata.setParents(Arrays.asList(folderId));
        
        if ((desc != null) && (!desc.isEmpty())) {
            fileMetadata.setDescription(desc);
        }
        
        
        fileMetadata = drive.files().create(fileMetadata, mediaContent).execute();
        LOGGER.info("File uploaded, id: " + fileMetadata.getId());

        return fileMetadata;
    }
    
    public Drive getDrive() {
        return drive;
    }
}
