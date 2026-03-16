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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String STATE_TITLE = "state_title";
    private static final String STATE_DATE = "state_date";
    private static final String STATE_SOLVED = "state_solved";
    private static final String STATE_REQUIRES_POLICE = "state_requires_police";
    private static final String STATE_SUSPECT = "state_suspect";
    private static final String STATE_SUSPECT_PHONE = "state_suspect_phone";

    // Static list to persist manually added contacts across dialog sessions
    private static final java.util.List<Contact> manuallyAddedContacts = new java.util.ArrayList<>();

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
            showContactSelectionDialog();
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

    private void showContactSelectionDialog() {
        if (getContext() == null) return;

        // Load contacts from device
        java.util.List<Contact> contacts = loadContactsFromDevice();

        // Add the current crime's suspect to the list if available and not already in manually added contacts
        if (crime != null && crime.getSuspect() != null && !crime.getSuspect().trim().isEmpty()) {
            String suspectName = crime.getSuspect().trim();
            String suspectPhone = crime.getSuspectPhoneNumber() != null ? crime.getSuspectPhoneNumber().trim() : "";
            
            // Check if suspect is already in the contacts list (including manually added)
            boolean found = false;
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).getName().equals(suspectName)) {
                    // Update the phone number if it's different
                    if (!contacts.get(i).getPhoneNumber().equals(suspectPhone)) {
                        contacts.set(i, new Contact(suspectName, suspectPhone.isEmpty() ? "No phone number" : suspectPhone));
                    }
                    found = true;
                    break;
                }
            }
            
            // Add to list if not found
            if (!found) {
                contacts.add(0, new Contact(suspectName, suspectPhone.isEmpty() ? "No phone number" : suspectPhone));
            }
        }

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_contact_selection, null);
        
        RecyclerView recyclerView = dialogView.findViewById(R.id.contacts_recycler_view);
        TextView noContactsText = dialogView.findViewById(R.id.no_contacts_text);
        Button addNewContactButton = dialogView.findViewById(R.id.add_new_contact_button);

        // Create the dialog first so we can reference it in the adapter
        builder.setView(dialogView);
        builder.setTitle(R.string.select_suspect);
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();

        if (contacts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noContactsText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noContactsText.setVisibility(View.GONE);
            
            ContactAdapter adapter = new ContactAdapter(contacts, contact -> {
                if (crime != null) {
                    crime.setSuspect(contact.getName());
                    crime.setSuspectPhoneNumber(contact.getPhoneNumber());
                    updateSuspectUi();
                }
                // Close the dialog after selection
                dialog.dismiss();
            });
            
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }

        addNewContactButton.setOnClickListener(v -> {
            showAddContactDialog(dialog);
        });

        dialog.show();
    }

    private void showAddContactDialog(AlertDialog parentDialog) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_contact, null);
        
        android.widget.EditText nameInput = dialogView.findViewById(R.id.contact_name_input);
        android.widget.EditText phoneInput = dialogView.findViewById(R.id.phone_number_input);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button addButton = dialogView.findViewById(R.id.add_button);

        AlertDialog dialog = builder.setView(dialogView).create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        addButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(name)) {
                nameInput.setError("Name required");
                return;
            }
            
            // Phone number is optional for adding a contact
            if (TextUtils.isEmpty(phone)) {
                phone = "No phone number";
            }
            
            // Create the new contact
            Contact newContact = new Contact(name, phone);
            
            // Add to manually added contacts list (avoid duplicates)
            boolean found = false;
            for (Contact existingContact : manuallyAddedContacts) {
                if (existingContact.getName().equals(name)) {
                    // Update existing contact
                    existingContact.setPhoneNumber(phone);
                    found = true;
                    break;
                }
            }
            if (!found) {
                manuallyAddedContacts.add(newContact);
            }
            
            if (crime != null) {
                crime.setSuspect(name);
                crime.setSuspectPhoneNumber(phone);
                updateSuspectUi();
            }
            
            dialog.dismiss();
            parentDialog.dismiss();
            // Refresh the contact selection dialog to show the new contact
            showContactSelectionDialog();
        });

        dialog.show();
    }

    private java.util.List<Contact> loadContactsFromDevice() {
        java.util.List<Contact> contacts = new java.util.ArrayList<>();
        
        if (getContext() == null) return contacts;

        // Always add default contacts first
        contacts.add(new Contact("Juan Dela Cruz", "+63917-123-4567"));
        contacts.add(new Contact("Maria Santos", "+63918-234-5678"));
        contacts.add(new Contact("Jose Reyes", "+63919-345-6789"));
        contacts.add(new Contact("Ana Garcia", "+63920-456-7890"));
        contacts.add(new Contact("Carlos Rodriguez", "+63921-567-8901"));

        // Try to load real device contacts and add them (avoid duplicates with defaults)
        String[] projection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        try (Cursor cursor = getContext().getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long contactId = cursor.getLong(0);
                    String name = cursor.getString(1);
                    
                    if (name == null) name = "";
                    
                    // Check if already in list (to avoid duplicates with defaults)
                    boolean alreadyExists = false;
                    for (Contact existingContact : contacts) {
                        if (existingContact.getName().equals(name)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    
                    if (!alreadyExists) {
                        String phoneNumber = findPhoneNumberForContact(contactId);
                        if (TextUtils.isEmpty(phoneNumber)) {
                            phoneNumber = "No phone number";
                        }
                        contacts.add(new Contact(name, phoneNumber));
                    }
                }
            }
        } catch (Exception e) {
            // Handle permission errors gracefully
        }

        // Add manually added contacts (avoid duplicates)
        for (Contact manualContact : manuallyAddedContacts) {
            boolean found = false;
            for (Contact existingContact : contacts) {
                if (existingContact.getName().equals(manualContact.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                contacts.add(manualContact);
            }
        }
        
        return contacts;
    }
}
