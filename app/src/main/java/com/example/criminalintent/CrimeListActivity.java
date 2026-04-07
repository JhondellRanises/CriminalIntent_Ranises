package com.example.criminalintent;

import android.os.Bundle;
<<<<<<< HEAD
=======
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
>>>>>>> c126cc4 (Finish the Chapter 16, 17 and 18)
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.List;

public class CrimeListActivity extends AppCompatActivity implements CrimeFragment.Callbacks {
    private boolean isTwoPane;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
<<<<<<< HEAD
        // Set status bar icons to white
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(0);
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
=======
        // Set status bar to be transparent and icons to white
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        // Check if we're in two-pane mode (tablet)
        View detailFragmentContainer = findViewById(R.id.detail_fragment_container);
        isTwoPane = detailFragmentContainer != null && detailFragmentContainer.getVisibility() == View.VISIBLE;
>>>>>>> c126cc4 (Finish the Chapter 16, 17 and 18)

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CrimeListFragment())
                    .commit();
            
            // For tablets, show welcome fragment initially
            if (isTwoPane) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_fragment_container, new WelcomeFragment())
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only create options menu in tablet mode
        if (isTwoPane) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.fragment_crime_list, menu);
            
            // Hide the language option that matches current locale
            // Use the same method as the language switching
            androidx.core.os.LocaleListCompat currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales();
            String currentLanguage = currentLocales.get(0).getLanguage();
            
            if ("es".equals(currentLanguage)) {
                // Currently in Spanish, hide Spanish option, show English option
                menu.findItem(R.id.action_spanish).setVisible(false);
                menu.findItem(R.id.action_english).setVisible(true);
            } else {
                // Currently in English or other language, hide English option, show Spanish option
                menu.findItem(R.id.action_english).setVisible(false);
                menu.findItem(R.id.action_spanish).setVisible(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_crime) {
            // Create new crime
            CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (listFragment != null) {
                listFragment.createNewCrime();
            }
            return true;
        } else if (item.getItemId() == R.id.show_subtitle) {
            // Show subtitle
            CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (listFragment != null) {
                listFragment.setSubtitleVisible(true);
                listFragment.updateToolbarSubtitle();
            }
            return true;
        } else if (item.getItemId() == R.id.hide_subtitle) {
            // Hide subtitle
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(null);
            }
            CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (listFragment != null) {
                listFragment.setSubtitleVisible(false);
            }
            return true;
        } else if (item.getItemId() == R.id.action_spanish) {
            // Delegate to fragment to switch language
            CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (listFragment != null) {
                listFragment.switchToSpanish();
            }
            return true;
        } else if (item.getItemId() == R.id.action_english) {
            // Delegate to fragment to switch language
            CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (listFragment != null) {
                listFragment.switchToEnglish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        // Refresh the list fragment to remove the deleted crime
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (listFragment != null) {
            listFragment.refreshList();
        }
        
        // In tablet mode, handle the detail view after deletion
        if (isTwoPane) {
            List<Crime> crimes = CrimeRepository.get().getCrimes();
            if (!crimes.isEmpty()) {
                // Select the first available crime after deletion
                Crime nextCrime = crimes.get(0);
                CrimeFragment crimeFragment = CrimeFragment.newInstance(nextCrime.getId());
                showCrimeDetail(crimeFragment);
            } else {
                // Show welcome fragment if no crimes left
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_fragment_container, new WelcomeFragment())
                        .commit();
            }
        }
    }
}
