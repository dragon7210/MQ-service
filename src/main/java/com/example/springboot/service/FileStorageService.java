package com.example.springboot.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.stream.Stream;

@Service
public class FileStorageService {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ResourceLoader resourceLoader;
    public FileStorageService() {
    }
    public void checkXmlfile5min(String folderPath, String specificPath){
        Path path = Paths.get(folderPath);
        System.out.println("File read and write every 5 mins");
        if (!Files.isDirectory(path)) {
            System.out.println("Directory doesn't exist");
        }

        List<Path> result;
        try (Stream<Path> pathStream = Files.list(path)) {
            result = pathStream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".xml"))
                    .collect(Collectors.toList());

        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        result.forEach(xmlFile ->
        {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = null;
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
                File xmlfile = new File(xmlFile.toString());
                Path destinationPath = Paths.get(specificPath).resolve(xmlFile.getFileName());
                try {
                    Files.move(xmlFile, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                sendFile("aaaa", xmlFile.toString());


            } catch (ParserConfigurationException e) {
                System.out.println("File move error: "+xmlFile.getFileName());
            }

        });
    }
    public void sendFile(String destinationQueue, String filePath) {
        try {
            Path path = Paths.get(filePath);
            byte[] fileContent = Files.readAllBytes(path);

            jmsTemplate.convertAndSend(destinationQueue, fileContent);
            System.out.println("File sent: "+filePath.toString());
        } catch (IOException e) {
            // Handle the exception properly
            e.printStackTrace();
        }
    }

}
