package com.example.springboot.config;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import javax.net.ssl.SSLContext;

@Configuration
public class RestTemplateConfig {
    @Value("${keystore.location}")
    private Resource keystore;
    @Value("${keystore.password}")
    private String keystorePassword;

    @Value("${truststore.location}")
    private Resource truststore;

    @Value("${truststore.password}")
    private String truststorePassword;
    @Bean
    public HttpClient restTemplate() throws Exception {
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadKeyMaterial(
                        keystore.getURL(),
                        keystorePassword.toCharArray(),
                        keystorePassword.toCharArray()
                )
                .loadTrustMaterial(
                        truststore.getURL(),
                        truststorePassword.toCharArray()
                )
                .build();
        final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .build();
        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }
    public ResponseEntity<String> sendTextWithCert(String url, String text) {
        try {
            HttpClient httpClient = restTemplate();
            HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
            httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);
            RestTemplate restTemplate = new RestTemplate(httpComponentsClientHttpRequestFactory);

            ResponseEntity<String> response = restTemplate.postForEntity(url, text, String.class);
            return response;
            // Handle the response as needed
        } catch (Exception ex) {
            return null;
            // Proper error handling here
        }
    }
}