package com.example.springboot.service;
import com.example.springboot.config.RestTemplateConfig;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
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
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private final RestTemplateConfig restTemplate;
    @Autowired
    private JmsTemplate jmsTemplate;

    public FileStorageService(RestTemplateConfig restTemplate) {
        this.restTemplate = restTemplate;
    }
    public void checkAndSendToQueue(String folderPath, String specificPath){
        Path path = Paths.get(folderPath);
        System.out.println("checkAndSendToQueue 5 mins");
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
                ResponseEntity<String> response = sendPayloadWithCertificate("https://google.com", xmlFile.toString());
                if(response != null){
                    System.out.println("Response : " + jmsTemplate);
                    sendFile("aaaa", response.toString());
                    try {
                        Files.move(xmlFile, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    System.out.println("Response: " + "NULL");
                }
            } catch (ParserConfigurationException e) {
                System.out.println("File move error: "+xmlFile.getFileName());
            }

        });
    }
    public void sendFile(String destinationQueue, String response) {
//        try {
//            Path path = Paths.get(filePath);
//            byte[] fileContent = Files.readAllBytes(path);
            jmsTemplate.convertAndSend(destinationQueue, response);
            System.out.println("File sent: "+response.toString());
//        } catch (IOException e) {
//             Handle the exception properly
//            e.printStackTrace();
//        }
    }
    public ResponseEntity<String> sendPayloadWithCertificate(String endpoint, String filePath) {
        Path path = Paths.get(filePath);
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(path);
        } catch (IOException e) {
//            throw new RuntimeException(e);
            return null;
        }
        return restTemplate.sendTextWithCert(endpoint, fileContent.toString());
    }

    public void readXMLfromQueue(String specfic){
        BytesMessage message = (BytesMessage) jmsTemplate.receive("aaaa");

        // Process received message
        if (message != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while (true) {
                    try {
                        if (!((len = message.readBytes(buffer)) != -1)) break;
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                    baos.write(buffer, 0, len);
                }

                byte[] fileBytes = baos.toByteArray();
                baos.close();

                Path filePath = Files.createTempFile(Path.of(specfic), "received-", ".xml");
                Files.write(filePath, fileBytes);

                System.out.println("File saved: " + filePath);
            } catch (IOException e) {
                // Exception handling logic
                e.printStackTrace();
            }
        }
    }

}
