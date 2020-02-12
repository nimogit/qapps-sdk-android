package com.qassioun.android.demo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.qassioun.android.sdk.Qapps;
import com.qassioun.android.sdk.messaging.QappsPush;

//import com.qassioun.android.sdk.messaging.QappsPush;

/**
 * Demo service explaining Firebase Messaging notifications handling:
 * - how to decode Qapps messages;
 * - how to handle other notifications sent from other tools (FCM console, for example);
 * - how to override message handling based on message content;
 * - how to report Actioned metric back to Qapps server.
 */

public class DemoFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "DemoMessagingService";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d("DemoFirebaseService", "got new token: " + token);
        QappsPush.onTokenRefresh(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("DemoFirebaseService", "got new message: " + remoteMessage.getData());

        // decode message data and extract meaningful information from it: title, body, badge, etc.
        QappsPush.Message message = QappsPush.decodeMessage(remoteMessage.getData());

        if (message != null && message.has("typ")) {
            // custom handling only for messages with specific "typ" keys
            if (message.data("typ").equals("download")) {
                // Some bg download case.
                // We want to know how much devices started downloads after this particular message,
                // so we report Actioned metric back to server:

                // AppDownloadManager.initiateBackgroundDownload(message.link());
                message.recordAction(getApplicationContext());
                return;
            } else if (message.data("typ").equals("promo")) {
                // Now we want to override default Qapps UI for a promo message type.
                // We know that it should contain 2 buttons, so we start Activity
                // which would handle UI and report Actioned metric back to the server.

//                Intent intent = new Intent(this, PromoActivity.class);
//                intent.putExtra("qapps_message", message);
//                startActivity(intent);
//
//                // ... and then in PromoActivity:
//
//                final QappsPush.Message msg = intent.getParcelableExtra("qapps_message");
//                if (msg != null) {
//                    Button btn1 = new Button(this);
//                    btn1.setText(msg.buttons().get(0).title());
//                    btn1.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            msg.recordAction(getApplicationContext(), 1);
//                        }
//                    });
//
//                    Button btn2 = new Button(this);
//                    btn2.setText(msg.buttons().get(1).title());
//                    btn2.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            msg.recordAction(getApplicationContext(), 2);
//                        }
//                    });
//                }

                return;
            }
        }

        Intent intent = null;
        if (message.has("another")) {
            intent = new Intent(getApplicationContext(), AnotherActivity.class);
        }
        Boolean result = QappsPush.displayMessage(getApplicationContext(), message, R.drawable.ic_message, intent);
        if (result == null) {
            Log.i(TAG, "Message wasn't sent from Qapps server, so it cannot be handled by Qapps SDK");
        } else if (result) {
            Log.i(TAG, "Message was handled by Qapps SDK");
        } else {
            Log.i(TAG, "Message wasn't handled by Qapps SDK because API level is too low for Notification support or because currentActivity is null (not enough lifecycle method calls)");
        }

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}

