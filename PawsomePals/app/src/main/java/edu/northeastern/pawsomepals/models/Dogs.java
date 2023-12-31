package edu.northeastern.pawsomepals.models;

import java.io.Serializable;

public class Dogs implements Serializable {
    private String dogId;
    private String userId;
    private String name;
    private String breed;
    private Boolean isMixedBreed;
    private String mixedBreed;
    private String profileImage;
    private String gender;
    private String dob;
    private String size;
    private Boolean isDeleted;

    private Boolean isExpandable;
    public Dogs() {

    }

    public Dogs(String dogId, String name, String breed, Boolean isMixedBreed, String mixedBreed, String profileImage, String gender, String dob, String size) {
        this.dogId = dogId;
        this.userId = userId;
        this.name = name;
        this.breed = breed;
        this.isMixedBreed = isMixedBreed;
        this.mixedBreed = mixedBreed;
        this.profileImage = profileImage;
        this.gender = gender;
        this.dob = dob;
        this.size = size;
        this.isDeleted = false;
        this.isExpandable = false;
    }

    public Boolean getExpandable() {
        return isExpandable;
    }

    public void setExpandable(Boolean expandable) {
        isExpandable = expandable;
    }

    public String getDogId() {
        return dogId;
    }

    public void setDogId(String dogId) {
        this.dogId = dogId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }


    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }


    public void setIsMixedBreed(Boolean mixedBreed) {
        isMixedBreed = mixedBreed;
    }

    public Boolean getIsMixedBreed() {
        return isMixedBreed;
    }

    public void setMixedBreed(String mixedBreed) {
        this.mixedBreed = mixedBreed;
    }

    public String getMixedBreed() {
        return mixedBreed;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
