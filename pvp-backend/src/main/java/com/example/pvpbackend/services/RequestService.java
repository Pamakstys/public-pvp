package com.example.pvpbackend.services;

import com.example.pvpbackend.DTO.*;
import com.example.pvpbackend.enums.EmployeeRole;
import com.example.pvpbackend.enums.RegisteredRequestType;
import com.example.pvpbackend.models.*;
import com.example.pvpbackend.repositories.EmployeeRepository;
import com.example.pvpbackend.repositories.RegisteredRequestRepository;
import com.example.pvpbackend.repositories.RequestFieldRepository;
import com.example.pvpbackend.repositories.RequestRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.mail.MessagingException;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.naming.AuthenticationException;

import java.io.ByteArrayOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RequestService {
    @Autowired
    private RegisteredRequestRepository registeredRequestRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private RequestFieldRepository requestFieldRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    private final UsersService usersService;
    private final EmailService emailService;
    private final AddressService addressService;
    public RequestService(UsersService usersService, AddressService addressService ,EmailService emailService) {
        this.usersService = usersService;
        this.emailService = emailService;
        this.addressService = addressService;
    }

    public Request getRequest(Integer id){
        return requestRepository.findById(id).orElse(null);
    }

    public void changeState(Integer id, Integer state) throws Exception {
        User user = usersService.getCurrent();
        Employee employee = usersService.getEmployee(user);
        if(employee == null) throw new AuthenticationException("Unauthorized");
        RegisteredRequest registeredRequest = getRegisteredRequest(id);
        if(registeredRequest == null) throw new Exception("Request not found");
        if(state < 1 || state > 4) throw new Exception("Invalid state");
        registeredRequest.setBusena(state);
        registeredRequestRepository.save(registeredRequest);
    }
    public void addComment(Integer id, String comment) throws Exception {
        User user = usersService.getCurrent();
        Employee employee = usersService.getEmployee(user);
        if(employee == null) throw new AuthenticationException("Unauthorized");
        RegisteredRequest registeredRequest = getRegisteredRequest(id);
        if(registeredRequest == null) throw new Exception("Request not found");
        if(comment.isEmpty()) throw new Exception("Comment cannot be empty");
        String data = registeredRequest.getDuomenys();
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = (ObjectNode) mapper.readTree(data);
            ArrayNode commentsArray;
            JsonNode existingComments = root.get("comments");
            if (existingComments != null && existingComments.isArray()) {
                commentsArray = (ArrayNode) existingComments;
            } else {
                commentsArray = mapper.createArrayNode();
            }

            ObjectNode newComment = mapper.createObjectNode();
            newComment.put("comment", comment);
            newComment.put("date", LocalDate.now().toString());

            commentsArray.add(newComment);

            root.set("comments", commentsArray);
            String updatedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            registeredRequest.setDuomenys(updatedJson);
            registeredRequestRepository.save(registeredRequest);
        } catch (Exception e) {throw new Exception(e);}
    }

    public RegisteredRequest getRegisteredRequestForView(Integer id) throws AuthenticationException {
        RegisteredRequest registeredRequest = registeredRequestRepository.findById(id).orElse(null);
        assert registeredRequest != null;
        User currUser = usersService.getCurrent();
        Client currClient = usersService.getClient(currUser);
        Employee currEmployee = usersService.getEmployee(currUser);

        Client client = registeredRequest.getClient();
        User user = client.getUser();
        user.setSlaptazodis(null);
        client.setUser(user);
        List<Address> addresses = client.getAdresai();
        for(Address address : addresses){
            address.setGyventojas(null);
        }
        client.setAdresai(addresses);
        registeredRequest.setClient(client);

        if(currClient != null){
            registeredRequest.setEmployee(null);
        }
        else if(currEmployee != null) {
            Employee employee = registeredRequest.getEmployee();
            if(employee != null){
                employee.setRegisteredRequests(null);
                user = employee.getUser();
                user.setSlaptazodis(null);
                employee.setUser(user);
                registeredRequest.setEmployee(employee);
            }
        }
        else {throw new AuthenticationException("Unauthorized");}
        return registeredRequest;
    }

    public RegisteredRequest getRegisteredRequest(Integer id){
        return registeredRequestRepository.findById(id).orElse(null);
    }

    public void assignRequest(Integer id){
        User user = usersService.getCurrent();
        Employee employee = usersService.getEmployee(user);
        if(employee != null) {
            RegisteredRequest registeredRequest = getRegisteredRequest(id);
            registeredRequest.setEmployee(employee);
            registeredRequest.setBusena(3);
            registeredRequestRepository.save(registeredRequest);
        }
    }

    public void registerRequest(RequestRegisterDTO requestDTO){
        RegisteredRequest registeredRequest = new RegisteredRequest();
        Request request = getRequest(requestDTO.getRequestId());
        if(request == null) return;
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);
        if(client == null) return;

        String data = requestDTO.getData();
        registeredRequest.setDuomenys(data);

        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = (ObjectNode) mapper.readTree(data);
            if (request.getFailas() != null) {
                root.put("filePath", request.getFailas());
            }
            String updatedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            registeredRequest.setDuomenys(updatedJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse request data", e);
        }
        registeredRequest.setPavadinimas(request.getPavadinimas());
        registeredRequest.setAprasymas(request.getAprasymas());
        registeredRequest.setRequest(request);
        registeredRequest.setClient(client);
        registeredRequest.setBusena(1);

        LocalDateTime sqlDate = LocalDateTime.now();
        registeredRequest.setData(sqlDate);
        registeredRequestRepository.save(registeredRequest);

        assignRequestByRole(registeredRequest);
    }

    public void assignRequestByRole(RegisteredRequest request){
        List<Employee> employees = employeeRepository.findByRole(request.getRequest().getRole());

        if(employees.isEmpty()){
            employees = employeeRepository.findAll();
        }

        Employee leastBusyEmployee = null;
        int minRequests = Integer.MAX_VALUE;

        List<Integer> busenaValues = List.of(
                RegisteredRequestType.Priimtas.ordinal(),
                RegisteredRequestType.Vykdomas.ordinal()
        );

        for(Employee employee : employees){
            List<RegisteredRequest> requests = registeredRequestRepository.findByEmployeeAndBusenaIn(employee, busenaValues);

            if (requests.size() < minRequests) {
                minRequests = requests.size();
                leastBusyEmployee = employee;
            }
        }
        Employee employee = null;
        if (leastBusyEmployee != null) {
            employee = leastBusyEmployee;
            request.setEmployee(leastBusyEmployee);
            registeredRequestRepository.save(request);
        } else {
            employee = employees.get(0);
            request.setEmployee(employee);
            registeredRequestRepository.save(request);
        }
        try {
            emailService.sendNewRequestReminderEmail(employee, request);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }
    public int createRequest(RequestCreateDTO createDTO){
        Request request = new Request();
        request.setPavadinimas(createDTO.getName());
        request.setAprasymas(createDTO.getDescription());
        LocalDate today = LocalDate.now();
        Date sqlDate = Date.valueOf(today);
        request.setData(sqlDate);
        requestRepository.save(request);
        return request.getId();
    }

    public List<RequestField> getFieldsByRequest(Request request){
        return requestFieldRepository.findByRequestOrderByEilNrAsc(request).orElse(null);
    }

    public RequestField getFieldById(Integer id){
        return requestFieldRepository.findById(id).orElse(null);
    }

    public void deleteField(Integer fieldId, Integer requestId){
        Request request = getRequest(requestId);
        RequestField field = getFieldById(fieldId);
        if(field.getRequest().getId().equals(request.getId())){
        requestFieldRepository.deleteById(fieldId);
        }
    }

    public void deleteRegisteredRequest(Integer id){
        RegisteredRequest registeredRequest = getRegisteredRequest(id);
        registeredRequestRepository.delete(registeredRequest);
    }

    public void deleteClientRegisteredRequest(Integer id) throws AuthenticationException {
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);
        if(client == null) throw new AuthenticationException("Unauthorized");
        RegisteredRequest registeredRequest = getRegisteredRequest(id);
        if(!Objects.equals(registeredRequest.getClient().getIdNaudotojas(), client.getIdNaudotojas())) throw new AuthenticationException("Unauthorized");
        registeredRequestRepository.delete(registeredRequest);
    }

    public List<Request> getAllRequests(){
        return requestRepository.findAll();
    }

    public List<Request> getAllActiveRequests() {
        return requestRepository.findByAktyvusTrue();
    }

    public void activateRequest(Integer id, Boolean active) throws Exception {
        Request request = getRequest(id);
        if(request == null) throw new Exception("Request not found");
        request.setAktyvus(active);
        requestRepository.save(request);
    }

    public byte[] getFormatedTemplate(Integer id) {
        RegisteredRequest registeredRequest = getRegisteredRequest(id);
        if (registeredRequest == null) {
            throw new IllegalArgumentException("Registered request not found with ID: " + id);
        }
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);
        if (client != null){
            if(registeredRequest.getClient() != client) {
                throw new IllegalArgumentException("Unauthorized");
            }
        }

        String data = registeredRequest.getDuomenys();
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("No data found for registered request with ID: " + id);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(data);

            String filePath = root.path("filePath").asText();
            if (filePath.isEmpty()) {
                throw new IllegalArgumentException("No file path found in registered request data");
            }

            Path file = uploadDir.resolve(filePath);
            if (!Files.exists(file)) {
                throw new IllegalArgumentException("File does not exist at path: " + file.toString());
            }

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(file.toFile());

            Map<String, String> variables = new HashMap<>();
            JsonNode fieldsArray = root.path("fields");
            if (fieldsArray.isArray()) {
                for (JsonNode fieldEntry : fieldsArray) {
                    String key = fieldEntry.path("field").asText();
                    String value = fieldEntry.path("data").asText();
                    if (!key.isEmpty()) {
                        variables.put(key, value);
                    }
                }
            }

            JsonNode addressId = root.path("addressId");
            if(addressId.asInt() != 0){
                Address address = addressService.get(addressId.asInt());
                if(address != null){
                    variables.put("Adresas", address.getSeniunija() + ", " + address.getGatve() + ", " + address.getNamoNumeris());
                }
            }
            VariablePrepare.prepare(wordMLPackage);
            MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
            mainDocumentPart.variableReplace(variables);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wordMLPackage.save(baos);

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate formatted template", e);
        }
    }


    public List<RegisteredRequest> getClientsRequests() throws AuthenticationException {
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);
        if(client == null) throw new AuthenticationException("Unauthorized");
        List<RegisteredRequest> requests = registeredRequestRepository.findByClient(client);
        for(RegisteredRequest request: requests){
            request.setEmployee(null);
            request.setClient(null);
        }
        return requests;
    }

    public List<RegisteredRequest> getAllRegisteredRequests(Boolean assigned){
        if(assigned == null){
            return registeredRequestRepository.findAll();
        }
        else if (assigned){
            return registeredRequestRepository.findByEmployeeIsNotNull();
        }
        else{
            return registeredRequestRepository.findByEmployeeIsNull();
        }
    }

    public List<RegisteredRequest> getAllRegisteredRequestsForEmployee() throws AuthenticationException {
        User user = usersService.getCurrent();
        Employee employee = usersService.getEmployee(user);
        if(employee == null) throw new AuthenticationException("Unauthorized");
        List<RegisteredRequest> requests = registeredRequestRepository.findByEmployee(employee);
        for(RegisteredRequest request : requests){
            request.setClient(null);
            request.setEmployee(null);
        }
        return requests;
    }

    public List<RequestRolesDTO> getRequestRoles(){
        List<RequestRolesDTO> roles = new ArrayList<>();
        int index = 0;
        for (EmployeeRole role : EmployeeRole.values()){
            roles.add(new RequestRolesDTO(index++, role.name()));
        }
        return roles;
    }

    public void deleteRequest(Integer id){
        Request request = getRequest(id);
        List<RequestField> fields = getFieldsByRequest(request);
        requestFieldRepository.deleteAll(fields);
        List<RegisteredRequest> registeredRequests = registeredRequestRepository.findByRequest(request).orElse(null);
        assert registeredRequests != null;
        for(RegisteredRequest registered : registeredRequests){
            registered.setRequest(null);
        }
        registeredRequestRepository.saveAll(registeredRequests);
        requestRepository.delete(request);
    }
    public void createField(RequestFieldCreateDTO createDTO, Integer id){
        RequestField field = new RequestField();
        Request request = getRequest(id);
        if(request == null){
            return;
        }
        field.setRequest(request);
        field.setEtikete(createDTO.getLabel());
        field.setPrivalomas(createDTO.getRequired());
        field.setEilNr(createDTO.getPosition());
        switch (createDTO.getType().toLowerCase()) {

            case "dropdown":
                field.setTipas("dropdown");
//              String regex = "[,\\.\\s]";
//              String[] values = options.split(regex);
                field.setPasirinkimai(createDTO.getOptions());
                requestFieldRepository.save(field);
                break;
            case "text":
                field.setTipas("text");
                requestFieldRepository.save(field);
                break;
            default:
                throw new IllegalArgumentException("Unsupported field type: " + createDTO.getType());
        }
    }
    @Transactional
    public void updateRequest(RequestUpdateDTO requestDTO){
        Request request = getRequest(requestDTO.getId());
        request.setPavadinimas(requestDTO.getPavadinimas());
        request.setAprasymas(requestDTO.getAprasymas());
        request.setRole(requestDTO.getRole());
        requestRepository.save(request);
        for(RequestFieldUpdateDTO fieldDTO : requestDTO.getFields()){
            RequestField field = getFieldById(fieldDTO.getId());
            if (!field.getRequest().getId().equals(requestDTO.getId())) {
                throw new RuntimeException("Field " + fieldDTO.getId() + " is not in request");
            }
            field.setEtikete(fieldDTO.getEtikete());
            field.setTipas(fieldDTO.getTipas());
            field.setPrivalomas(fieldDTO.getPrivalomas());
            field.setPasirinkimai(fieldDTO.getPasirinkimai());
            field.setEilNr(fieldDTO.getEilNr());
            requestFieldRepository.save(field);
        }
    }

    private final Path uploadDir = Paths.get("uploads/templates");

    public void uploadTemplate(MultipartFile file, Integer requestId) throws Exception {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Request request = getRequest(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request not found with ID: " + requestId);
        }

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or null");
        }

        if (!file.getOriginalFilename().endsWith(".docx")) {
            throw new IllegalArgumentException("Only .docx files are supported");
        }

        // Generate unique filename
        String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(filename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        request.setFailas(filename); // Or filePath.toString() if you want full path
        requestRepository.save(request);
    }

}
