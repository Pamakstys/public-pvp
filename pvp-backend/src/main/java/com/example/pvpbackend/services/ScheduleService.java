package com.example.pvpbackend.services;

import com.example.pvpbackend.models.Address;
import com.example.pvpbackend.models.Contract;
import com.example.pvpbackend.models.ScheduleDate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.TimeZoneRegistry;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class ScheduleService {

    private final AddressService addressService;
    public ScheduleService(AddressService addressService){
        this.addressService = addressService;
    }
    public String getWasteObjects(int wasteObjectId) {
        String baseUrl = "https://grafikai.svara.lt/api/schedule";
        String wasteObject = Integer.toString(wasteObjectId);

        String url = String.format(
                "%s?wasteObjectId=%s",
                baseUrl,
                encode(wasteObject)
        );
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
    public String getContracts(String address, String region, int houseNum, String subDistrict) {
        String baseUrl = "https://grafikai.svara.lt/api/contracts";
        String houseNumber = Integer.toString(houseNum);
        String url = String.format(
                "%s?pageSize=100&pageIndex=0&address=%s&region=%s&houseNumber=%s&subDistrict=%s&matchHouseNumber=true",
                baseUrl,
                encode(address),
                encode(region),
                encode(houseNumber),
                encode(subDistrict)
        );
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    public List<Contract> getFormatedSchedules(String _address, int _houseNumber, String _subDistrict){
        String response = getContracts(_address, "Kauno r. sav.", _houseNumber, _subDistrict);
        if(response == null){return null;}
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            Object totalRecordsObj = responseMap.get("totalRecords");
            if(totalRecordsObj != null) {
                int totalRecords = (int) totalRecordsObj;
                if(totalRecords == 0){return null;}
            }
            Object dataObj = responseMap.get("data");
            List<Contract> contracts = new ArrayList<>();
            if(dataObj instanceof List){
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObj;
                for (Map<String, Object> dataItem : dataList) {
                    String description = (String) dataItem.get("description");
                    String frequency = (String) dataItem.get("frequency");
                    String address = (String) dataItem.get("fullAddress");
                    Integer wasteObjectId = (Integer) dataItem.get("wasteObjectId");
                    Contract contract = new Contract(description, frequency, address, wasteObjectId);

                    String wasteObjectsResponse = getWasteObjects(wasteObjectId);
                    if(wasteObjectsResponse == null){
                        return null;
                    }
                    ObjectMapper wasteObjectMapper = new ObjectMapper();
                    try {
                        List<ScheduleDate> scheduleDates = wasteObjectMapper.readValue(wasteObjectsResponse, new TypeReference<List<ScheduleDate>>() {});
                        contract.setDates(scheduleDates);
                        contracts.add(contract);
                    } catch (IOException e) {return null;}
                }
            }
            return contracts;
        } catch (IOException e) {return null;}
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public byte[] generateCalendarFile(List<ScheduleDate> scheduleDates, String description) throws Exception {
        Calendar calendar = new Calendar();
        calendar.add(new ProdId("-//Rubbish Schedule" + description + "//iCal4j 1.0//EN"));
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timezone = registry.getTimeZone("Europe/Vilnius");

        calendar.add(Version.VERSION_2_0);
        calendar.add(CalScale.GREGORIAN);

        for(ScheduleDate date : scheduleDates){
            java.util.Calendar startDate = new GregorianCalendar();
            startDate.setTimeZone(timezone);
            startDate.set(java.util.Calendar.YEAR, date.getYear());
            startDate.set(java.util.Calendar.MONTH, date.getMonth());
            startDate.set(java.util.Calendar.DAY_OF_MONTH, date.getDay());
            startDate.set(java.util.Calendar.HOUR_OF_DAY, 7);
            startDate.set(java.util.Calendar.MINUTE, 0);
            startDate.set(java.util.Calendar.SECOND, 0);

//            java.util.Calendar endDate = new GregorianCalendar();
//            endDate.setTimeZone(timezone);
//            endDate.set(java.util.Calendar.YEAR, date.getYear());
//            endDate.set(java.util.Calendar.MONTH, date.getMonth());
//            endDate.set(java.util.Calendar.DAY_OF_MONTH, date.getDay());
//            endDate.set(java.util.Calendar.HOUR_OF_DAY, 17);
//            endDate.set(java.util.Calendar.MINUTE, 0);
//            endDate.set(java.util.Calendar.SECOND, 0);

            java.util.Date utilDate = startDate.getTime();
            java.time.Instant instantStart = utilDate.toInstant();
            VEvent event = new VEvent(instantStart, "Šiukšlių išvėžimas" + description);

            Uid uid = new Uid(UUID.randomUUID().toString());
            event.add(uid);
            calendar.add(event);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(calendar, outputStream);

        return outputStream.toByteArray();
    }

    public byte[] generateZip(int id) throws IOException {
        try {
            ByteArrayOutputStream zipOutStream = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(zipOutStream);

            Address address = addressService.getWithClientCheck(id);
            List<Contract> contracts = getFormatedSchedules(address.getGatve(), address.getNamoNumeris(), address.getSeniunija());
            if(contracts == null){
                return null;
            }
            int count = 1;
            for (Contract contract : contracts){
                List<ScheduleDate> scheduleDates = contract.getDates();
                String description = contract.getFullAddress() + " " + contract.getDescription();
                byte[] fileBytes = generateCalendarFile(scheduleDates, description);

                String fileName = "schedule" + count;
                zip.putNextEntry(new ZipEntry(fileName + ".ics"));
                zip.write(fileBytes);
                zip.closeEntry();

                count++;
            }
            zip.close();
            return zipOutStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
