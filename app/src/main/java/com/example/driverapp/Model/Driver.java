package com.example.driverapp.Model;

public class Driver {

    private String Email;
    private String Password;
    private String ConfirmPassword;
    private String FullName;
    private String MobileNumber;
    private String imageUri;

    public Driver() {
    }

    public Driver(String email, String password, String confirmPassword, String fullName, String mobileNumber, String imageUri) {
        Email = email;
        Password = password;
        ConfirmPassword = confirmPassword;
        FullName = fullName;
        MobileNumber = mobileNumber;
        this.imageUri = imageUri;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getConfirmPassword() {
        return ConfirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        ConfirmPassword = confirmPassword;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getMobileNumber() {
        return MobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        MobileNumber = mobileNumber;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
