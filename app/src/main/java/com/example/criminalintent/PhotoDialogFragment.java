package com.example.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

public class PhotoDialogFragment extends DialogFragment {

    private static final String ARG_PHOTO_PATH = "arg_photo_path";

    public static PhotoDialogFragment newInstance(File photoFile) {
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_PATH, photoFile != null ? photoFile.getAbsolutePath() : null);

        PhotoDialogFragment fragment = new PhotoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_photo, null);

        PhotoView photoView = view.findViewById(R.id.dialog_photo_view);
        String path = getArguments() != null ? getArguments().getString(ARG_PHOTO_PATH) : null;
        if (path != null) {
            DisplayMetrics metrics = requireContext().getResources().getDisplayMetrics();
            Bitmap bitmap = PictureUtils.getScaledBitmap(path, metrics.widthPixels, metrics.heightPixels);
            if (bitmap != null) {
                photoView.setImageBitmap(bitmap);
            } else {
                photoView.setImageResource(android.R.color.darker_gray);
            }
        }

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .create();
    }
}
