package com.zeba.permission.test;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zeba.permission.DeniedListener;
import com.zeba.permission.GrantedListener;
import com.zeba.permission.ZebaPermission;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ZebaPermission.request(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new GrantedListener() {
            @Override
            public void granted() {

            }
        }, new DeniedListener() {
            @Override
            public boolean denied() {

                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ZebaPermission.onRequestPermissionsResult(this,requestCode,permissions,grantResults);
    }
}
