package com.example.utube.viewmodels;

import androidx.lifecycle.ViewModel;

public class AddCommentViewModel extends ViewModel {
    public boolean isCommentValid(String comment) {
        return comment != null && !comment.trim().isEmpty();
    }
}