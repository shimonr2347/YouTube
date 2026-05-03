package com.example.utube.data;

import android.app.Application;

import com.example.utube.models.UserEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private UserDao userDao;
    private ExecutorService executorService;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(UserEntity user) {
        executorService.execute(() -> userDao.insert(user));
    }

    public UserEntity getUserByUsername(String username) {
        try {
            return executorService.submit(() -> userDao.getUserByUsername(username)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<UserEntity> getAllUsers() {
        try {
            return executorService.submit(userDao::getAllUsers).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean userExists(String username) {
        try {
            return executorService.submit(() -> userDao.userExists(username)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateUser(String username, String password) {
        try {
            return executorService.submit(() -> userDao.validateUser(username, password)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}