package com.finisinfinitatis.authservice.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String emailAddress;

    @NotNull
    private String passwordHash;

    public User(String emailAddress, String passwordHash){
        this.emailAddress = emailAddress;
        this.passwordHash = passwordHash;
    }
}
