package edu.polytech.filrouge_tp5;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.polytech.filrouge_tp5.R;
import androidx.fragment.app.Fragment;


/**
 *  Fragment pret a remplir
 */
public class Screen6Fragment extends Fragment {
    public final static int FRAGMENT_ID = 5;
    private final String TAG = "frallo "+getClass().getSimpleName();
    private Notifiable notifiable;



    public Screen6Fragment() {
        Log.d(TAG,"screenFragment type 6 created"); // Required empty public constructor
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
            //Log.d(TAG, "Class " + requireActivity().getClass().getSimpleName() + " implements Notifiable.");
        } else {
            throw new AssertionError("Classe " + requireActivity().getClass().getName() + " ne met pas en oeuvre Notifiable.");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen6, container, false);


        return view;
    }
}
