// sesi/petvita/config/FirebaseConfig.java
package sesi.petvita.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value; // Importar Value
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream; // Importar ByteArrayInputStream
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger; // Importar Logger
import org.slf4j.LoggerFactory; // Importar LoggerFactory

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    // 1. Injete o conteúdo da variável de ambiente
    //    (Nós vamos criar 'FIREBASE_CREDENTIALS' no Azure)
    @Value("${FIREBASE_CREDENTIALS:#{null}}")
    private String firebaseCredentials;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        log.info("Inicializando Firebase...");

        FirebaseOptions options;

        if (firebaseCredentials != null && !firebaseCredentials.isEmpty()) {
            // --- MODO DE PRODUÇÃO (Azure) ---
            // Lê o JSON diretamente da variável de ambiente
            log.info("Carregando credenciais do Firebase a partir de variável de ambiente (PROD).");
            InputStream serviceAccount = new ByteArrayInputStream(firebaseCredentials.getBytes());
            options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
        } else {
            // --- MODO DE DESENVOLVIMENTO (Local) ---
            // Procura o arquivo físico em /resources (como antes)
            log.warn("Variável de ambiente FIREBASE_CREDENTIALS não encontrada.");
            log.warn("Tentando carregar 'firebase-service-account.json' do classpath (DEV).");

            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (!resource.exists()) {
                log.error("!!! ERRO CRÍTICO: Arquivo 'firebase-service-account.json' não encontrado em src/main/resources/ !!!");
                throw new IOException("Arquivo de credenciais do Firebase não encontrado.");
            }

            InputStream serviceAccount = resource.getInputStream();
            options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
        }

        // Inicializa o App
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