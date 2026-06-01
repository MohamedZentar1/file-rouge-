package edu.polytech.filrouge_tp5.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import edu.polytech.filrouge_tp5.Notifiable;
import edu.polytech.filrouge_tp5.Notifier;
import edu.polytech.filrouge_tp5.ProfilePrefs;
import edu.polytech.filrouge_tp5.R;

public class Screen7Fragment extends Fragment {
    public final static int FRAGMENT_ID = 6;
    private final String TAG = "frallo " + getClass().getSimpleName();

    private Notifiable notifiable;
    private ProfilePrefs prefs;

    private ShapeableImageView profilePhoto;
    private TextView summaryName, summaryPoste, authStatus;
    private EditText editName, editPoste;
    private SwitchMaterial switchNotif, switchAnon, switchMapVis;
    private Button btnChangePhoto, btnSaveProfile, btnAuth;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    saveImageFromUri(uri);
                }
            });

    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    saveImageFromBitmap(bitmap);
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    cameraLauncher.launch(null);
                } else {
                    Toast.makeText(requireContext(), "Permission camera refusee", Toast.LENGTH_SHORT).show();
                }
            });

    public Screen7Fragment() {
        Log.d(TAG, "screenFragment type 7 created"); // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        notifiable.onFragmentDisplayed(FRAGMENT_ID);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (requireActivity() instanceof Notifiable) {
            notifiable = (Notifiable) requireActivity();
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en oeuvre Notifiable.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen7, container, false);
        prefs = new ProfilePrefs(requireContext());

        profilePhoto = view.findViewById(R.id.profilePhoto);
        summaryName = view.findViewById(R.id.summaryName);
        summaryPoste = view.findViewById(R.id.summaryPoste);
        authStatus = view.findViewById(R.id.authStatus);
        editName = view.findViewById(R.id.editName);
        editPoste = view.findViewById(R.id.editPoste);
        switchNotif = view.findViewById(R.id.switchNotif);
        switchAnon = view.findViewById(R.id.switchAnon);
        switchMapVis = view.findViewById(R.id.switchMapVis);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnAuth = view.findViewById(R.id.btnAuth);

        loadStateIntoViews();

        btnChangePhoto.setOnClickListener(v -> showPhotoChooser());

        btnSaveProfile.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String poste = editPoste.getText().toString().trim();
            if (name.isEmpty()) {
                name = "Utilisateur SignalRoute";
            }
            prefs.saveProfile(name, poste);
            summaryName.setText(name);
            summaryPoste.setText(poste);
            Toast.makeText(requireContext(), "Profil enregistre", Toast.LENGTH_SHORT).show();
        });

        btnAuth.setOnClickListener(v -> {
            prefs.setLoggedIn(!prefs.isLoggedIn());
            refreshAuthUi();
        });

        switchNotif.setOnCheckedChangeListener((b, checked) -> prefs.setNotifEnabled(checked));
        switchAnon.setOnCheckedChangeListener((b, checked) -> prefs.setAnonymous(checked));
        switchMapVis.setOnCheckedChangeListener((b, checked) -> prefs.setMapVisible(checked));

        view.findViewById(R.id.btnTestNotif).setOnClickListener(v -> {
            boolean shown = Notifier.show(requireContext(),
                    "SignalRoute",
                    "Ceci est une notification de test. Touchez pour ouvrir l'application.");
            if (!shown) {
                Toast.makeText(requireContext(),
                        "Notifications desactivees ou non autorisees.",
                        Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private void loadStateIntoViews() {
        editName.setText(prefs.getName());
        editPoste.setText(prefs.getPoste());
        summaryName.setText(prefs.getName());
        summaryPoste.setText(prefs.getPoste());
        switchNotif.setChecked(prefs.isNotifEnabled());
        switchAnon.setChecked(prefs.isAnonymous());
        switchMapVis.setChecked(prefs.isMapVisible());
        loadPhoto(prefs.getPhotoPath());
        refreshAuthUi();
    }

    private void refreshAuthUi() {
        boolean logged = prefs.isLoggedIn();
        authStatus.setText(logged ? ("Connecte : " + prefs.getName()) : "Non connecte");
        btnAuth.setText(logged ? "Se deconnecter" : "Se connecter");

        editName.setEnabled(logged);
        editPoste.setEnabled(logged);
        btnChangePhoto.setEnabled(logged);
        btnSaveProfile.setEnabled(logged);
        switchNotif.setEnabled(logged);
        switchAnon.setEnabled(logged);
        switchMapVis.setEnabled(logged);

        if (logged) {
            summaryName.setText(prefs.getName());
            summaryPoste.setText(prefs.getPoste());
        } else {
            summaryName.setText("Invite");
            summaryPoste.setText("Non connecte");
        }
    }

    private void showPhotoChooser() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Photo de profil")
                .setItems(new String[]{"Galerie", "Appareil photo"}, (dialog, which) -> {
                    if (which == 0) {
                        galleryLauncher.launch("image/*");
                    } else {
                        launchCamera();
                    }
                })
                .show();
    }

    private void launchCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null);
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private File profileFile() {
        return new File(requireContext().getFilesDir(), "profile_photo.jpg");
    }

    private void saveImageFromUri(Uri uri) {
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(profileFile())) {
            if (in == null) {
                return;
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            onPhotoSaved();
        } catch (Exception e) {
            Log.e(TAG, "Erreur copie image galerie", e);
            Toast.makeText(requireContext(), "Impossible de charger l'image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageFromBitmap(Bitmap bitmap) {
        try (OutputStream out = new FileOutputStream(profileFile())) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            onPhotoSaved();
        } catch (Exception e) {
            Log.e(TAG, "Erreur sauvegarde photo", e);
            Toast.makeText(requireContext(), "Impossible d'enregistrer la photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPhotoSaved() {
        prefs.setPhotoPath(profileFile().getAbsolutePath());
        loadPhoto(prefs.getPhotoPath());
        Toast.makeText(requireContext(), "Photo mise a jour", Toast.LENGTH_SHORT).show();
    }

    private void loadPhoto(String path) {
        if (path != null && new File(path).exists()) {
            profilePhoto.setImageTintList(null);
            profilePhoto.setPadding(0, 0, 0, 0);
            Picasso.get()
                    .load(new File(path))
                    .placeholder(R.drawable.ic_users)
                    .error(R.drawable.ic_users)
                    .into(profilePhoto);
        } else {
            profilePhoto.setImageResource(R.drawable.ic_users);
        }
    }
}
