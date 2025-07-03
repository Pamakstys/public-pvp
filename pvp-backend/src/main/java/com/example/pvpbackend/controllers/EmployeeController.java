package com.example.pvpbackend.controllers;

import com.example.pvpbackend.DTO.RequestCreateDTO;
import com.example.pvpbackend.DTO.RequestFieldCreateDTO;
import com.example.pvpbackend.models.*;
import com.example.pvpbackend.services.*;

import com.example.pvpbackend.DTO.*;
import com.example.pvpbackend.models.Request;
import com.example.pvpbackend.models.RequestField;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/employee")
public class EmployeeController {

    private final RequestService requestService;
    private final MalfunctionService malfunctionService;
       
    private final ReviewService reviewService;
    private final ReminderService reminderService;
    private final UsersService usersService;
    private final AddressService addressService;
    public EmployeeController(AddressService addressService, RequestService requestService,ReminderService reminderService, ReviewService reviewService, MalfunctionService malfunctionService, UsersService usersService){
        this.requestService = requestService;
        this.reminderService = reminderService;
        this.reviewService = reviewService;
        this.malfunctionService = malfunctionService;
        this.usersService = usersService;
        this.addressService = addressService;
    }

    @GetMapping("/address/view/{id}")
    public ResponseEntity<Address> viewAddress(@PathVariable int id){
        Address address = addressService.get(id);
        address.setGyventojas(null);
        return ResponseEntity.ok(address);
    }

    @PostMapping("/request/template/upload")
    public ResponseEntity<?> uploadTemplate(@RequestParam("file") MultipartFile file, @RequestParam("requestId") Integer requestId) {
        try {
            requestService.uploadTemplate(file, requestId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", "Template uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload template: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/request/template/create")
    public ResponseEntity<Object> createRequest(@RequestBody RequestCreateDTO createDTO){
        try{
            int id = requestService.createRequest(createDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to create request");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/request/template/add-field/{id}")
    public ResponseEntity<Object> addRequestField(@PathVariable int id, @RequestBody RequestFieldCreateDTO createDTO){
        try{
            requestService.createField(createDTO, id);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Request field created");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to create request field");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/request/template/delete-field")
    public ResponseEntity<Object> removeRequestField(@RequestParam int fieldId, @RequestParam int requestId){
        try {
            requestService.deleteField(fieldId, requestId);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Field removed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to remove field");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/request/assign/{id}")
    public ResponseEntity<Object> assignRequest(@PathVariable int id){
        try {
            requestService.assignRequest(id);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Request assigned");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to assign request");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/request/view/{id}/comment")
    public ResponseEntity<Object> commentRequest(@PathVariable Integer id, @RequestBody RequestCommentDTO commentDTO){
        try {
            requestService.addComment(id, commentDTO.getComment());
            Map<String, String> response = new HashMap<>();
            response.put("success", "Comment added");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to save comment");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/request/view/{id}/delete")
    public ResponseEntity<Object> deleteRequest(@PathVariable Integer id){
        try {
            requestService.deleteRegisteredRequest(id);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Request deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to delete request");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/request/view")
    public ResponseEntity<Object> getAllRegistered(@RequestParam(required = false) Boolean assigned){
        try {
            List<RegisteredRequest> requests = requestService.getAllRegisteredRequests(assigned);
            for(RegisteredRequest request : requests){
                request.setClient(null);
                Employee employee = request.getEmployee();
                if(employee != null){
                    employee.setRegisteredRequests(null);
                    User user = employee.getUser();
                    user.setSlaptazodis(null);
                    employee.setUser(user);
                    request.setEmployee(employee);
                }
            }
            return ResponseEntity.ok(requests);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to fetch requests");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/request/view/byEmployee")
    public ResponseEntity<Object> getAllRegisteredByEmployee(){
        try {
            List<RegisteredRequest> requests = requestService.getAllRegisteredRequestsForEmployee();
            return ResponseEntity.ok(requests);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to register request");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/request/view/{id}/set-state")
    public ResponseEntity<Object> changeRequestState(@PathVariable Integer id, @RequestBody RequestStateDTO stateDTO){
        try {
            requestService.changeState(id, stateDTO.getState());
            Map<String, String> response = new HashMap<>();
            response.put("success", "State changed");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "failed to change state");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/request/template/view")
    public ResponseEntity<Object> viewTemplates(){
        try {
            List<Request> requests = requestService.getAllRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "unable to view template");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/request/template/activate")
    public ResponseEntity<Object> activateRequest(@RequestBody RequestActivateDTO activateDTO){
        try {
            requestService.activateRequest(activateDTO.getRequestId(), activateDTO.getActive());
            Map<String, String> response = new HashMap<>();
            response.put("success", "Template activated");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to activate template");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/malfunctions/assign/{id}")
    public ResponseEntity<Object>assignMalfunction(@PathVariable int id) {
        try {
            malfunctionService.assignMalfunction(id);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to assign malfucntion");
            return ResponseEntity.internalServerError().body(response);
        }
        Map<String, String> response = new HashMap<>();
        response.put("success", "Malfunction added successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/malfunctions/getUnregistered")
    public ResponseEntity<Object> getUnregisteredMalfunctions() {
        try {
            return ResponseEntity.ok(malfunctionService.getUnassignedMalfunctions());
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to get malfunctions");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/request/template/view/{id}")
    public ResponseEntity<Object> viewRequest(@PathVariable int id){
        Request request = requestService.getRequest(id);
        if(request == null){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to view template");
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

    @PostMapping("/request/template/update")
    public ResponseEntity<Object> editRequest(@RequestBody RequestUpdateDTO requestDTO){
        try {
            requestService.updateRequest(requestDTO);
            Map<String, String> response = new HashMap<>();
            response.put("success", "template updated");
            return ResponseEntity.ok(response);
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to update template");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/set-role")
    public ResponseEntity<Object> setEmployeeRole(@RequestBody EmployeeRoleUpdateDTO roleDTO) {
        try {
            usersService.updateEmployeeRole(roleDTO.getRole());
            Map<String, String> response = new HashMap<>();
            response.put("success", "Role Updated");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.toString());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/request/roles")
    public ResponseEntity<Object> getRequestRoles(){
        try {
            List<RequestRolesDTO> roles = requestService.getRequestRoles();
            return ResponseEntity.ok(roles);
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to fetch roles");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/request/template/delete/{id}")
    public ResponseEntity<Object> deleteRequest(@PathVariable int id){
        try {
            requestService.deleteRequest(id);
            Map<String, String> response = new HashMap<>();
            response.put("success", "template deleted");
            return  ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to delete template");
            return ResponseEntity.badRequest().body(e);
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewSummaryDTO>> getRatingsSummary(@RequestParam(value = "from", required = false) Date from) {
        return ResponseEntity.ok(reviewService.getRatings(from));
    }

    @GetMapping("/schedules/force-reminders")
    public ResponseEntity<Object> forceReminderSending(){
        reminderService.sendDailyRemindersInternal();
        Map<String, String> response = new HashMap<>();
        response.put("success", "Reminders sent");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/malfunctions/delete/{id}")
    public ResponseEntity<Object> deleteMalfunction(@PathVariable int id) {
        try {
            malfunctionService.deleteMalfunction(id);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Malfunction deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to delete malfunction");
            return ResponseEntity.badRequest().body(response);
        }
    }


    @GetMapping("/clients/list")
    public ResponseEntity<Object> getAllClients() {
        try {
            List<ClientListDTO> clientList = usersService.listClientsDTO();
            return ResponseEntity.ok(clientList);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to get clients");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/malfunctions/my")
    public ResponseEntity<Object> getMyMalfunctions() {
        try {
            return ResponseEntity.ok(malfunctionService.getMalfunctionsByCurrentEmployee());
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Nepavyko gauti priskirtų gedimų");
            return ResponseEntity.internalServerError().body(response);
        }
    }


}
