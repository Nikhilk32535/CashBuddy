package com.example.cashbuddy;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Load the first fragment (CashDenominationFragment)
        loadFragment(new cash_denomination());

        // Optional: Handle fragment back stack
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    // Pop the last fragment from back stack
                    fm.popBackStack();
                } else {
                    // If no fragments in back stack, finish the activity
                    finish();
                }
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        // Get NavController from NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
    }
}
