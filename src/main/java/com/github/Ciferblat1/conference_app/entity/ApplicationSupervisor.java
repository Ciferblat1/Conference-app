package com.github.Ciferblat1.conference_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Embeddable 
@Getter
@NoArgsConstructor 
public class ApplicationSupervisor {
    
    @Column(name = "supervisor_first_name", nullable = false)
    private String firstName;

    @Column(name = "supervisor_surname", nullable = false)
    private String surname;

    @Column(name = "supervisor_patronymic", nullable = true)
    private String patronymic;

    @Column(name = "supervisor_position", nullable = false)
    private String position;

    @Column(name = "supervisor_department", nullable = false)
    private String department;



    public ApplicationSupervisor(
        String firstName,
        String surname,
        String patronymic,
        String department,
        String position
    ){
        this.firstName = firstName;
        this.surname = surname;
        this.patronymic = patronymic;
        this.department = department;
        this.position = position;
    }
}
