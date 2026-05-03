package com.example.utube.models;

import android.app.Application;

import com.example.utube.MyApplication;
import com.example.utube.data.UserRepository;
import com.example.utube.models.UserEntity;

public class Users {
    private static Users instance;
    private UserRepository userRepository;

    private Users() {
        userRepository = MyApplication.getInstance().getUserRepository();
    }

    public static synchronized Users getInstance() {
        if (instance == null) {
            instance = new Users();
        }
        return instance;
    }

    public void addUser(String username, String password, String firstName, String lastName, String dob, String email, String profilePic) {
        if (!userRepository.userExists(username)) {
            UserEntity newUser = new UserEntity(username, password, firstName, lastName, dob, email, profilePic);
            userRepository.insert(newUser);
        } else {
            throw new IllegalArgumentException("Username already exists");
        }
    }

    public UserEntity getUser(String username) {
        return userRepository.getUserByUsername(username);
    }

    public boolean userExists(String username) {
        return userRepository.userExists(username);
    }

    public boolean validateUser(String username, String password) {
        return userRepository.validateUser(username, password);
    }


    // User class to store individual user details
    public static class User {
        private String username;
        private String password;
        private String firstName;
        private String lastName;
        private String dob;
        private String email;
        private String profilePic;

        public User(String username, String password, String firstName, String lastName, String dob, String email, String profilePic) {
            this.username = username;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.dob = dob;
            this.email = email;
            this.profilePic = profilePic;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getDob() {
            return dob;
        }

        public String getEmail() {
            return email;
        }

        public String getProfilePic() {
            return profilePic;
        }
    }
}
