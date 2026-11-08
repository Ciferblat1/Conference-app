package com.github.Ciferblat1.conference_app.dto;


import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@Setter 
@Getter 
public class UpdateApplicationRequest {

    private boolean refreshAuthorSnapshot;

    @NotNull(message = "Научные руководители обязательны")
    @Size(min = 1, max = 2, message = "Нужны один или два руководителя")
    @Valid
    private List<@NotNull(message = "Руководитель не может быть null")
            SupervisorRequest> supervisors;


    @NotBlank(message = "Название обязательно")
    @Size(max = 255, message = "Название должно быть не длиннее 255 символов")
    private String title;


    @NotBlank(message = "Тезисы обязательны")
    private String theses;

    //Список литературы может быть пустым
    private List<
            @NotBlank(message = "Библиографическая запись не может быть пустой")
            String> referencesList;
}
