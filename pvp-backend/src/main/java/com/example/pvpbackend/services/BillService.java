package com.example.pvpbackend.services;

import com.example.pvpbackend.DTO.BillCreateDTO;
import com.example.pvpbackend.DTO.BillPaymentDTO;
import com.example.pvpbackend.models.Address;
import com.example.pvpbackend.models.Bill;
import com.example.pvpbackend.models.Client;
import com.example.pvpbackend.models.User;
import com.example.pvpbackend.repositories.BillRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.LineItem;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BillService {
    @Autowired
    private BillRepository billRepository;

    private final UsersService usersService;
    private final AddressService addressService;
    public BillService (UsersService usersService, AddressService addressService) {
        this.usersService = usersService;
        this.addressService = addressService;
    }

    public Bill getBill(int id) throws AuthenticationException {
        Bill bill = billRepository.findById(id).orElse(null);
        if(bill == null) return null;
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);
        if(client == null) throw new AuthenticationException("Unauthorized - Client not found");
        if(!Objects.equals(bill.getGyventojas().getIdNaudotojas(), client.getIdNaudotojas())) {
            throw new AuthenticationException("Unauthorized - Bill does not belong to the current user");
        }
        bill.setAdresas(null);
        bill.setGyventojas(null);
        return bill;
    }

    public Bill getBillById(int id) {
        return billRepository.findById(id).orElse(null);
    }

    public List<Bill> getBills(List<BillPaymentDTO> items){
        try {
            List<Bill> bills = new ArrayList<>();
            for(BillPaymentDTO item : items){
                Bill bill = getBill(item.getId());
                if(bill == null) return null;
                if(bill.getSumoketa()) return null;
                if(item.getSum() <= 0) continue;
                bill.setSelectedAmountToPay(item.getSum());
                bills.add(bill);
            }
            return bills;
        } catch (Exception e){
            return null;
        }
    }

    public Object createBill(BillCreateDTO createDTO){
        User user = usersService.get(createDTO.getClientId());
        Client client = usersService.getClient(user);
        if(client == null) return null;
        Address address = addressService.get(createDTO.getAddressId());
        Bill bill = new Bill(createDTO.getImones_pavadinimas(), createDTO.getMokejimo_data(), createDTO.getSumoketi_iki(), createDTO.getGavimo_data(), createDTO.getIban(), createDTO.getSuma(), createDTO.getImokos_kodas(), createDTO.getAprasymas(), createDTO.getSumoketa_suma(), false, createDTO.getJson(), address, client);
        billRepository.save(bill);
        return bill.getId();
    }

    public List<Bill> getBillsByPaid(Boolean paid) throws AuthenticationException {
        User user = usersService.getCurrent();
        Client client = usersService.getClient(user);
        if(client == null) throw new AuthenticationException("Unauthorized");
        List<Bill> bills = billRepository.findAllByGyventojasAndSumoketa(client, paid);
        for(Bill bill : bills){
            bill.setGyventojas(null);
            Address address = bill.getAdresas();
            address.setGyventojas(null);
            bill.setAdresas(address);
        }
        return bills;
    }

    @Transactional
    public Boolean payBills(List<LineItem> lineItems) throws StripeException{
        for(LineItem lineItem : lineItems){
            Price price = lineItem.getPrice();
            String productId = price.getProduct();
            Product product = Product.retrieve(productId);

            Map<String, String> metadata = product.getMetadata();
            String billId = metadata.get("billId");
            long amountToPay = price.getUnitAmount();
            try {
                Bill bill = getBillById(Integer.parseInt(billId));
                if(bill == null) throw new IllegalArgumentException("Bill not found");
                bill.setSumoketa_suma(bill.getSumoketa_suma() + amountToPay);
                if(Objects.equals(bill.getSumoketa_suma(), bill.getSuma())){
                    bill.setSumoketa(true);
                }
                bill.setMokejimo_data(LocalDateTime.now());
                billRepository.save(bill);
                bill.setSelectedAmountToPay(0.0);
            } catch (Exception e) {
                throw new RuntimeException("Payment processing failed for bill ID: " + billId, e);
            }
        }
        return true;
    }

}
