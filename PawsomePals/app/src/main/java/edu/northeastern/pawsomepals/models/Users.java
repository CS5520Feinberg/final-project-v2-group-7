package edu.northeastern.pawsomepals.models;

public class Users {

    private String userId;
    private String email;
    private String name;
    private String recipeId;
  //  private String userImg;
  private String profileImage;
    private String gender;
    private String dob;
    private String fcmToken;
    private boolean chatSelected = false;


    public Users() {
    }

    public Users(String name, String userId, String email,String fcmToken) {
        this.name = name;
        this.userId = userId;
        this.email = email;
        this.fcmToken = fcmToken;
    }

    public Users(String userId, String email) {
        this.userId = userId;
        this.email = email;

    }

    public Users(String userId, String email, String name, String recipeId, String profileImage) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.recipeId = recipeId;
        this.profileImage = profileImage;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
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

    public boolean isChatSelected() {
        return chatSelected;
    }

    public void setChatSelected(boolean chatSelected) {
        this.chatSelected = chatSelected;
    }
}

