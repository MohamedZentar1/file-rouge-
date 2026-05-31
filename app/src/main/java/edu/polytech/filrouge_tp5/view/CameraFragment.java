package edu.polytech.filrouge_tp5.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import edu.polytech.filrouge_tp5.Picturable;
import edu.polytech.filrouge_tp5.R;

/**
 * Fragment responsible for capturing or displaying a picture for an incident.
 */
public class CameraFragment extends Fragment {
    public static final String REQUEST_KEY = "camera_request";
    public static final String BUNDLE_PATH = "photo_path";

    private final String TAG = "frallo " + getClass().getSimpleName();
    private Picturable callback;
    private String currentPhotoPath;
    private boolean isPictureTaken = false;
    private boolean isFirstLaunchChecked = false;

    private ImageView picturePreview;
    private Uri photoUri;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    handlePermissionDenied();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    isPictureTaken = true;
                    displayImage(currentPhotoPath);
                    if (callback != null) {
                        callback.onPictureTaken(currentPhotoPath);
                    }
                } else {
                    Log.d(TAG, "Capture annulée par l'utilisateur.");
                }
            });

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Picturable) {
            callback = (Picturable) context;
        } else {
            throw new IllegalStateException("L'activité hôte doit implémenter Picturable.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString("path");
            isPictureTaken = savedInstanceState.getBoolean("isTaken");
            isFirstLaunchChecked = savedInstanceState.getBoolean("isFirst");
        }

        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY, this, (requestKey, bundle) -> {
            String path = bundle.getString(BUNDLE_PATH);
            if (path != null && !isPictureTaken) {
                currentPhotoPath = path;
                displayImage(path);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        picturePreview = view.findViewById(R.id.picture_preview);
        view.findViewById(R.id.btn_capture).setOnClickListener(v -> checkPermissionAndLaunch());

        if (currentPhotoPath != null) {
            displayImage(currentPhotoPath);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("path", currentPhotoPath);
        outState.putBoolean("isTaken", isPictureTaken);
        outState.putBoolean("isFirst", isFirstLaunchChecked);
    }

    private void checkPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showRationaleDialog();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
        isFirstLaunchChecked = true;
    }

    private void launchCamera() {
        try {
            File photoFile = createTemporaryFile();
            currentPhotoPath = photoFile.getAbsolutePath();
            photoUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);
            takePictureLauncher.launch(photoUri);
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la création du fichier temporaire", e);
            Toast.makeText(requireContext(), "Impossible de préparer l'appareil photo.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTemporaryFile() throws IOException {
        String fileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = requireContext().getCacheDir();
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    private void displayImage(String path) {
        if (picturePreview != null && path != null) {
            File file = new File(path);
            if (file.exists()) {
                Picasso.get()
                        .load(file)
                        .placeholder(R.drawable.ic_mic)
                        .error(R.drawable.ic_alert)
                        .into(picturePreview);
            }
        }
    }

    private void handlePermissionDenied() {
        if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showDisabledDialog();
        }
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permission requise")
                .setMessage("L'accès à l'appareil photo est nécessaire pour illustrer votre signalement.")
                .setPositiveButton("Réessayer", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.CAMERA))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showDisabledDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Fonctionnalité désactivée")
                .setMessage("La permission a été refusée définitivement. Vous ne pourrez pas prendre de photo pour cet incident.")
                .setPositiveButton("J'ai compris", null)
                .show();
    }
}
