package com.example.pvpbackend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDate {
    private String date;
    private int year;
    private int month;
    private int day;
    private String dateFmt;
    private String weekDay;
}
