package edu.polytech.filrouge_tp5;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.polytech.filrouge_tp5.model.EmergencyService;


/**
 *  Fragment pret a remplir
 */
public class Screen4Fragment extends Fragment {
    public final static int FRAGMENT_ID = 3;
    private final String TAG = "frallo "+getClass().getSimpleName();
    private Notifiable notifiable;
    private RecyclerView recyclerView;
    private AlertAdapter adapter;

    public Screen4Fragment() {
        Log.d(TAG,"screenFragment type 4 created"); // Required empty public constructor
    }


    @Override
    public void onStart() {
        super.onStart();
        notifiable.onFragmentDisplayed(FRAGMENT_ID);
        updateAlerts();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen4, container, false);
        recyclerView = view.findViewById(R.id.recyclerAlerts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AlertAdapter(EmergencyService.getInstance().getAlerts());
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void updateAlerts() {
        if (adapter != null) {
            adapter.setAlerts(EmergencyService.getInstance().getAlerts());
        }
    }

    private static class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {
        private List<String> alerts;

        AlertAdapter(List<String> alerts) {
            this.alerts = alerts;
        }

        void setAlerts(List<String> alerts) {
            this.alerts = alerts;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_alert, parent, false);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 10;
            view.setLayoutParams(params);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String message = alerts.get(position);
            holder.textView.setText(message);

            boolean isCritical = message.startsWith("ALERT");
            holder.icon.setBackgroundResource(isCritical
                    ? R.drawable.bg_circle_danger
                    : R.drawable.bg_circle_primary);
            holder.icon.setImageResource(isCritical
                    ? R.drawable.ic_alert
                    : R.drawable.ic_notifications);
        }

        @Override
        public int getItemCount() {
            return alerts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textView;
            final ImageView icon;

            ViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.alertText);
                icon = v.findViewById(R.id.alertIcon);
            }
        }
    }
}
