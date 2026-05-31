package edu.polytech.filrouge_tp5;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    private final String TAG = "frallo " + getClass().getSimpleName();
    private final int[] menuIcons = {
            R.drawable.ic_map,           // 0 -> Carte
            R.drawable.ic_list,          // 1 -> Liste des incidents
            R.drawable.ic_alert,         // 2 -> Signaler
            R.drawable.ic_mic,           // 3 -> Alerte vocale
            R.drawable.ic_notifications, // 4 -> Suivi des alertes
            R.drawable.ic_settings       // 5 -> Parametres
    };

    private Menuable menuable;
    private int currentActivatedIndex = 0;
    private View layout;

    public MenuFragment() {
    }

    public void setCurrentActivatedIndex(int index) {
        Log.d(TAG, "setCurrentActivatedIndex updated to " + index + " (currentActivatedIndex = " + currentActivatedIndex + ")");
        if (layout == null) {
            currentActivatedIndex = index;
            return;
        }
        List<ImageView> imageViews = findPicturesMenuFromId(layout.findViewById(R.id.itemsMenu));
        if (index < 0 || index >= imageViews.size()) {
            return;
        }
        currentActivatedIndex = index;
        updateMenuVisualState(imageViews);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_menu, container, false);
        List<ImageView> imageViews = findPicturesMenuFromId(layout.findViewById(R.id.itemsMenu));

        if (getArguments() != null) {
            currentActivatedIndex = getArguments().getInt(getString(R.string.index), 0);
        }
        if (currentActivatedIndex < 0 || currentActivatedIndex >= imageViews.size()) {
            currentActivatedIndex = 0;
        }

        TextView text = layout.findViewById(R.id.txtFragmentMenu);
        text.setText("SignalRoute");
        updateMenuVisualState(imageViews);

        for (ImageView imageView : imageViews) {
            imageView.setOnClickListener(menu -> {
                int nextIndex = Integer.parseInt(imageView.getTag().toString());
                if (nextIndex < 0 || nextIndex >= imageViews.size()) {
                    return;
                }
                currentActivatedIndex = nextIndex;
                updateMenuVisualState(imageViews);
                menuable.onMenuChange(currentActivatedIndex);
            });
        }
        return layout;
    }

    private void updateMenuVisualState(List<ImageView> imageViews) {
        int primary = getResources().getColor(R.color.primary);
        int subtle = getResources().getColor(R.color.icon_default);
        for (int i = 0; i < imageViews.size(); i++) {
            ImageView imageView = imageViews.get(i);
            imageView.setImageResource(menuIcons[Math.min(i, menuIcons.length - 1)]);
            imageView.setColorFilter(i == currentActivatedIndex ? primary : subtle);
            imageView.setBackgroundColor(i == currentActivatedIndex ? getResources().getColor(R.color.primary_soft) : Color.TRANSPARENT);
        }
    }

    private List<ImageView> findPicturesMenuFromId(View view) {
        List<ImageView> pictures = new ArrayList<>();
        collectMenuPictures(view, pictures);
        return pictures;
    }

    private void collectMenuPictures(View view, List<ImageView> pictures) {
        if (view == null) {
            return;
        }
        if (view instanceof ImageView) {
            String idString = getResources().getResourceEntryName(view.getId());
            if (idString.matches("menu[1-9]?")) {
                pictures.add((ImageView) view);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                collectMenuPictures(viewGroup.getChildAt(i), pictures);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (requireActivity() instanceof Menuable) {
            menuable = (Menuable) requireActivity();
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en oeuvre Menuable.");
        }
    }
}
