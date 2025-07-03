package com.example.pvpbackend.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Data
public class Contract {
    private String description;
    private String frequency;
    private String fullAddress;
    private int wasteObjectId;
    private List<ScheduleDate> dates;

    public Contract(String description, String frequency, String fullAddress, Integer wasteObjectId){
        this.description = description;
        this.frequency = frequency;
        this.fullAddress = fullAddress;
        this.wasteObjectId = wasteObjectId;
        this.dates = new ArrayList<>();
    }
}
