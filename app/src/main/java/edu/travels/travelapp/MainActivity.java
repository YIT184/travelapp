package edu.travels.travelapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import edu.travels.travelapp.ui.home.HomeFragment ;
import edu.travels.travelapp.ui.map.MapFragment ;
import edu.travels.travelapp.ui.user.UserFragment;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 高德地图隐私合规声明（必须在super.onCreate之后）
        try {
            MapsInitializer.updatePrivacyShow(this, true, true);
            MapsInitializer.updatePrivacyAgree(this, true);
            ServiceSettings.updatePrivacyShow(this, true, true);
            ServiceSettings.updatePrivacyAgree(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // 默认打开首页
        replaceFragment(new HomeFragment());
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());

            } else if (id == R.id.nav_map) {
                replaceFragment(new MapFragment());

            } else if (id == R.id.nav_profile) {
                replaceFragment(new UserFragment());
            }
            return true;
        });
    }

    public void jumpToPublish() {
        // 切换到发布
        replaceFragment(new UserFragment());
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}