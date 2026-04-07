package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.color.MaterialColors;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CrimeListFragment extends Fragment {

    private static final int MAX_CRIMES_LIMIT = 10;

    private RecyclerView recyclerView;
    private CrimeAdapter adapter;
    private FloatingActionButton fabAddCrime;
    private View emptyView;
    private Button emptyAddButton;
    private boolean subtitleVisible = true;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_crime_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if we're in two-pane mode
        CrimeListActivity mainActivity = (CrimeListActivity) getActivity();
        boolean isTwoPane = mainActivity != null && mainActivity.isTwoPane();

        // Only set up toolbar if not in tablet mode
        if (!isTwoPane) {
            Toolbar toolbar = view.findViewById(R.id.toolbar);
            AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
            if (appCompatActivity != null) {
                appCompatActivity.setSupportActionBar(toolbar);
                if (appCompatActivity.getSupportActionBar() != null) {
                    appCompatActivity.getSupportActionBar().setTitle(R.string.app_name);
                }
            }
        }

        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.crime_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);
        emptyAddButton = view.findViewById(R.id.empty_add_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CrimeAdapter(CrimeRepository.get().getCrimes(), crime -> {
            if (getContext() == null) return;
            
            if (isTwoPane) {
                // Show detail fragment in right pane
                CrimeFragment crimeFragment = CrimeFragment.newInstance(crime.getId());
                if (mainActivity != null) {
                    mainActivity.showCrimeDetail(crimeFragment);
                }
            } else {
                // Start new activity for phone
                Intent intent = CrimePagerActivity.newIntent(getContext(), crime.getId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
        attachSwipeToDismiss();

        fabAddCrime = view.findViewById(R.id.fab_add_crime);
        fabAddCrime.setOnClickListener(v -> createNewCrime());
        emptyAddButton.setOnClickListener(v -> createNewCrime());

        // Hide FAB in tablet mode
        if (isTwoPane) {
            fabAddCrime.setVisibility(View.GONE);
        }

        updateListUiState();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Only create options menu if not in tablet mode
        CrimeListActivity activity = (CrimeListActivity) getActivity();
        boolean isTwoPane = activity != null && activity.isTwoPane();
        
        if (!isTwoPane) {
            super.onCreateOptionsMenu(menu, inflater);
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
<<<<<<< HEAD
        if (item.getItemId() == R.id.show_subtitle) {
=======
        if (item.getItemId() == R.id.add_crime) {
            createNewCrime();
            return true;
        } else if (item.getItemId() == R.id.show_subtitle) {
>>>>>>> c126cc4 (Finish the Chapter 16, 17 and 18)
            subtitleVisible = true;
            updateToolbarSubtitle();
            return true;
        } else if (item.getItemId() == R.id.hide_subtitle) {
            subtitleVisible = false;
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null && activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setSubtitle(null);
            }
            return true;
<<<<<<< HEAD
=======
        } else if (item.getItemId() == R.id.action_spanish) {
            switchToSpanish();
            return true;
        } else if (item.getItemId() == R.id.action_english) {
            switchToEnglish();
            return true;
>>>>>>> c126cc4 (Finish the Chapter 16, 17 and 18)
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateListUiState();
    }

<<<<<<< HEAD
    private void createNewCrime() {
        List<Crime> crimes = CrimeRepository.get().getCrimes();
        if (crimes.size() >= MAX_CRIMES_LIMIT) {
            // Show message that limit has been reached
            if (getContext() != null) {
                Toast.makeText(getContext(), "Maximum limit of " + MAX_CRIMES_LIMIT + " crimes reached", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        UUID id = UUID.randomUUID();
        if (getContext() != null) {
            startActivity(CrimeActivity.newIntent(getContext(), id));
        }
    }

    private void updateListUiState() {
        List<Crime> crimes = CrimeRepository.get().getCrimes();
        boolean hasCrimes = !crimes.isEmpty();
        boolean canAddMore = crimes.size() < MAX_CRIMES_LIMIT;
        
        recyclerView.setVisibility(hasCrimes ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(hasCrimes ? View.GONE : View.VISIBLE);
        
        // Hide/show add buttons based on limit
        fabAddCrime.setVisibility(canAddMore ? View.VISIBLE : View.GONE);
        emptyAddButton.setVisibility(canAddMore ? View.VISIBLE : View.GONE);
        
        updateToolbarSubtitle();
    }

    private void updateToolbarSubtitle() {
=======
    public void setSubtitleVisible(boolean visible) {
        subtitleVisible = visible;
    }

    public void updateToolbarSubtitle() {
>>>>>>> c126cc4 (Finish the Chapter 16, 17 and 18)
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null || activity.getSupportActionBar() == null || !subtitleVisible) return;

        List<Crime> crimes = CrimeRepository.get().getCrimes();
        int total = crimes.size();
        int solved = 0;
        for (Crime c : crimes) {
            if (c.isSolved()) solved++;
        }
        int open = total - solved;
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, total, total, open, solved);
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    public void createNewCrime() {
        List<Crime> crimes = CrimeRepository.get().getCrimes();
        if (crimes.size() >= MAX_CRIMES_LIMIT) {
            // Show message that limit has been reached
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.max_crimes_limit, MAX_CRIMES_LIMIT), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        UUID id = UUID.randomUUID();
        CrimeListActivity mainActivity = (CrimeListActivity) getActivity();
        boolean isTwoPane = mainActivity != null && mainActivity.isTwoPane();
        
        if (isTwoPane) {
            // Show new crime in detail pane
            CrimeFragment crimeFragment = CrimeFragment.newInstance(id);
            if (mainActivity != null) {
                mainActivity.showCrimeDetail(crimeFragment);
            }
        } else {
            // Start new activity for phone
            if (getContext() != null) {
                startActivity(CrimeActivity.newIntent(getContext(), id));
            }
        }
    }

    private void updateListUiState() {
        List<Crime> crimes = CrimeRepository.get().getCrimes();
        boolean hasCrimes = !crimes.isEmpty();
        boolean canAddMore = crimes.size() < MAX_CRIMES_LIMIT;
        
        recyclerView.setVisibility(hasCrimes ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(hasCrimes ? View.GONE : View.VISIBLE);
        
        // Check if we're in tablet mode
        CrimeListActivity activity = (CrimeListActivity) getActivity();
        boolean isTwoPane = activity != null && activity.isTwoPane();
        
        // Hide/show add buttons based on limit and tablet mode
        if (!isTwoPane) {
            // Only show FAB in phone mode
            fabAddCrime.setVisibility(canAddMore ? View.VISIBLE : View.GONE);
        }
        emptyAddButton.setVisibility(canAddMore ? View.VISIBLE : View.GONE);
        
        updateToolbarSubtitle();

        // In tablet mode, if list is empty, show welcome fragment
        if (isTwoPane && (!hasCrimes)) {
            if (activity != null) {
                activity.showCrimeDetail(new WelcomeFragment());
            }
        }
    }

    public void switchToSpanish() {
        try {
            // App-wide locale change (does not require system settings)
            androidx.core.os.LocaleListCompat spanish = androidx.core.os.LocaleListCompat.forLanguageTags("es");
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(spanish);
            
            // Show toast first
            Toast.makeText(requireContext(), R.string.language_changed_to_spanish, Toast.LENGTH_SHORT).show();
            
            // Delay recreation to ensure locale is applied
            if (getActivity() != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                }, 100);
            }
        } catch (Exception e) {
            // Fallback method if the above fails
            if (getContext() != null) {
                java.util.Locale locale = new java.util.Locale("es");
                java.util.Locale.setDefault(locale);
                android.content.res.Configuration config = new android.content.res.Configuration();
                config.setLocale(locale);
                getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());
                
                Toast.makeText(getContext(), R.string.language_changed_to_spanish, Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
        }
    }

    public void switchToEnglish() {
        try {
            // App-wide locale change (does not require system settings)
            androidx.core.os.LocaleListCompat english = androidx.core.os.LocaleListCompat.forLanguageTags("en");
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(english);
            
            // Show toast first
            Toast.makeText(requireContext(), R.string.language_changed_to_english, Toast.LENGTH_SHORT).show();
            
            // Delay recreation to ensure locale is applied
            if (getActivity() != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                }, 100);
            }
        } catch (Exception e) {
            // Fallback method if the above fails
            if (getContext() != null) {
                java.util.Locale locale = new java.util.Locale("en");
                java.util.Locale.setDefault(locale);
                android.content.res.Configuration config = new android.content.res.Configuration();
                config.setLocale(locale);
                getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());
                
                Toast.makeText(getContext(), R.string.language_changed_to_english, Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
        }
    }

    public void invalidateOptionsMenu() {
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private void attachSwipeToDismiss() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // we only support swipe
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    adapter.notifyDataSetChanged();
                    return;
                }
                List<Crime> crimes = CrimeRepository.get().getCrimes();
                if (position >= crimes.size()) {
                    adapter.notifyDataSetChanged();
                    return;
                }
                Crime crime = crimes.get(position);
                CrimeRepository.get().deleteCrime(crime);
                adapter.notifyItemRemoved(position);
                
                // Update UI state first
                updateListUiState();
                
                // Handle tablet view after deletion with a small delay
                CrimeListActivity activity = (CrimeListActivity) getActivity();
                if (activity != null && activity.isTwoPane()) {
                    // Post delayed to ensure UI is updated first
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        List<Crime> updatedCrimes = CrimeRepository.get().getCrimes();
                        if (!updatedCrimes.isEmpty()) {
                            // Select the next available crime, or the previous one if deleted was last
                            int newPosition = Math.min(position, updatedCrimes.size() - 1);
                            if (newPosition >= 0 && activity != null) {
                                Crime nextCrime = updatedCrimes.get(newPosition);
                                CrimeFragment crimeFragment = CrimeFragment.newInstance(nextCrime.getId());
                                activity.showCrimeDetail(crimeFragment);
                            }
                        }
                    }, 100); // Small delay to ensure UI updates
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private static class CrimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_NORMAL = 0;
        private static final int VIEW_TYPE_POLICE = 1;

        private final List<Crime> crimes;
        private final OnCrimeClickListener onCrimeClick;

        interface OnCrimeClickListener {
            void onCrimeClick(Crime crime);
        }

        CrimeAdapter(List<Crime> crimes, OnCrimeClickListener onCrimeClick) {
            this.crimes = crimes;
            this.onCrimeClick = onCrimeClick;
        }

        @Override
        public int getItemViewType(int position) {
            Crime crime = crimes.get(position);
            return crime.isSolved() ? VIEW_TYPE_NORMAL : VIEW_TYPE_POLICE;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_TYPE_POLICE) {
                View view = inflater.inflate(R.layout.list_item_crime_police, parent, false);
                return new PoliceCrimeHolder(view);
            }
            View view = inflater.inflate(R.layout.list_item_crime, parent, false);
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Crime crime = crimes.get(position);
            if (holder instanceof PoliceCrimeHolder) {
                ((PoliceCrimeHolder) holder).bind(crime, onCrimeClick);
            } else if (holder instanceof CrimeHolder) {
                ((CrimeHolder) holder).bind(crime, onCrimeClick);
            }
        }

        @Override
        public int getItemCount() {
            return crimes.size();
        }

        static class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView titleTextView;
            private final TextView dateTextView;
            private final ImageView solvedIcon;
            private Crime crime;
            private OnCrimeClickListener onCrimeClick;

            CrimeHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.crime_title);
                dateTextView = itemView.findViewById(R.id.crime_date);
                solvedIcon = itemView.findViewById(R.id.crime_solved_icon);
                itemView.setOnClickListener(this);
            }

            void bind(Crime crime, OnCrimeClickListener onCrimeClick) {
                this.crime = crime;
                this.onCrimeClick = onCrimeClick;

                titleTextView.setText(crime.getTitle());
                if (TextUtils.isEmpty(crime.getTitle())) {
                    titleTextView.setText(R.string.crime_title_fallback);
                }
                java.text.DateFormat dateFormatter = DateFormat.getMediumDateFormat(itemView.getContext());
                dateTextView.setText(dateFormatter.format(crime.getDate()));
                solvedIcon.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);

                if (crime.isSolved()) {
                    titleTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_closed));
                } else {
                    int onSurfaceColor = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface);
                    titleTextView.setTextColor(onSurfaceColor);
                }
            }

            @Override
            public void onClick(View v) {
                if (crime != null && onCrimeClick != null) {
                    onCrimeClick.onCrimeClick(crime);
                }
            }
        }

        static class PoliceCrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView titleTextView;
            private final TextView dateTextView;
            private final ImageView solvedIcon;
            private final Button contactPoliceButton;

            private Crime crime;
            private OnCrimeClickListener onCrimeClick;

            PoliceCrimeHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.crime_title);
                dateTextView = itemView.findViewById(R.id.crime_date);
                solvedIcon = itemView.findViewById(R.id.crime_solved_icon);
                contactPoliceButton = itemView.findViewById(R.id.contact_police_button);

                itemView.setOnClickListener(this);
            }

            void bind(Crime crime, OnCrimeClickListener onCrimeClick) {
                this.crime = crime;
                this.onCrimeClick = onCrimeClick;

                titleTextView.setText(crime.getTitle());
                if (TextUtils.isEmpty(crime.getTitle())) {
                    titleTextView.setText(R.string.crime_title_fallback);
                }
                java.text.DateFormat dateFormatter = DateFormat.getMediumDateFormat(itemView.getContext());
                dateTextView.setText(dateFormatter.format(crime.getDate()));
                solvedIcon.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
                contactPoliceButton.setVisibility(crime.isSolved() ? View.GONE : View.VISIBLE);

                if (crime.isSolved()) {
                    titleTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_closed));
                } else {
                    int onSurfaceColor = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurface);
                    titleTextView.setTextColor(onSurfaceColor);
                }

                contactPoliceButton.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.report_crime_title)
                            .setMessage(R.string.report_crime_confirm_message)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.report, (dialog, which) ->
                                    Toast.makeText(v.getContext(), v.getContext().getString(R.string.contact_police_toast), Toast.LENGTH_SHORT).show()
                            )
                            .show();
                });
            }

            @Override
            public void onClick(View v) {
                if (crime != null && onCrimeClick != null) {
                    onCrimeClick.onCrimeClick(crime);
                }
            }
        }
    }
}
