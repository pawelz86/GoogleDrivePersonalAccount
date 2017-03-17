/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pawelz.pl.googledrivepersonalaccount.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.model.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pawelz.pl.googledrivepersonalaccount.config.DriveHelper;
import pawelz.pl.googledrivepersonalaccount.util.FileDictionary;

/**
 *
 * @author p.zachwieja
 */
@RestController
@RequestMapping("/api/google")

@Configuration
@ConfigurationProperties(prefix = "drive")
public class Controller {
    private static final Logger LOGGER = LogManager.getLogger(Controller.class);
    
    private String sessionHeaderName;
    
    private String appName;
    
    @Inject
    private GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity listFiles(
            HttpServletRequest request, 
            @RequestParam(name="folderId", required = true) String folderId, 
            @RequestParam(name="pageSize", defaultValue = "10") int pageSize, 
            @RequestParam(name="nextPageToken", required = false) String nextPageToken
                                    ) throws IOException, GeneralSecurityException {
        
        LOGGER.debug("Listing files, got header with session id: " + request.getHeader(sessionHeaderName));
        Map<String, Object> result = new HashMap<>();
        result.put("content", new DriveHelper(appName, googleAuthorizationCodeFlow, request.getHeader(sessionHeaderName)).listFiles(folderId, pageSize, nextPageToken));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseEntity deleteFile(HttpServletRequest request, @RequestBody String fileId) throws IOException {
        new DriveHelper(appName, googleAuthorizationCodeFlow, request.getHeader(sessionHeaderName)).deleteFile(fileId);
        return new ResponseEntity<>(new HashMap<>().put("message", "File deleted"), HttpStatus.OK);
    }

    @RequestMapping(value = "/getFile", method = RequestMethod.POST)
    public void donwloadFile(HttpServletRequest request, @RequestParam("fileId") String fileId, HttpServletResponse response) throws IOException {
        Get get = new DriveHelper(appName, googleAuthorizationCodeFlow, request.getHeader(sessionHeaderName)).getDrive().files().get(fileId);
        File file = get.execute();
        String mimeType = "application/octet-stream";
        response.setContentType(mimeType);
        String headerKey = "Content-Disposition";
        String headerValue = null;
        headerValue = String.format("attachment; filename=\"%s\"", file.getName());
        response.setHeader(headerKey, headerValue);
        FileCopyUtils.copy(
                get.executeMediaAsInputStream(), 
                response.getOutputStream()
        );
    }

    @RequestMapping(value = "/shareFile", method = RequestMethod.POST)
    public ResponseEntity shareFile(HttpServletRequest request, @RequestBody Map<String, Object> param) throws IOException {
        StringTokenizer st = null;
        Map<String, Object> result = new HashMap<>();
        DriveHelper drive = new DriveHelper(appName, googleAuthorizationCodeFlow, request.getHeader(sessionHeaderName));
        for (Map<String, String> token: (List<Map<String, String>>) param.get(FileDictionary.SHARES.getCode())) {
            drive.shareFile(
                    token.get(FileDictionary.FILEID.getCode()), 
                    token.get(FileDictionary.EMAIL.getCode()), 
                    token.get(FileDictionary.ROLE.getCode()), 
                    token.get(FileDictionary.TYPE.getCode()),
                    Boolean.parseBoolean(token.get(FileDictionary.SENDEMAIL.getCode())),
                    token.get(FileDictionary.EMAILMESSAGE.getCode())
            );
        }
        result.put("message", "OK");
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @RequestMapping(value = "/folderCreate", method = RequestMethod.POST)
    public File createFolder(HttpServletRequest request, @RequestBody Map<String, Object> newFile) throws IOException {
        File folder = null;
        DriveHelper drive = new DriveHelper(appName, googleAuthorizationCodeFlow, request.getHeader(sessionHeaderName));
        drive.createFolder(
                newFile.get(FileDictionary.NAME.getCode()).toString(), 
                newFile.get(FileDictionary.PARENTID.getCode()).toString()
        );
        return folder;
    }

    @RequestMapping(value = "/file/upload", method = RequestMethod.POST)
    public File handleFileUpload(HttpServletRequest request, @RequestParam("idFolderu") String idFolderu,
                                @RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        File wynik = null;
        if (!file.isEmpty()) {
            DriveHelper drive = new DriveHelper(appName, googleAuthorizationCodeFlow, request.getHeader(sessionHeaderName));
            drive.uploadFileOnly(true, file, idFolderu, "");
        } else {
            LOGGER.warn("File " + file.getOriginalFilename() + " was empty.");
            return null;
        }
        return wynik;
    }

    public void setSessionHeaderName(String sessionHeaderName) {
        this.sessionHeaderName = sessionHeaderName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    
}
