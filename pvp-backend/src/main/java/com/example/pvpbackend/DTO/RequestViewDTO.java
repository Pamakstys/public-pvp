package com.example.pvpbackend.DTO;

import com.example.pvpbackend.models.Request;
import com.example.pvpbackend.models.RequestField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestViewDTO {
    private Request request;
    private List<RequestField> fields;
}
