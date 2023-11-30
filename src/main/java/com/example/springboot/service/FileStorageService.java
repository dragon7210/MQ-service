package com.example.springboot.service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
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

    public FileStorageService() {
        rabbitTemplate = null;
    }
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routingkey}")
    private String routingKey;
    public void checkXmlfile5min(String folderPath, String specificPath){
        Path path = Paths.get(folderPath);
        System.out.println("File read and write every 5 mins");
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("The provided path is not a directory");
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
//                try {
//                    Files.move(xmlFile, destinationPath, StandardCopyOption.REPLACE_EXISTING);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                System.out.println("File move: "+xmlFile.getFileName()+"   "+destinationPath.toString());
                RestTemplate restTemplate = new RestTemplate();
                MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                map.add("file", new FileSystemResource(xmlFile.toFile()));

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map);
                // Send the file to the .NET service which interacts with MSMQ
                String response = restTemplate.postForObject("", requestEntity, String.class);


            } catch (ParserConfigurationException e) {
                System.out.println("File move error: "+xmlFile.getFileName());
            }

        });
    }

}
