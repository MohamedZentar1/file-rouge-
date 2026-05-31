package edu.polytech.filrouge_tp5.view;

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

import edu.polytech.filrouge_tp5.Menuable;
import edu.polytech.filrouge_tp5.R;

public class MenuFragment extends Fragment {
    private final String TAG = "frallo " + getClass().getSimpleName();
    private final int[] menuIcons = {
            R.drawable.ic_map,           // 0 -> Carte
            R.drawable.ic_list,          // 1 -> Liste des incidents
            R.drawable.ic_alert,         // 2 -> Signaler
            R.drawable.ic_mic,           // 3 -> Aide vocale
            R.drawable.ic_settings       // 4 -> Parametres
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

        updateMenuVisualState(imageViews);

        for (ImageView imageView : imageViews) {
            View.OnClickListener listener = menu -> selectMenuItem(imageView, imageViews);
            imageView.setOnClickListener(listener);
            if (imageView.getParent() instanceof View) {
                ((View) imageView.getParent()).setOnClickListener(listener);
            }
        }
        return layout;
    }

    private void selectMenuItem(ImageView imageView, List<ImageView> imageViews) {
        int nextIndex = Integer.parseInt(imageView.getTag().toString());
        if (nextIndex < 0 || nextIndex >= imageViews.size()) {
            return;
        }
        currentActivatedIndex = nextIndex;
        updateMenuVisualState(imageViews);
        menuable.onMenuChange(currentActivatedIndex);
    }

    private void updateMenuVisualState(List<ImageView> imageViews) {
        int primary = getResources().getColor(R.color.primary);
        int subtle = getResources().getColor(R.color.icon_default);
        for (int i = 0; i < imageViews.size(); i++) {
            ImageView imageView = imageViews.get(i);
            boolean selected = i == currentActivatedIndex;
            imageView.setImageResource(menuIcons[Math.min(i, menuIcons.length - 1)]);
            imageView.setColorFilter(selected ? primary : subtle);
            View parent = imageView.getParent() instanceof View ? (View) imageView.getParent() : imageView;
            parent.setBackgroundColor(selected ? getResources().getColor(R.color.primary_soft) : Color.TRANSPARENT);
            colorMenuLabel(parent, selected ? primary : subtle);
        }
    }

    private void colorMenuLabel(View parent, int color) {
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) parent;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            }
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
