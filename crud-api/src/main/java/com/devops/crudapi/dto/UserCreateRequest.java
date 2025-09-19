package com.devops.crudapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserCreateRequest {

    @NotBlank(message = "Le nom complet est requis")
    private String fullname;

    @NotBlank(message = "Le niveau d'étude est requis")
    private String studyLevel;

    @NotNull(message = "L'âge est requis")
    @Min(value = 1, message = "L'âge doit être positif")
    private Integer age;

    public UserCreateRequest() {}

    public UserCreateRequest(String fullname, String studyLevel, Integer age) {
        this.fullname = fullname;
        this.studyLevel = studyLevel;
        this.age = age;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStudyLevel() {
        return studyLevel;
    }

    public void setStudyLevel(String studyLevel) {
        this.studyLevel = studyLevel;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}