package com.example.criminalintent;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Crime crime;

    private EditText titleField;
    private Button dateButton;
    private CheckBox solvedCheckBox;
    private Button saveButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = null;
        Bundle args = getArguments();
        if (args != null) {
            Object serializable = args.getSerializable(ARG_CRIME_ID);
            if (serializable instanceof UUID) {
                crimeId = (UUID) serializable;
            }
        }
        if (crimeId == null) return;

        crime = CrimeRepository.get().getCrime(crimeId);

        getParentFragmentManager().setFragmentResultListener(
                DatePickerFragment.REQUEST_KEY_DATE,
                this,
                (requestKey, result) -> {
                    Object serializable = result.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE);
                    if (serializable instanceof Date && crime != null) {
                        crime.setDate((Date) serializable);
                        updateDate();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        if (crime == null) return null;
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        titleField = view.findViewById(R.id.crime_title);
        dateButton = view.findViewById(R.id.crime_date);
        solvedCheckBox = view.findViewById(R.id.crime_solved);
        saveButton = view.findViewById(R.id.crime_save);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (crime == null) return;

        titleField.setText(crime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (crime != null) crime.setTitle(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        updateDate();
        dateButton.setEnabled(true);
        dateButton.setOnClickListener(v -> {
            if (crime == null) return;
            DatePickerFragment dialog = DatePickerFragment.newInstance(crime.getDate());
            dialog.show(getParentFragmentManager(), "DatePickerFragment");
        });

        solvedCheckBox.setChecked(crime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (crime != null) crime.setSolved(isChecked);
        });

        saveButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // No menu items to handle - plus icon removed
        return super.onOptionsItemSelected(item);
    }

    private void updateDate() {
        if (dateButton == null || crime == null) return;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy", Locale.getDefault());
        dateButton.setText(formatter.format(crime.getDate()));
    }
}
