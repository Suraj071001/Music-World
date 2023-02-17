package com.example.android.musicworld.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.android.musicworld.R;
import com.example.android.musicworld.databinding.ActivityAllowAccessBinding;
import com.example.android.musicworld.databinding.ActivityMainBinding;

import java.security.Permission;

public class AllowAccess extends AppCompatActivity {

    private static final int REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAllowAccessBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_allow_access);

        binding.button.setOnClickListener(view -> {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
            }else{
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startActivity(new Intent(AllowAccess.this,MainActivity.class));
            }else{
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
            }
    }
}