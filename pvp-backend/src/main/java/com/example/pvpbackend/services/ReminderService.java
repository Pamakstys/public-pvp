package com.example.pvpbackend.services;

import com.example.pvpbackend.enums.RegisteredRequestType;
import com.example.pvpbackend.models.*;
import com.example.pvpbackend.repositories.ClientRepository;
import com.example.pvpbackend.repositories.EmployeeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReminderService {
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final ScheduleService scheduleService;
    public ReminderService(EmailService emailService, ClientRepository clientRepository, ScheduleService scheduleService, EmployeeRepository employeeRepository) {
        this.emailService = emailService;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.scheduleService = scheduleService;
    }

    @Scheduled(cron = "0 0 12 * * *", zone = "Europe/Vilnius")
    public void sendDailyReminders() {
        sendDailyRemindersInternal();
    }

    public void sendDailyRemindersInternal(){
        List<Client> clients = clientRepository.findAll();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        for (Client client : clients) {
            List<Contract> sendableContracts = new ArrayList<>();
            for (Address address : client.getAdresai()) {
                List<Contract> contracts = scheduleService.getFormatedSchedules(address.getGatve(), address.getNamoNumeris(), address.getSeniunija());
                for(Contract contract : contracts){
                    List<ScheduleDate> dates = contract.getDates();
                    for(ScheduleDate date : dates){
                        if(date.getYear() == tomorrow.getYear()
                                && date.getMonth() == tomorrow.getMonthValue()
                                && date.getDay() == tomorrow.getDayOfMonth()){
                            sendableContracts.add(contract);
                            break;
                        }
                    }
                }
            }
            if(!sendableContracts.isEmpty()){
                try{
                    emailService.sendReminderEmail(client, sendableContracts);
                } catch (Exception e){
                    continue;
                }
            }
        }
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Europe/Vilnius")
    public void sendDailyRemindersRequests() {
        sendDailyRequestRemindersInternal();
    }

    public void sendDailyRequestRemindersInternal(){
        List<Employee> employees = employeeRepository.findAll();
        for(Employee employee: employees){
            List<RegisteredRequest> requests = employee.getRegisteredRequests();
            List<RegisteredRequest> sendableRequests = new ArrayList<>();
            for(RegisteredRequest request : requests){
                if(request.getBusenaEnum() != RegisteredRequestType.Atmestas && request.getBusenaEnum() != RegisteredRequestType.Ivykdytas){
                    sendableRequests.add(request);
                }
            }
            if(!sendableRequests.isEmpty()){
                try{
                    emailService.sendNewRequestReminderEmail(employee, sendableRequests);
                } catch (Exception e){
                    continue;
                }
            }
        }
    }
}
