// sesi/petvita/config/FirebaseConfig.java
package sesi.petvita.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream; // Importar

@Configuration
public class FirebaseConfig {

    // Adiciona um logger para diagnóstico
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        log.info("Inicializando Firebase...");

        // Carrega o arquivo de credenciais
        ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

        if (!resource.exists()) {
            log.error("!!! ERRO CRÍTICO: Arquivo 'firebase-service-account.json' não encontrado em src/main/resources/ !!!");
            throw new IOException("Arquivo de credenciais do Firebase não encontrado.");
        }

        InputStream serviceAccount = resource.getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp app;
        if (FirebaseApp.getApps().isEmpty()) {
            app = FirebaseApp.initializeApp(options);
            log.info("FirebaseApp inicializado com sucesso.");
        } else {
            app = FirebaseApp.getInstance();
            log.warn("FirebaseApp já estava inicializado.");
        }

        return app;
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        log.info("Obtendo instância do Firestore.");
        return FirestoreClient.getFirestore(firebaseApp);
    }
}