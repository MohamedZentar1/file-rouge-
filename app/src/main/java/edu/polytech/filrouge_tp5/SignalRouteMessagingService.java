package edu.polytech.filrouge_tp5;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class SignalRouteMessagingService extends FirebaseMessagingService {

    private static final String TAG = "frallo FCM";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "SignalRoute";
        String body = "Nouvelle notification";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        if (remoteMessage.getData().containsKey("titre")) {
            title = remoteMessage.getData().get("titre");
        }
        if (remoteMessage.getData().containsKey("corps")) {
            body = remoteMessage.getData().get("corps");
        }

        Notifier.show(getApplicationContext(), title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Token FCM : " + token);
    }
}
