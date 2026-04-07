package com.example.criminalintent;

import android.os.Bundle;
<<<<<<< HEAD
=======
import android.view.View;
>>>>>>> c126cc4 (Finish the Chapter 16, 17 and 18)
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity implements CrimeFragment.Callbacks {
    private boolean isTwoPane;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set status bar icons to white
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(0);
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
<<<<<<< HEAD
=======

        // Check if we're in two-pane mode (tablet)
        View detailFragmentContainer = findViewById(R.id.detail_fragment_container);
        isTwoPane = detailFragmentContainer != null && detailFragmentContainer.getVisibility() == View.VISIBLE;
>>>>>>> c126cc4 (Finish the Chapter 16, 17 and 18)

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CrimeListFragment())
                    .commit();
            
            // For tablets, show the first crime in detail pane
            if (isTwoPane) {
                // We'll let CrimeListFragment handle showing the first crime
            }
        }
    }

    public boolean isTwoPane() {
        return isTwoPane;
    }

    public void showCrimeDetail(Fragment fragment) {
        if (isTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        // Refresh the list fragment to show the updated crime
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (listFragment != null) {
            listFragment.refreshList();
        }
        
        // In tablet mode, show the updated crime in detail pane
        if (isTwoPane) {
            CrimeFragment crimeFragment = CrimeFragment.newInstance(crime.getId());
            showCrimeDetail(crimeFragment);
        }
    }

    @Override
    public void onCrimeDeleted(Crime crime) {
        // Refresh the list fragment
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (listFragment != null) {
            listFragment.refreshList();
        }
        
        // In tablet mode, clear the detail pane or show first crime
        if (isTwoPane) {
            java.util.List<Crime> crimes = CrimeRepository.get().getCrimes();
            if (!crimes.isEmpty()) {
                CrimeFragment crimeFragment = CrimeFragment.newInstance(crimes.get(0).getId());
                showCrimeDetail(crimeFragment);
            }
        }
    }
}
