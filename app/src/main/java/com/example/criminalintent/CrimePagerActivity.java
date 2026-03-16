package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {

    public static final String EXTRA_CRIME_ID = "com.example.criminalintent.crime_id";
    private static final String STATE_CURRENT_INDEX = "state_current_index";

    private ViewPager2 viewPager;
    private Button firstButton;
    private Button lastButton;
    private TextView pageIndicator;
    private List<Crime> crimes;

    public static Intent newIntent(Context context, UUID crimeId) {
        Intent intent = new Intent(context, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);
        
        // Set status bar icons to white
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(0);
        window.setStatusBarColor(getResources().getColor(android.R.color.transparent));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.crime_datails);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        crimes = CrimeRepository.get().getCrimes();
        viewPager = findViewById(R.id.crime_view_pager);
        firstButton = findViewById(R.id.button_first);
        lastButton = findViewById(R.id.button_last);
        pageIndicator = findViewById(R.id.page_indicator);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return CrimeFragment.newInstance(crimes.get(position).getId());
            }

            @Override
            public int getItemCount() {
                return crimes.size();
            }
        });

        int startIndex = resolveStartIndex(savedInstanceState);
        viewPager.setCurrentItem(startIndex, false);
        updateNavigationUI(startIndex);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateNavigationUI(position);
            }
        });

        firstButton.setOnClickListener(v -> viewPager.setCurrentItem(0, false));
        lastButton.setOnClickListener(v -> {
            int lastIndex = Math.max(0, crimes.size() - 1);
            viewPager.setCurrentItem(lastIndex, false);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_INDEX, viewPager.getCurrentItem());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateNavigationUI(int position) {
        int lastIndex = Math.max(0, crimes.size() - 1);
        firstButton.setEnabled(position > 0);
        lastButton.setEnabled(position < lastIndex);

        if (pageIndicator != null) {
            pageIndicator.setText(getString(R.string.case_position, position + 1, Math.max(1, crimes.size())));
        }
    }

    private int resolveStartIndex(@Nullable Bundle savedInstanceState) {
        int lastIndex = Math.max(0, crimes.size() - 1);
        if (savedInstanceState != null) {
            int restored = savedInstanceState.getInt(STATE_CURRENT_INDEX, 0);
            return Math.max(0, Math.min(restored, lastIndex));
        }

        UUID crimeId = null;
        if (getIntent() != null) {
            Object extra = getIntent().getSerializableExtra(EXTRA_CRIME_ID);
            if (extra instanceof UUID) {
                crimeId = (UUID) extra;
            }
        }

        if (crimeId != null) {
            for (int i = 0; i < crimes.size(); i++) {
                if (crimeId.equals(crimes.get(i).getId())) {
                    return i;
                }
            }
        }
        return 0;
    }
}

