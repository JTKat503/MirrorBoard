package com.teamcreators.mirrorboard.utilities;

import java.util.HashMap;

/**
 * A class that holds constants used by the APP
 *
 * @author Jianwei Li
 */
public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_NAME = "name";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_AVATAR_URI = "avatarUri";
    public static final String KEY_USER_ID = "user_id"; // user's Key(phone num), subRoot of "users"
    public static final String KEY_FCM_TOKEN = "fcm_token";
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_HOBBIES = "hobbies";

    public static final String KEY_PREFERENCE_NAME = "mirrorBoardPreference";
    public static final String KEY_NUM_OF_REQUESTS = "numberOfRequests";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_MODE = "mode";
    public static final String KEY_NOTICE_ON = "notice_on";
    public static final String MODE_ELDERLY = "Elderly";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";
    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";

    public static final int MIN_PHONE_NUM_LENGTH = 10;

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAAtT87LrQ:APA91bEZoPLWeruPsAKPesZFb-xZ3VPj7bsFb0y3kw0RItLLTiRgLqcMJlv6Ki-YSIX335RBJeksR8odrSzkfgGCR9QLK7_n1DxKdpmI0uvGgtYtkOtpVsDqC_gEO4jrRT9Vzva4HBqe"
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE, "application/json");
        return headers;
    }



}
