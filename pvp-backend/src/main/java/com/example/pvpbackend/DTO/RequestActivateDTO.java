package com.example.pvpbackend.DTO;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestActivateDTO {
    private Integer requestId;
    private Boolean active;
}
