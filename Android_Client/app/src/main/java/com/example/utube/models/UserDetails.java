package com.example.utube.models;

import java.io.File;

public class UserDetails {
    private String firstName;
    private String lastName;
    private String date;
    private String email;

    private String username;
    private String password;
    private String passwordConfirm;


    private File profilePicFile;

    private String profilePic;
    private String _id;

    private String token = "";
    private static UserDetails instance;
    private boolean signIn = false;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static void setInstance(UserDetails instance) {
        UserDetails.instance = instance;
    }

    private UserDetails() {
    }

    public static synchronized UserDetails getInstance() {
        if (instance == null) {
            instance = new UserDetails();
        }
        return instance;
    }

    public File getProfilePicFile() {
        return profilePicFile;
    }

    public void setProfilePicFile(File profilePicFile) {
        this.profilePicFile = profilePicFile;
    }

    public boolean isSignIn() {
        return signIn;
    }

    public void setSignIn(boolean signIn) {
        this.signIn = signIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getPassword() {
        return password;
    }

    //
    public void setPassword(String password) {
        this.password = password;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public void clear() {
        this.signIn = false;
        this.username = null;
        this.token = null;
        this._id = null;
        this.email = null;
        this.firstName = null;
        this.lastName = null;
        this.profilePic = null;
        this.date = null;
        this.profilePicFile = null;
        this.password = null;
        this.passwordConfirm = null;
    }


}
