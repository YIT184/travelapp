package edu.travels.travelapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import edu.travels.travelapp.ui.user.UserFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 直接加载你的登录页面
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new UserFragment())
                    .commit();
        }
    }
}