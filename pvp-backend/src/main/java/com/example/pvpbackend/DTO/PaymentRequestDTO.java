package com.example.pvpbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDTO {
    private List<BillPaymentDTO> items;

}
