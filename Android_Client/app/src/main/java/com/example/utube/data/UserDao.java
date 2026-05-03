package com.example.utube.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.utube.models.UserEntity;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username")
    UserEntity getUserByUsername(String username);

    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    boolean userExists(String username);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username AND password = :password)")
    boolean validateUser(String username, String password);
}