package com.example.pvpbackend.services;

import com.example.pvpbackend.DTO.ClientListDTO;
import com.example.pvpbackend.DTO.UserClientDataUpdateDTO;
import com.example.pvpbackend.DTO.UserDataUpdateDTO;
import com.example.pvpbackend.enums.EmployeeRole;
import com.example.pvpbackend.models.*;
import com.example.pvpbackend.repositories.ClientRepository;
import com.example.pvpbackend.repositories.EmployeeRepository;
import com.example.pvpbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsersService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    private PasswordEncoder passwordEncoder;


    public boolean isEmailTaken(String email) {
        return userRepository.findByelPastas(email).isPresent();
    }

    public User findByEmail(String email) {
        Optional<User> optionalUser = userRepository.findByelPastas(email);
        return optionalUser.orElse(null);
    }

    public User getCurrent(){
        UserDetails details = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = details.getUsername();
        return this.userRepository.findByelPastas(email).orElse(null);
    }
    public Client getClient(User user){
        return this.clientRepository.findById(user.getId()).orElse(null);
    }
    public Employee getEmployee(User user){
        return this.employeeRepository.findById(user.getId()).orElse(null);
    }
    public User get(Integer id) {
        return this.userRepository.findById(id).orElse(null);
    }

    public User update(Integer id, User user) {
        return this.userRepository.save(user);
    }

    public void updateUser(UserDataUpdateDTO updateDTO) throws AuthenticationException {
        User user = getCurrent();
        user.setVardas(updateDTO.getVardas());
        user.setPavarde(updateDTO.getPavarde());
        user.setElPastas(updateDTO.getElPastas());
        user.setTelNumeris(updateDTO.getTelNumeris());
        userRepository.save(user);
    }

    public void updateEmployeeRole(String role) throws AuthenticationException{
        User user = getCurrent();
        Employee employee = getEmployee(user);
        if (employee == null) throw new AuthenticationException("Unauthorized");
        boolean valid = false;
        for(EmployeeRole value : EmployeeRole.values()){
            if (value.name().equals(role)) {
                valid = true;
                break;
            }
        }
        if(!valid) throw new AuthenticationException("Unauthorized - bad role");
        employee.setRole(role);
        employeeRepository.save(employee);
    }

    @Transactional
    public void updateClientUser(UserClientDataUpdateDTO updateDTO) throws AuthenticationException{
        User user = getCurrent();
        Client client = getClient(user);
        if(client == null){throw new AuthenticationException("Unauthorized");}
        client.setElPastoPriminimai(updateDTO.getEmailReminder());
        client.setTelNumerioPriminimai(updateDTO.getNumberReminder());
        LocalDate today = LocalDate.now();
        Date sqlDate = Date.valueOf(today);
        client.setAtnaujintiDuomenys(sqlDate);
        clientRepository.save(client);
        updateUser(updateDTO.getUser());
    }

    public Date getClientUpdateDate() throws AuthenticationException{
        User user = getCurrent();
        Client client = getClient(user);
        if(client == null) throw new AuthenticationException("Not client");
        return client.getAtnaujintiDuomenys();
    }

    public void delete(Integer id) {
        this.userRepository.deleteById(id);
    }

    public User create(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getSlaptazodis());
        user.setSlaptazodis(encodedPassword);
        return this.userRepository.save(user);
    }

    public UserClientDataUpdateDTO getUserClient() throws AuthenticationException {
        User user = getCurrent();
        Client client = getClient(user);
        if(client == null) throw new AuthenticationException("Unauthorized");
        UserClientDataUpdateDTO dataDTO = new UserClientDataUpdateDTO();

        UserDataUpdateDTO userDTO = new UserDataUpdateDTO();
        userDTO.setVardas(user.getVardas());
        userDTO.setPavarde(user.getPavarde());
        userDTO.setElPastas(user.getElPastas());
        userDTO.setTelNumeris(user.getTelNumeris());
        dataDTO.setUser(userDTO);
        dataDTO.setEmailReminder(client.getElPastoPriminimai());
        dataDTO.setNumberReminder(client.getTelNumerioPriminimai());
        return dataDTO;
    }

    public Employee createEmployee(Integer id){
        Employee  temp = new Employee(id);
        return this.employeeRepository.save(temp);
    }

    public Client createClient(Integer id){
        Client temp = new Client(id);
        temp.setElPastoPriminimai(false);
        temp.setTelNumerioPriminimai(false);
        return this.clientRepository.save(temp);
    }

    public List<User> list() {
        return this.userRepository.findAll();
    }
    public List<Client> listClients(){
        return clientRepository.findAll();
    }

    public List<ClientListDTO> listClientsDTO(){
        List<Client> clients = listClients();
        List<ClientListDTO> clientList = new ArrayList<>();
        for(Client client : clients){
            User user = client.getUser();
            ClientListDTO newClient = new ClientListDTO();
            newClient.setVardas(user.getVardas());
            newClient.setPavarde(user.getPavarde());
            newClient.setId(client.getIdNaudotojas());
            List<Address> addresses = client.getAdresai();
            for(Address address: addresses){
                address.setGyventojas(null);
            }
            newClient.setAddressList(addresses);
            clientList.add(newClient);
        }
        return clientList;
    }
}
