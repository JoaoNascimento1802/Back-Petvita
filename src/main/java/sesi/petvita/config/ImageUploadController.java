package sesi.petvita.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping("/user/{id}")
    public ResponseEntity<Map<String, String>> uploadUserImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String url = imageUploadService.uploadForUser(id, file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/pet/{id}")
    public ResponseEntity<Map<String, String>> uploadPetImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String url = imageUploadService.uploadForPet(id, file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/veterinary/{id}")
    public ResponseEntity<Map<String, String>> uploadVeterinaryImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String url = imageUploadService.uploadForVeterinary(id, file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
