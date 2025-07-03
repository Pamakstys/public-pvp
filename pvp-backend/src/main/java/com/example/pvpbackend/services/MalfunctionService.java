package com.example.pvpbackend.services;

import com.example.pvpbackend.DTO.MalfunctionFilesDTO;
import com.example.pvpbackend.DTO.MalfunctionPrintDTO;
import com.example.pvpbackend.DTO.MalfunctionRegisterDTO;
import com.example.pvpbackend.DTO.MalfunctionWithFilesDTO;
import com.example.pvpbackend.enums.EmployeeRole;
import com.example.pvpbackend.models.*;
import com.example.pvpbackend.repositories.EmployeeRepository;
import com.example.pvpbackend.repositories.MalfunctionFileRepository;
import com.example.pvpbackend.repositories.MalfunctionRepository;
import com.example.pvpbackend.repositories.RegisteredMalfunctionsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MalfunctionService {
    @Autowired
    private MalfunctionRepository malfunctionRepository;
    @Autowired
    private RegisteredMalfunctionsRepository registeredMalfunctionsRepository;
    @Autowired
    private MalfunctionFileRepository malfunctionFilesRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;


    private final UsersService usersService;
    public MalfunctionService (UsersService usersService) {
        this.usersService = usersService;
    }

    public void registerMalfunction(MalfunctionRegisterDTO dto) {
        RegisteredMalfunctions registeredMalfunction = new RegisteredMalfunctions();
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);
        registeredMalfunction.setGyventojas(client);
        registeredMalfunction.setDarbuotojas(pickBestEmployeeFor(dto.getTipas()));
        registeredMalfunctionsRepository.save(registeredMalfunction);
        createMalfunction(dto, registeredMalfunction);
    }

    public void createMalfunction (MalfunctionRegisterDTO dto, RegisteredMalfunctions registeredMalfunction) {
        Malfunction malfunction = new Malfunction();
        malfunction.setAprasymas(dto.getAprasymas());
        malfunction.setAddressJson(dto.getAddressJson());
        malfunction.setTipas(dto.getTipas());

        LocalDateTime sqlDate = LocalDateTime.now();
        malfunction.setUzregistravimoData(sqlDate);
        malfunction.setRegisteredMalfunctions(registeredMalfunction);
        malfunctionRepository.save(malfunction);
    }

    public void assignMalfunction(int id) {
        User user = usersService.getCurrent();
        Employee employee = usersService.getEmployee(user);
        if (employee == null)
        {
            throw new RuntimeException("Darbuotojas nerastas");
        }
        RegisteredMalfunctions registeredMalfunctions = getRegisteredMalfunction(id);
        registeredMalfunctions.setDarbuotojas(employee);
        updateRegisteredMalfunction(registeredMalfunctions);
    }

    public RegisteredMalfunctions getRegisteredMalfunction(int id) {
        return registeredMalfunctionsRepository.findById(id).orElse(null);
    }

    public void updateRegisteredMalfunction(RegisteredMalfunctions registeredMalfunction) {
        registeredMalfunctionsRepository.save(registeredMalfunction);
    }

    private final Path uploadDir = Path.of("uploads");
    public String saveMultipleFiles(MultipartFile[] files, Integer gedimasId) {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Malfunction gedimas = malfunctionRepository.findById(gedimasId)
                    .orElseThrow(() -> new RuntimeException("Gedimas nerastas"));

            StringBuilder response = new StringBuilder();

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String originalName = file.getOriginalFilename();
                String extension = "";

                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf('.'));
                }

                String uniqueName = UUID.randomUUID() + extension;

                Path targetPath = uploadDir.resolve(uniqueName);
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                MalfunctionFiles mf = new MalfunctionFiles();
                mf.setFizinisKelias(targetPath.toString());
                mf.setPavadinimas(originalName);
                mf.setGedimas(gedimas);

                malfunctionFilesRepository.save(mf);
                response.append(originalName).append(" įkeltas\n");
            }

            return response.toString();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Nepavyko įrašyti failų: " + e.getMessage());
        }
    }

    public List<MalfunctionFilesDTO> getFilesByGedimasId(Integer gedimasId) {
        List<MalfunctionFiles> files = malfunctionFilesRepository.findByGedimasId(gedimasId);
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);
        Employee employee = usersService.getEmployee(user);

        if (client == null && employee == null) {
            throw new RuntimeException("Prieiga negalima");
        }
        if (client != null) {
            RegisteredMalfunctions registeredMalfunctions = getRegisteredMalfunction(gedimasId);

            if (!client.getIdNaudotojas().equals(registeredMalfunctions.getGyventojas().getIdNaudotojas())) {
                throw new RuntimeException("Neturite prieigos prie šio gedimo");
            }
        }
        return files.stream()
                .map(f -> new MalfunctionFilesDTO(
                        f.getId().longValue(),
                        f.getPavadinimas(),
                        "/api/malfunction/file/" + f.getId()
                ))
                .toList();

    }

    public MalfunctionWithFilesDTO getMalfunctionWithFiles(Integer gedimasId) {
        Malfunction gedimas = malfunctionRepository.findById(gedimasId)
                .orElseThrow(() -> new RuntimeException("Gedimas nerastas"));

        List<MalfunctionFiles> files = malfunctionFilesRepository.findByGedimasId(gedimasId);

        List<MalfunctionFilesDTO> fileDTOs = files.stream()
                .map(f -> {
                    String downloadUrl = "/api/malfunction/file/" + f.getId();
                    return new MalfunctionFilesDTO(f.getId().longValue(), f.getPavadinimas(), downloadUrl);
                })
                .toList();

        return new MalfunctionWithFilesDTO(
                gedimas.getId(),
                gedimas.getAprasymas(),
                gedimas.getTipas(),
                gedimas.getUzregistravimoData(),
                gedimas.getAddressJson(),
                fileDTOs
        );
    }

    public ResponseEntity<?> downloadFileById(Long id) {
        try {
            MalfunctionFiles fileEntry = malfunctionFilesRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Failas nerastas"));

            Path filePath = Path.of(fileEntry.getFizinisKelias());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntry.getPavadinimas() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileBytes.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Klaida: " + e.getMessage());
        }
    }

    public List<MalfunctionPrintDTO> getUnassignedMalfunctions() {
        User user = usersService.getCurrent();
        Employee employee = usersService.getEmployee(user);
        if (employee == null) {
            throw new RuntimeException("Darbuotojas nerastas");
        }

        return malfunctionRepository.findAll().stream()
                .filter(m -> m.getRegisteredMalfunctions() != null &&
                        m.getRegisteredMalfunctions().getDarbuotojas() == null)
                .map(m -> new MalfunctionPrintDTO(
                        m.getId(),
                        m.getAprasymas(),
                        m.getTipas(),
                        m.getUzregistravimoData(),
                        getGyventojasVardas(m),
                        m.getAddressJson()
                ))
                .toList();
    }

    private String getGyventojasVardas(Malfunction m) {
        if (m.getRegisteredMalfunctions() != null &&
                m.getRegisteredMalfunctions().getGyventojas() != null &&
                m.getRegisteredMalfunctions().getGyventojas().getUser() != null) {
            var user = m.getRegisteredMalfunctions().getGyventojas().getUser();
            return user.getVardas() + " " + user.getPavarde();
        }
        return "Nežinomas gyventojas";
    }

    public void deleteMalfunction(Integer id) {
        Malfunction malfunction = malfunctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gedimas nerastas"));

        List<MalfunctionFiles> files = malfunctionFilesRepository.findByGedimasId(id);
        for(MalfunctionFiles file : files){
            deleteMalfunctionFile(Long.valueOf(file.getId()));
        }

        Employee employee = usersService.getEmployee(usersService.getCurrent());
        if (employee == null) {
            throw new RuntimeException("Darbuotojas nerastas");
        }
        malfunctionRepository.delete(malfunction);
    }
    public List<Integer> getCurrentClientMalfunctionIds() {
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);

        if (client == null) {
            throw new RuntimeException("Naudotojas nėra klientas");
        }

        List<RegisteredMalfunctions> regs = registeredMalfunctionsRepository.findByGyventojas(client);

        return regs.stream()
                .map(RegisteredMalfunctions::getGedimas)
                .filter(Objects::nonNull)
                .map(Malfunction::getId)
                .toList();
    }

    public void registerWithFiles(
            String aprasymas,
            String tipas,
            String addressJson,
            MultipartFile[] files
    ) {
        // 1) Registracijos įrašas
        RegisteredMalfunctions reg = new RegisteredMalfunctions();
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);
        reg.setGyventojas(client);
        reg.setDarbuotojas(pickBestEmployeeFor(tipas));
        registeredMalfunctionsRepository.save(reg);

        // 2) Paties gedimo įrašymas (dabar su addressJson)
        Malfunction malfunction = new Malfunction();
        malfunction.setAprasymas(aprasymas);
        malfunction.setTipas(tipas);
        malfunction.setUzregistravimoData(LocalDateTime.now());
        malfunction.setAddressJson(addressJson);            // ← svarbiausias įrašymas
        malfunction.setRegisteredMalfunctions(reg);
        malfunctionRepository.save(malfunction);

        // 3) Jeigu yra failų – įrašome juos kaip iki šiol
        if (files != null && files.length > 0) {
            try {
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                for (MultipartFile file : files) {
                    if (file.isEmpty()) continue;
                    String originalName = file.getOriginalFilename();
                    String ext = originalName != null && originalName.contains(".")
                            ? originalName.substring(originalName.lastIndexOf('.'))
                            : "";
                    String unique = UUID.randomUUID() + ext;
                    Path target = uploadDir.resolve(unique);
                    Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                    MalfunctionFiles mf = new MalfunctionFiles();
                    mf.setFizinisKelias(target.toString());
                    mf.setPavadinimas(originalName);
                    mf.setGedimas(malfunction);
                    malfunctionFilesRepository.save(mf);
                }
            } catch (IOException e) {
                throw new RuntimeException("Nepavyko įrašyti failų: " + e.getMessage());
            }
        }
    }

    public ResponseEntity<Object> deleteMalfunctionFile(Long fileId) {
        MalfunctionFiles file = malfunctionFilesRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Failas nerastas"));
        User current = usersService.getCurrent();
        Employee employee = usersService.getEmployee(current);

        RegisteredMalfunctions reg = file.getGedimas().getRegisteredMalfunctions();
        if(employee != null){
            if (!employee.equals(reg.getDarbuotojas())) {
                throw new RuntimeException("Neturite teisės ištrinti šio failo");
            }
        }
        Path path = Path.of(file.getFizinisKelias());
        try {
            Files.deleteIfExists(path);
            malfunctionFilesRepository.delete(file);
            Map<String, String> response = new HashMap<>();
            response.put("success", "File deleted");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to delete file");
            return ResponseEntity.badRequest().body(response);
        }
    }
    public List<MalfunctionPrintDTO> getMalfunctionsByCurrentEmployee() {
        // 1) Paimame dabar prisijungusį vartotoją ir jo employee profilį
        User current = usersService.getCurrent();
        Employee employee = usersService.getEmployee(current);
        if (employee == null) {
            throw new RuntimeException("Darbuotojas nerastas");
        }

        // 2) Filtruojame visus gedimus, kurių RegisteredMalfunctions.darbuotojas == šis employee
        return malfunctionRepository.findAll().stream()
                .filter(m -> m.getRegisteredMalfunctions() != null
                        && employee.equals(m.getRegisteredMalfunctions().getDarbuotojas()))
                .map(m -> new MalfunctionPrintDTO(
                        m.getId(),
                        m.getAprasymas(),
                        m.getTipas(),
                        m.getUzregistravimoData(),
                        getGyventojasVardas(m),
                        m.getAddressJson()
                ))
                .toList();
    }

    private EmployeeRole mapTypeToRole(String tipas) {
        return switch (tipas) {
            case "Elektra", "Šildymas", "Apšvietimas" ->
                    EmployeeRole.Energetikos_skyrius;
            case "Vandentiekis" ->
                    EmployeeRole.Santechnikos_skyrius;
            default ->
                    EmployeeRole.Bendras_skyrius;
        };
    }

    private Employee pickBestEmployeeFor(String tipas) {
        // 1) Pagrindinės rolės nustatymas pagal gedimo tipą
        String primaryRole = mapTypeToRole(tipas).name();

        // 2) Iš pradžių bandome rasti darbuotojus šioje rolėje
        List<Employee> candidates = employeeRepository.findByRole(primaryRole);

        // 3) Jei nėra – nukreipiame į bendrą skyrių
        if (candidates.isEmpty()) {
            candidates = employeeRepository.findByRole(EmployeeRole.Bendras_skyrius.name());
        }

        // 4) Jei ir „Bendras“ tuščias – paimame visus darbuotojus
        if (candidates.isEmpty()) {
            candidates = employeeRepository.findAll();
        }

        // 5) Jei vis tiek nėra – meta klaidą (sistema be darbuotojų)
        if (candidates.isEmpty()) {
            throw new RuntimeException("Sistemoje nėra nė vieno darbuotojo");
        }

        // 6) Suskaičiuojame kiek gedimų jau turi kiekvienas kandidatas
        Map<Employee, Long> load = candidates.stream().collect(Collectors.toMap(
                e -> e,
                e -> malfunctionRepository.countByRegisteredMalfunctions_Darbuotojas(e)
        ));

        // 7) Randame mažiausią apkrovą turintį(-čius)
        long minLoad = load.values().stream().min(Long::compare).orElse(0L);
        List<Employee> leastLoaded = load.entrySet().stream()
                .filter(ent -> ent.getValue() == minLoad)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 8) Jei keli – paimame atsitiktinį
        Collections.shuffle(leastLoaded);
        return leastLoaded.get(0);
    }




}