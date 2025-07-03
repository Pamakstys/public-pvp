package com.example.pvpbackend.controllers;

import com.example.pvpbackend.DTO.AddressCreateDTO;
import com.example.pvpbackend.DTO.AddressEditDTO;
import com.example.pvpbackend.models.Address;
import com.example.pvpbackend.models.Client;
import com.example.pvpbackend.models.User;
import com.example.pvpbackend.repositories.AddressRepository;
import com.example.pvpbackend.repositories.ClientRepository;
import com.example.pvpbackend.repositories.UserRepository;
import com.example.pvpbackend.services.AddressService;
import com.example.pvpbackend.services.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/address")
@Slf4j
public class AddressController {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;
    private final AddressService addressService;
    private final UsersService usersService;
    public AddressController(AddressService addressService, UsersService usersService){
        this.addressService = addressService;
        this.usersService = usersService;
    }

    @PostMapping("/edit")
    public ResponseEntity<Object> editAddress(@RequestBody AddressEditDTO addressDTO){
        try {
            addressService.saveEdit(addressDTO);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Address edited successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to edit address");
            return ResponseEntity.badRequest().body(response);
        }
    }



    @GetMapping("/view/{id}")
    public ResponseEntity<Address> viewAddress(@PathVariable int id){
        Address address = addressService.getWithClientCheck(id);
        address.setGyventojas(null);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteAddress(@PathVariable int id) {
        try {
            addressService.deleteWithClientCheck(id);
            Map<String, String> response = new HashMap<>();
            response.put("success", "Address deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to delete address");
            return ResponseEntity.badRequest().body(response);
        }

    }

    @PostMapping("/create")
    public ResponseEntity<Object> createAddress(@RequestBody AddressCreateDTO addressDTO){
        try {
            Map<String, String> response = new HashMap<>();

            User user = usersService.getCurrent();
            Client client = usersService.getClient(user);
            if(client == null){
                response.put("error", "User is not client");
                return ResponseEntity.badRequest().body(response);
            }
            addressService.create(addressDTO, client);
            response.put("success", "Address added");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to create address");
            return ResponseEntity.badRequest().body(response);
        }
    }


    @GetMapping("/list")
    public ResponseEntity<Object> getAllAddressesForUser() {
        try {
            UserDetails details = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String email = details.getUsername();
            Optional<User> optionalUser = userRepository.findByelPastas(email);
            if (optionalUser.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            User user = optionalUser.get();
            Optional<Client> optionalClient = clientRepository.findById(user.getId());
            if (optionalClient.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Client not found");
                return ResponseEntity.badRequest().body(response);
            }
            Client client = optionalClient.get();

            List<Address> addresses = addressRepository.findByGyventojas(client);
            for (Address address : addresses) {
                address.setGyventojas(null);
            }
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to fetch addresses");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
