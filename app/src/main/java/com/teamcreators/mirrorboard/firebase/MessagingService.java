package com.teamcreators.mirrorboard.firebase;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.teamcreators.mirrorboard.activitiesforelderly.IncomingCallActivityElderly;
import com.teamcreators.mirrorboard.activitiesforfamily.IncomingCallActivityFamily;
import com.teamcreators.mirrorboard.utilities.Constants;
import com.teamcreators.mirrorboard.utilities.PreferenceManager;

/**
 * A class to monitor incoming calls and build the call inviter information
 * displayed on incoming call interface
 *
 * @author Jianwei Li
 */
public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        String mode = preferenceManager.getString(Constants.KEY_MODE);
        if (type != null) {
            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                Intent intent;
                /*
                * According to the requirements of the UX Design team, Elderly mode
                * and Relatives mode start different interfaces when a call comes in
                */
                if (mode != null && mode.equals(Constants.MODE_ELDERLY)) {
                    intent = new Intent(getApplicationContext(), IncomingCallActivityElderly.class);
                } else {
                    intent = new Intent(getApplicationContext(), IncomingCallActivityFamily.class);
                }
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)
                );
                intent.putExtra(
                        Constants.KEY_NAME,
                        remoteMessage.getData().get(Constants.KEY_NAME)
                );
                intent.putExtra(
                        Constants.KEY_PHONE,
                        remoteMessage.getData().get(Constants.KEY_PHONE)
                );
                intent.putExtra(
                        Constants.KEY_AVATAR_URI,
                        remoteMessage.getData().get(Constants.KEY_AVATAR_URI)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                // send accept/reject actions back to the sender to notify sender
                // send cancel action to the receiver to cancel invitation on receiver side
                Intent intent = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }
}
