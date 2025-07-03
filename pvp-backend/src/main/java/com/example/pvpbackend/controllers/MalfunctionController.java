package com.example.pvpbackend.controllers;

import com.example.pvpbackend.DTO.MalfunctionFilesDTO;
import com.example.pvpbackend.DTO.MalfunctionRegisterDTO;
import com.example.pvpbackend.DTO.MalfunctionWithFilesDTO;
import com.example.pvpbackend.repositories.MalfunctionRepository;
import com.example.pvpbackend.services.MalfunctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/malfunction")
@RequiredArgsConstructor
public class MalfunctionController {

    private final MalfunctionService malfunctionService;
    private final MalfunctionRepository malfunctionRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerMalfunction(@RequestBody MalfunctionRegisterDTO dto) {
        try {
            malfunctionService.registerMalfunction(dto);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Gedimas sėkmingai užregistruotas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to register malfunction");
            return ResponseEntity.badRequest().body(response);
        }
    }




    @PostMapping("/upload")
    public ResponseEntity<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                                 @RequestParam("gedimasId") Integer gedimasId) {
        try {
            String result = malfunctionService.saveMultipleFiles(files, gedimasId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload files");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/files/{gedimasId}")
    public ResponseEntity<?> getFilesByGedimasId(@PathVariable Integer gedimasId) {
        try {
            List<MalfunctionFilesDTO> files = malfunctionService.getFilesByGedimasId(gedimasId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to fetch files");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> getMalfunctionWithFiles(@PathVariable Integer id) {
        try {
            MalfunctionWithFilesDTO dto = malfunctionService.getMalfunctionWithFiles(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to get malfunction");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping(value = "/file/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadFile(@PathVariable Long id) {
        return malfunctionService.downloadFileById(id);
    }


    @GetMapping("/user-ids")
    public ResponseEntity<?> getCurrentClientMalfunctionIds() {
        try {
            List<Integer> ids = malfunctionService.getCurrentClientMalfunctionIds();
            return ResponseEntity.ok(ids);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Gedimų sąrašo gauti nepavyko");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register-with-files")
    public ResponseEntity<?> registerWithFiles(
            @RequestParam("aprasymas") String aprasymas,
            @RequestParam("tipas") String tipas,
            @RequestParam("addressJson") String addressJson,
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) {
        try {
            malfunctionService.registerWithFiles(aprasymas, tipas, addressJson, files);
            return ResponseEntity.ok(Map.of("success", "Gedimas užregistruotas su failais"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Nepavyko registruoti gedimo: " + e.getMessage()));
        }
    }



    @DeleteMapping("/file/{id}")
    public ResponseEntity<Object> deleteFile(@PathVariable Long id) {
        try {
            malfunctionService.deleteMalfunctionFile(id);
            return ResponseEntity.ok(Map.of("success", "Failas ištrintas"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


}