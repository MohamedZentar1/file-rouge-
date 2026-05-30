package edu.polytech.filrouge_tp5;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.polytech.filrouge_tp5.R;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.polytech.filrouge_tp5.model.Issue;

/**
 * ScreenxFragment used as the incident detail screen.
 *
 * <p>It receives an Issue from ControlActivity through arguments and only
 * reports button actions back to the activity with {@link Notifiable}.</p>
 */
public class Screen1Fragment extends Fragment {
    public static final int FRAGMENT_ID = 0;

    private final String TAG = "frallo " + getClass().getSimpleName();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
    private Notifiable notifiable;

    public Screen1Fragment() {
        Log.d(TAG, "screenFragment type 1 created");
    }

    /**
     * Called when this ScreenxFragment becomes started/visible. It notifies the
     * activity so the menu can reflect BackStack navigation.
     */
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
        View view = inflater.inflate(R.layout.fragment_screen1, container, false);

        TextView screenTitle = view.findViewById(R.id.labelScreen1Fragment);
        TextView detailTitle = view.findViewById(R.id.detailTitle);
        TextView content = view.findViewById(R.id.topic);
        TextView meta = view.findViewById(R.id.detailMeta);
        TextView protocol = view.findViewById(R.id.detailProtocol);
        ImageView picture = view.findViewById(R.id.picture);

        screenTitle.setText(getString(R.string.Screen1Fragment_label));

        Issue issue = readIssueArgument();
        if (issue != null) {
            detailTitle.setText(issue.getTitle());
            content.setText(issue.getDescription() + "\n\nPosition : " + issue.getLocation());
            meta.setText(issue.getContextLabel() + " | " + issue.getPriorityLabel()
                    + " | " + issue.getStatusLabel()
                    + " | " + dateFormat.format(new Date(issue.getTimestamp())));
            protocol.setText("Protocole : " + issue.getSafetyProtocol());
            picture.setImageResource(issue.getPriority() == Issue.Priority.CRITICAL
                    ? R.drawable.ic_warning_critical
                    : R.drawable.ic_alert);
        }

        view.findViewById(R.id.button).setOnClickListener(clic -> notifiable.onClick(FRAGMENT_ID));
        return view;
    }

    private Issue readIssueArgument() {
        if (getArguments() == null) {
            return null;
        }
        getArguments().setClassLoader(Issue.class.getClassLoader());
        return getArguments().getParcelable(getString(R.string.issue));
    }
}
