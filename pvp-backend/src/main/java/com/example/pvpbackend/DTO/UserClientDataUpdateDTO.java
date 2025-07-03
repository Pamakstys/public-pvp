package com.example.pvpbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserClientDataUpdateDTO {
    private boolean emailReminder;
    private boolean numberReminder;
    private UserDataUpdateDTO user;

    public boolean getEmailReminder(){
        return this.emailReminder;
    }
    public boolean getNumberReminder(){
        return this.numberReminder;
    }
}
