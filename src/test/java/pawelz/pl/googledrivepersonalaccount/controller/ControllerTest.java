/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pawelz.pl.googledrivepersonalaccount.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;
import pawelz.pl.googledrivepersonalaccount.config.DriveHelper;

/**
 *
 * @author pawelz
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("dev")
@ContextConfiguration(locations = {
    "/TestContext.xml", 
}, initializers = ConfigFileApplicationContextInitializer.class)
@SpringBootTest
public class ControllerTest {
        
        //Session id is valid for 1 hour after authentification
        private final String sessionId = "session id";
        
        private final String parentId = "0B7oHAESBL5cyWFppbTY0OHd1VW8";
        
        private final String myShareTestEmail = "sharewithemail";
        
        private final String listTestId = "parent folder";
        
        private final String appName = "personalApp";
        
        @Inject
        private GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;
    
    public ControllerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws NoSuchFieldException {
//        ReflectionTestUtils.setField(pawelz.pl.googledrivepersonalaccount.config.DriveHelper.class, "appName", "personalApp", String.class);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of listFile method, of class Controller.
     */
    @Test
    public void testListFile() throws Exception {
        DriveHelper drive = new DriveHelper(appName, googleAuthorizationCodeFlow, sessionId);
        Map<String, Object> result = drive.listFiles(listTestId, 5, null);
        FileList filesPage = (FileList) result.get("content");
        List<File> files = filesPage.getFiles();
        String nextPageToken = filesPage.getNextPageToken();
        assertEquals(4, files.size());
    }

    /**
     * Test of donwloadFile method, of class Controller.
     */
    @Test
    public void testDonwloadFile() throws Exception {
        System.out.println("No test for downloading a file");
    }

    /**
     * Test of shareFile method, of class Controller.
     */
    @Test
    public void testShareFile() throws Exception {
        DriveHelper drive = new DriveHelper(appName, googleAuthorizationCodeFlow, sessionId);
        File result = drive.createFolder("meTestFolder", parentId);
        assertNotNull(result);
        drive.shareFile(result.getId(), myShareTestEmail , "reader", "group", false, null);
        result = drive.getFile(result.getId(), "id,permissions");
        List<Permission> permissions = result.getPermissions();
        permissions = permissions.stream().filter(p -> p.getEmailAddress().equals(myShareTestEmail)).collect(Collectors.toList());
        assertTrue(permissions.size() == 1);
        drive.deleteFile(result.getId());
        result = drive.getFile(result.getId(), null);
        assertNull(result);
    }

    /**
     * Test of createFolder method, of class Controller.
     */
    @Test
    public void testCreateFolder() throws Exception {
        DriveHelper drive = new DriveHelper(appName, googleAuthorizationCodeFlow, sessionId);
        File result = drive.createFolder("meTestFolder", parentId);
        assertNotNull(result);
        drive.deleteFile(result.getId());
        result = drive.getFile(result.getId(), null);
        assertNull(result);
    }

    /**
     * Test of handleFileUpload method, of class Controller.
     */
    @Test
    public void testHandleFileUploadAndDelete() throws Exception {
        DriveHelper drive = new DriveHelper(appName, googleAuthorizationCodeFlow, sessionId);
        MultipartFile multipartFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());
        File result = drive.uploadFileOnly(true, multipartFile, parentId, "this is description");
        assertNotNull(result);
        assertEquals(0, drive.deleteFile(result.getId()));
        result = drive.getFile(result.getId(), null);
        assertNull(result);
    }
    
}
