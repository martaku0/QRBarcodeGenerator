package com.example.barcodegenerator.ui.barqr;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BarqrViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public BarqrViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is barqr fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}