package com.veera.uploadfiles.controller;

import com.box.sdk.*;
import com.veera.uploadfiles.service.FTPUploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/box")
public class BoxController {

    @Value("${multipart.upload.files.location}")
    private String uploadFolder;

    @Value("${box.access_token}")
    private String accessToken;

    @Value("${box.refresh_token}")
    private String refreshToken;

    @Value("${box.client.secret}")
    private String clientSecret;

    @Value("${box.client.id}")
    private String clientId;

    @RequestMapping(value = "/file", method = RequestMethod.POST, consumes = "multipart/form-data")
    public void uploadFile(@RequestBody MultipartFile file)  {

        Logger.getLogger("com.box.sdk").setLevel(Level.OFF);
        BoxAPIConnection api = new BoxAPIConnection(accessToken);

        BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
        System.out.format("Welcome, %s <%s>!\n\n", userInfo.getName(), userInfo.getLogin());

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        listFolder(rootFolder, 0);

        try (ByteArrayInputStream stream = new ByteArrayInputStream(file.getBytes())) {
            rootFolder.uploadFile(stream, file.getOriginalFilename());
        } catch (Exception e) {
            e.printStackTrace();
        }

       /*
       BoxAPIConnection api = new BoxAPIConnection(clientId, clientSecret, accessToken, refreshToken);
        api.setAutoRefresh(true);

        api.addListener(new BoxAPIConnectionListener() {
            @Override
            public void onRefresh(BoxAPIConnection api) {
                System.out.println("Refreshed");
            }

            @Override
            public void onError(BoxAPIConnection api, BoxAPIException error) {
                System.out.println("Error");
            }
        });
        api.refresh();

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);

        try (ByteArrayInputStream stream = new ByteArrayInputStream(file.getBytes())) {
            rootFolder.uploadFile(stream, file.getOriginalFilename());
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        System.out.println("Start");
        try {
            FTPUploader ftpUploader = new FTPUploader("localhost", "veera", "veera");
            //FTP server path is relative. So if FTP account HOME directory is "/home/kanum/public_html/" and you need to upload
            // files to "/home/kanum/public_html/wp-content/uploads/image2/", you should pass directory parameter as "/wp-content/uploads/image2/"
            ftpUploader.uploadFile(FTPUploader.convert(file), file.getOriginalFilename(), "/");
            ftpUploader.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Done");

    }

    private static void listFolder(BoxFolder folder, int depth) {
        for (BoxItem.Info itemInfo : folder) {
            String indent = "";
            for (int i = 0; i < depth; i++) {
                indent += "    ";
            }

            System.out.println(indent + itemInfo.getName());
            if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder childFolder = (BoxFolder) itemInfo.getResource();
                if (depth < 1) {
                    listFolder(childFolder, depth + 1);
                }
            }
        }
    }


}
