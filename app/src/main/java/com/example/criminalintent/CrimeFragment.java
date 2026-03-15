package com.example.criminalintent;

import android.Manifest;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String STATE_TITLE = "state_title";
    private static final String STATE_DATE = "state_date";
    private static final String STATE_SOLVED = "state_solved";
    private static final String STATE_REQUIRES_POLICE = "state_requires_police";
    private static final String STATE_SUSPECT = "state_suspect";
    private static final String STATE_SUSPECT_PHONE = "state_suspect_phone";

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
    private Button reportButton;
    private Button suspectButton;
    private Button callSuspectButton;

    private final ActivityResultLauncher<Intent> pickContactLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != android.app.Activity.RESULT_OK || result.getData() == null) {
                    return;
                }
                Uri contactUri = result.getData().getData();
                if (contactUri == null || crime == null || getContext() == null) {
                    return;
                }
                readSuspectFromContact(contactUri);
                updateSuspectUi();
            });

    private final ActivityResultLauncher<String> requestContactPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchContactPicker();
                }
            });

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
            crime.setSuspect(originalCrime.getSuspect());
            crime.setSuspectPhoneNumber(originalCrime.getSuspectPhoneNumber());
        } else {
            isNewCrime = true;
            crime = new Crime(crimeId);
            int crimeCount = CrimeRepository.get().getCrimes().size();
            crime.setTitle(getString(R.string.case_number_title, crimeCount + 1));
        }

        if (savedInstanceState != null && crime != null) {
            crime.setTitle(savedInstanceState.getString(STATE_TITLE, crime.getTitle()));
            Object dateSerializable = savedInstanceState.getSerializable(STATE_DATE);
            if (dateSerializable instanceof Date) {
                crime.setDate((Date) dateSerializable);
            }
            crime.setSolved(savedInstanceState.getBoolean(STATE_SOLVED, crime.isSolved()));
            crime.setRequiresPolice(savedInstanceState.getBoolean(STATE_REQUIRES_POLICE, crime.isRequiresPolice()));
            crime.setSuspect(savedInstanceState.getString(STATE_SUSPECT, crime.getSuspect()));
            crime.setSuspectPhoneNumber(savedInstanceState.getString(STATE_SUSPECT_PHONE, crime.getSuspectPhoneNumber()));
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
        reportButton = view.findViewById(R.id.crime_report);
        suspectButton = view.findViewById(R.id.crime_suspect);
        callSuspectButton = view.findViewById(R.id.crime_call_suspect);

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

        reportButton.setOnClickListener(v -> {
            Intent reportIntent = ShareCompat.IntentBuilder.from(requireActivity())
                    .setType("text/plain")
                    .setSubject(getString(R.string.crime_report_subject))
                    .setText(getCrimeReport())
                    .createChooserIntent();
            startActivity(reportIntent);
        });

        suspectButton.setOnClickListener(v -> {
            if (crime == null) return;

            if (!TextUtils.isEmpty(crime.getSuspect())) {
                showSuspectOptionsDialog();
            } else {
                requestSuspectSelection();
            }
        });

        callSuspectButton.setOnClickListener(v -> {
            if (crime == null || TextUtils.isEmpty(crime.getSuspectPhoneNumber())) return;
            Uri number = Uri.parse("tel:" + crime.getSuspectPhoneNumber());
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, number);
            startActivity(dialIntent);
        });

        updateStatusAndButtonColor();
        updateSuspectUi();
        configureImplicitIntentButtons();

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
                            originalCrime.setSuspect(crime.getSuspect());
                            originalCrime.setSuspectPhoneNumber(crime.getSuspectPhoneNumber());
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (crime == null) return;

        outState.putString(STATE_TITLE, crime.getTitle());
        outState.putSerializable(STATE_DATE, crime.getDate());
        outState.putBoolean(STATE_SOLVED, crime.isSolved());
        outState.putBoolean(STATE_REQUIRES_POLICE, crime.isRequiresPolice());
        outState.putString(STATE_SUSPECT, crime.getSuspect());
        outState.putString(STATE_SUSPECT_PHONE, crime.getSuspectPhoneNumber());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_delete_crime) {
            if (getContext() != null && crime != null) {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle(R.string.delete_crime)
                        .setMessage(R.string.delete_crime_message)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            Crime crimeToDelete = originalCrime;
                            if (crimeToDelete == null) {
                                crimeToDelete = CrimeRepository.get().getCrime(crime.getId());
                            }
                            if (crimeToDelete != null) {
                                CrimeRepository.get().deleteCrime(crimeToDelete);
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

    private void launchContactPicker() {
        Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        try {
            pickContactLauncher.launch(pickContact);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(requireContext(), R.string.no_contacts_app, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasContactPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void configureImplicitIntentButtons() {
        suspectButton.setEnabled(true);
        reportButton.setEnabled(true);
    }

    private void showSuspectOptionsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.suspect_options_title)
                .setItems(new CharSequence[]{
                        getString(R.string.change_suspect),
                        getString(R.string.delete_suspect)
                }, (dialog, which) -> {
                    if (which == 0) {
                        requestSuspectSelection();
                    } else if (which == 1) {
                        clearSuspect();
                    }
                })
                .show();
    }

    private void requestSuspectSelection() {
        if (!hasContactPermission()) {
            requestContactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        } else {
            launchContactPicker();
        }
    }

    private void clearSuspect() {
        if (crime == null) return;
        crime.setSuspect("");
        crime.setSuspectPhoneNumber("");
        updateSuspectUi();
    }

    private void readSuspectFromContact(Uri contactUri) {
        if (getContext() == null || crime == null) return;

        String[] contactProjection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        try (Cursor cursor = requireContext().getContentResolver().query(
                contactUri,
                contactProjection,
                null,
                null,
                null
        )) {
            if (cursor == null || !cursor.moveToFirst()) return;

            long contactId = cursor.getLong(0);
            String suspectName = cursor.getString(1);

            crime.setSuspect(suspectName != null ? suspectName : "");
            crime.setSuspectPhoneNumber(findPhoneNumberForContact(contactId));
        }
    }

    private String findPhoneNumberForContact(long contactId) {
        if (getContext() == null) return "";

        String[] phoneProjection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(contactId)};

        try (Cursor phoneCursor = requireContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                phoneProjection,
                selection,
                selectionArgs,
                null
        )) {
            if (phoneCursor == null || !phoneCursor.moveToFirst()) {
                return "";
            }
            String phoneNumber = phoneCursor.getString(0);
            return phoneNumber != null ? phoneNumber : "";
        }
    }

    private void updateSuspectUi() {
        if (crime == null) return;

        if (TextUtils.isEmpty(crime.getSuspect())) {
            suspectButton.setText(R.string.choose_suspect);
        } else {
            suspectButton.setText(getString(R.string.suspect_button_text, crime.getSuspect()));
        }

        boolean hasPhone = !TextUtils.isEmpty(crime.getSuspectPhoneNumber());
        callSuspectButton.setEnabled(hasPhone);
    }

    private String getCrimeReport() {
        if (crime == null) return "";

        String solvedString = crime.isSolved()
                ? getString(R.string.crime_report_solved)
                : getString(R.string.crime_report_unsolved);

        String suspect = TextUtils.isEmpty(crime.getSuspect())
                ? getString(R.string.crime_report_no_suspect)
                : crime.getSuspect();

        String date = new SimpleDateFormat("EEE, MMM dd yyyy, hh:mm a", Locale.getDefault())
                .format(crime.getDate());

        String title = TextUtils.isEmpty(crime.getTitle())
                ? getString(R.string.crime_title_fallback)
                : crime.getTitle();

        return getString(R.string.crime_report, title, date, solvedString, suspect);
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
