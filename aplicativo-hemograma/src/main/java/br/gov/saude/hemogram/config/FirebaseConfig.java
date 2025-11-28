package br.gov.saude.hemogram.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-path}")
    private Resource credentialsResource;

    @Value("${firebase.enabled}")
    private Boolean firebaseEnabled;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            log.info("Firebase está desabilitado");
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(
                                credentialsResource.getInputStream()))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase inicializado com sucesso");
            }
        } catch (IOException e) {
            log.error("Erro ao inicializar Firebase", e);
            throw new RuntimeException("Falha ao inicializar Firebase", e);
        }
    }
}