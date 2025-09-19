package com.devops.crudapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @UuidGenerator
    @Column(name = "uuid", columnDefinition = "CHAR(36)")
    private UUID uuid;

    @NotBlank(message = "Le nom complet est requis")
    @Column(name = "fullname", nullable = false)
    private String fullname;

    @NotBlank(message = "Le niveau d'étude est requis")
    @Column(name = "study_level", nullable = false)
    private String studyLevel;

    @NotNull(message = "L'âge est requis")
    @Min(value = 1, message = "L'âge doit être positif")
    @Column(name = "age", nullable = false)
    private Integer age;

    public User() {}

    public User(String fullname, String studyLevel, Integer age) {
        this.fullname = fullname;
        this.studyLevel = studyLevel;
        this.age = age;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    @Override
    public String toString() {
        return "User{" +
                "uuid=" + uuid +
                ", fullname='" + fullname + '\'' +
                ", studyLevel='" + studyLevel + '\'' +
                ", age=" + age +
                '}';
    }
}