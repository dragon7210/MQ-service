package com.example.springboot;

import com.example.springboot.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class SpringbootApplication {
	@Autowired
	private FileStorageService fileService;
	public static void main(String[] args) {
		SpringApplication.run(SpringbootApplication.class, args);
	}
	//5mins
	@Scheduled(fixedDelay = 30000) // Delay in milliseconds (5 min * 60 sec * 1000 ms)
	public void scheduled5min() {
		String folderPath = "C:/outbound";
		String specificPath = "C:/inbound";
		fileService.checkXmlfile5min(folderPath, specificPath);
	}

}
