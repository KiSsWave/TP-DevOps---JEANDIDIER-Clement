package com.devops.crudapi.dto;

import com.devops.crudapi.entity.User;
import java.util.UUID;

public class UserResponse {

    private UUID uuid;
    private String fullname;
    private String studyLevel;
    private Integer age;

    public UserResponse() {}

    public UserResponse(User user) {
        this.uuid = user.getUuid();
        this.fullname = user.getFullname();
        this.studyLevel = user.getStudyLevel();
        this.age = user.getAge();
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
}