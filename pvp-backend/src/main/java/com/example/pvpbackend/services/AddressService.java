package com.example.pvpbackend.services;

import com.example.pvpbackend.DTO.AddressCreateDTO;
import com.example.pvpbackend.DTO.AddressEditDTO;
import com.example.pvpbackend.models.Address;
import com.example.pvpbackend.models.Client;
import com.example.pvpbackend.models.User;
import com.example.pvpbackend.repositories.AddressRepository;
import com.example.pvpbackend.repositories.ClientRepository;
import com.example.pvpbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;

    public Address get(int id){
        return this.addressRepository.findById(id).orElse(null);
    }

    public void save(Address address){
        addressRepository.save(address);
    }

    public void saveEdit(AddressEditDTO addressDTO){
        Address address = get(addressDTO.getId());
        address.setSeniunija(addressDTO.getSubDistrict());
        address.setGatve(addressDTO.getAddress());
        address.setNamoNumeris(addressDTO.getHouseNumber());
        addressRepository.save(address);
    }

    public void deleteWithClientCheck(int id){
        Address address = getWithClientCheck(id);
        addressRepository.delete(address);
    }
    public void delete(int id){
        addressRepository.deleteById(id);
    }

    public void create(AddressCreateDTO addressDTO, Client client){
        Address address = new Address(addressDTO.getSubDistrict(), addressDTO.getAddress(), addressDTO.getHouseNumber(), client);
        addressRepository.save(address);
    }

    public Address getWithClientCheck(int id){
        UserDetails details = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = details.getUsername();
        User user = userRepository.findByelPastas(email).orElse(null);
        assert user != null;
        Client client = clientRepository.findById(user.getId()).orElse(null);
        Address address = get(id);
        assert client != null;
        if(Objects.equals(address.getGyventojas().getIdNaudotojas(), client.getIdNaudotojas())){return address;}
        else {return null;}
    }
}
