package com.example.pvpbackend.controllers;

import com.example.pvpbackend.DTO.RequestRegisterDTO;
import com.example.pvpbackend.DTO.RequestViewDTO;
import com.example.pvpbackend.models.RegisteredRequest;
import com.example.pvpbackend.models.Request;
import com.example.pvpbackend.models.RequestField;
import com.example.pvpbackend.services.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping("/api/request")
public class RequestController {
    private final RequestService requestService;
    public RequestController(RequestService requestService){
        this.requestService = requestService;
    }
    @GetMapping("/view/{id}")
    public ResponseEntity<Object> viewRegisteredRequest(@PathVariable int id){
        try {
            RegisteredRequest registeredRequest = requestService.getRegisteredRequestForView(id);
            if(registeredRequest == null){
                Map<String, String> response = new HashMap<>();
                response.put("error", "Request not found");
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(registeredRequest);
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unauthorized");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteRegisteredRequest(@PathVariable Integer id){
        try {
            requestService.deleteClientRegisteredRequest(id);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Deleted");
            return ResponseEntity.ok(response);
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unauthorized");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/view")
    public ResponseEntity<Object> viewRegisteredRequests(){
        try {
            List<RegisteredRequest> requests = requestService.getClientsRequests();
            return ResponseEntity.ok(requests);
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unauthorized");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createRegisteredRequest(@RequestBody RequestRegisterDTO requestDTO){
        try {
            requestService.registerRequest(requestDTO);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Request registered");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to register request");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<Object> getAllTemplates(){
        try {
            List<Request> requests = requestService.getAllActiveRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to get templates");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/template/download/{id}")
    public ResponseEntity<Object> downloadTemplate(@PathVariable Integer id) {
        byte[] docBytes = requestService.getFormatedTemplate(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"template.docx\"")
                .contentLength(docBytes.length)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docBytes);
    }



    @GetMapping("/templates/view/{id}")
    public ResponseEntity<Object> viewRequest(@PathVariable int id){
        Request request = requestService.getRequest(id);
        if(request == null){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Request not found");
            return ResponseEntity.badRequest().body(response);
        }
        List<RequestField> fields = requestService.getFieldsByRequest(request);
        if(fields != null){
            for (RequestField field : fields){
                field.setRequest(null);
            }
        }
        RequestViewDTO viewDTO = new RequestViewDTO(request, fields);
        return ResponseEntity.ok(viewDTO);
    }
}
