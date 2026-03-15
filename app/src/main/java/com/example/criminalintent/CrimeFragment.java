package com.example.criminalintent;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
    private Crime originalCrime;
    private boolean isNewCrime;

    private EditText titleField;
    private Button dateButton;
    private CheckBox solvedCheckBox;
    private TextView statusTextView;
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

        originalCrime = CrimeRepository.get().getCrime(crimeId);
        if (originalCrime != null) {
            isNewCrime = false;
            crime = new Crime(originalCrime.getId());
            crime.setTitle(originalCrime.getTitle());
            crime.setDate(originalCrime.getDate());
            crime.setSolved(originalCrime.isSolved());
            crime.setRequiresPolice(originalCrime.isRequiresPolice());
        } else {
            isNewCrime = true;
            crime = new Crime(crimeId);
            int crimeCount = CrimeRepository.get().getCrimes().size();
            crime.setTitle(getString(R.string.case_number_title, crimeCount + 1));
        }

        getParentFragmentManager().setFragmentResultListener(
                DatePickerFragment.REQUEST_KEY_DATE,
                this,
                (requestKey, result) -> {
                    Object serializable = result.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE);
                    if (serializable instanceof Date && crime != null) {
                        crime.setDate((Date) serializable);
                        updateDateTime();
                    }
                }
        );

        getParentFragmentManager().setFragmentResultListener(
                TimePickerFragment.REQUEST_KEY_TIME,
                this,
                (requestKey, result) -> {
                    Object serializable = result.getSerializable(TimePickerFragment.BUNDLE_KEY_TIME);
                    if (serializable instanceof Date && crime != null) {
                        crime.setDate((Date) serializable);
                        updateDateTime();
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
        statusTextView = view.findViewById(R.id.crime_status);
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

        updateDateTime();
        dateButton.setEnabled(true);
        dateButton.setOnClickListener(v -> {
            if (crime == null) return;
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.change_date_or_time);
            builder.setItems(new CharSequence[]{getString(R.string.change_date), getString(R.string.change_time)}, (dialog, which) -> {
                if (which == 0) {
                    DatePickerFragment dateDialog = DatePickerFragment.newInstance(crime.getDate());
                    dateDialog.show(getParentFragmentManager(), "DatePickerFragment");
                } else if (which == 1) {
                    TimePickerFragment timeDialog = TimePickerFragment.newInstance(crime.getDate());
                    timeDialog.show(getParentFragmentManager(), "TimePickerFragment");
                }
            });
            builder.show();
        });

        solvedCheckBox.setChecked(crime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (crime != null) {
                crime.setSolved(isChecked);
                updateStatusAndButtonColor();
            }
        });

        updateStatusAndButtonColor();

        saveButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(R.string.save_case_title)
                    .setMessage(R.string.save_case_confirm_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.save, (dialog, which) -> {
                        if (isNewCrime) {
                            CrimeRepository.get().addCrime(crime);
                        } else if (originalCrime != null) {
                            originalCrime.setTitle(crime.getTitle());
                            originalCrime.setDate(crime.getDate());
                            originalCrime.setSolved(crime.isSolved());
                            originalCrime.setRequiresPolice(crime.isRequiresPolice());
                        }
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    })
                    .show();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_delete_crime) {
            if (getContext() != null && crime != null) {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle(R.string.delete_crime)
                    .setMessage(R.string.delete_crime_message)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        if (!isNewCrime) {
                            CrimeRepository.get().deleteCrime(originalCrime);
                        }
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateDateTime() {
        if (crime == null) return;
        if (dateButton != null) {
            SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("EEE MMM dd yyyy, hh:mm a", Locale.getDefault());
            dateButton.setText(dateTimeFormatter.format(crime.getDate()));
        }
    }

    private void updateStatusAndButtonColor() {
        if (crime == null || statusTextView == null || saveButton == null) return;

        if (crime.isSolved()) {
            statusTextView.setText(R.string.case_closed);
            statusTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_closed));
            statusTextView.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.status_closed_container)
            ));
            saveButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.status_closed)
            ));
        } else {
            statusTextView.setText(R.string.case_open);
            statusTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_open));
            statusTextView.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.status_open_container)
            ));
            saveButton.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.brand_primary)
            ));
        }
        saveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }
}
