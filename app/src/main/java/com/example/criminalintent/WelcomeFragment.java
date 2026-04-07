package com.example.criminalintent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        
        TextView welcomeTitle = view.findViewById(R.id.welcome_title);
        TextView welcomeMessage = view.findViewById(R.id.welcome_message);
        
        // Set the welcome text from string resources
        if (welcomeTitle != null) {
            welcomeTitle.setText(R.string.welcome_title);
        }
        if (welcomeMessage != null) {
            welcomeMessage.setText(R.string.welcome_message);
        }
        
        return view;
    }
}
