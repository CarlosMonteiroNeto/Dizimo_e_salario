package com.dizimo_e_salario;

import android.app.Application;

import androidx.lifecycle.ViewModelProvider;

public class MinhaAplicacao extends Application {
    private SharedViewModel viewModel;

    @Override
    public void onCreate() {
        super.onCreate();
        viewModel = new ViewModelProvider.AndroidViewModelFactory(this).create(SharedViewModel.class);
    }

    public SharedViewModel getViewModel() {
        return viewModel;
    }
}
