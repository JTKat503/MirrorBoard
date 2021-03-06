package com.teamcreators.mirrorboard.activitiesforfamily;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.teamcreators.mirrorboard.R;
import com.teamcreators.mirrorboard.utilities.Constants;
import com.teamcreators.mirrorboard.utilities.NetworkConnection;
import com.teamcreators.mirrorboard.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * the page for showing the requests
 * if the user accept or remove request,
 * then will show the next new request or show "NO new request"
 *
 * @author Xuannan Huang
 */
public class InfoRequestActivityFamily extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private TextView noRequest, contactName, contactNumber;
    private ImageView profileImage, goBack;
    private Button addContact, removeRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_request_family);

        db = FirebaseFirestore.getInstance();
        addContact = findViewById(R.id.family_infoRequest_add);
        removeRequest = findViewById(R.id.family_infoRequest_remove);
        noRequest = findViewById(R.id.family_infoRequest_errorMessage);
        contactName = findViewById(R.id.family_infoRequest_name);
        contactNumber = findViewById(R.id.family_infoRequest_phoneNum);
        profileImage = findViewById(R.id.family_infoRequest_avatar);
        goBack = findViewById(R.id.family_infoRequest_back);
        Button exitApp = findViewById(R.id.family_infoRequest_exitApp);
        LinearLayout offlineWarning = findViewById(R.id.family_infoRequest_offlineWarning);
        // save current user info
        preferenceManager = new PreferenceManager(getApplicationContext());
        // show the request user
        showUserProfile();
        contactName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        contactName.setMarqueeRepeatLimit(1);
        contactName.setSelected(true);

        // Monitor network connection changes. @author Jianwei Li
        NetworkConnection networkConnection = new NetworkConnection(getApplicationContext());
        networkConnection.observe(this, isConnected -> {
            if (isConnected) {
                goBack.setVisibility(View.VISIBLE);
                offlineWarning.setVisibility(View.GONE);
            } else {
                offlineWarning.setVisibility(View.VISIBLE);
                goBack.setVisibility(View.GONE);
            }
        });

        // adding contact button
        addContact.setOnClickListener(view -> {
            // get the sender phone
            String senderPhone = contactNumber.getText().toString();
            // get current user ID
            String receiverPhone = preferenceManager.getString(Constants.KEY_PHONE);
            // get and update the receiver's friend list
            // add the sender phone number to receiver friend list
            getAndUpdateFriendList(receiverPhone, senderPhone);
            // get and update the sender's friend list
            // add the receiver phone number to sender friend list
            getAndUpdateFriendList(senderPhone, receiverPhone);
            // show the message to tell the user Contact added
            Snackbar.make(findViewById(android.R.id.content),
                    "Contact added", Snackbar.LENGTH_SHORT).show();
            // after adding the new friend
            // delete the request and update the number of requests
            deleteRequestAndUpdateRequestNumber(senderPhone);
            // show the next request or no request
            showUserProfile();
        });

        // removing a Request button
        removeRequest.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(InfoRequestActivityFamily.this);
            builder.setMessage("Are you sure you want to delete this request?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                        String senderPhone = contactNumber.getText().toString();
                        deleteRequestAndUpdateRequestNumber(senderPhone);
                        showUserProfile();
                    }).show();
        });

        // goBack button
        goBack.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });

        // exit app button on the offline warning page
        exitApp.setOnClickListener(view -> {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        });
    }

    /**
     * show the request information
     * after the accept or remove the request, will show the next request
     * if no new request, will show the message "No new request"
     */
    private void showUserProfile() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_PHONE))
                .collection("requests").get().addOnCompleteListener(task -> {
                    String senderName;
                    String senderPhone;
                    if (task.isSuccessful()
                            && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        senderName = documentSnapshot.getString(Constants.KEY_NAME);
                        senderPhone = documentSnapshot.getString(Constants.KEY_PHONE);
                        contactName.setText(senderName);
                        contactNumber.setText(senderPhone);
                        Glide.with(InfoRequestActivityFamily.this)
                                .load(documentSnapshot.getString(Constants.KEY_AVATAR_URI))
                                .error(R.drawable.blank_profile)
                                .into(profileImage);
                        // @author  below added by Jianwei Li
                        profileImage.setVisibility(View.VISIBLE);
                        contactName.setVisibility(View.VISIBLE);
                        contactNumber.setVisibility(View.VISIBLE);
                        addContact.setVisibility(View.VISIBLE);
                        removeRequest.setVisibility(View.VISIBLE);
                    } else {
                        profileImage.setVisibility(View.INVISIBLE);
                        contactName.setVisibility(View.INVISIBLE);
                        contactNumber.setVisibility(View.INVISIBLE);
                        addContact.setVisibility(View.INVISIBLE);
                        removeRequest.setVisibility(View.INVISIBLE);
                        noRequest.setText(R.string.no_new_requests);
                        noRequest.setVisibility(View.VISIBLE);
                        // @author  above added by Jianwei Li
                    }
                });
    }

    /**
     * get and update the receiver's friend list, add the sender phone number to receiver friend list
     * get and update the sender's friend list, add the receiver phone number to sender friend list
     * @param userPhone
     * @param phoneNumber
     */
    private void getAndUpdateFriendList(String userPhone, String phoneNumber) {
        // get the receiver's friend list
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userPhone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        List<String> userFriendsList = new ArrayList<>();
                        if (document.exists()) {
                            userFriendsList = (List<String>) document.get(Constants.KEY_FRIENDS);
                        }
                        if (userFriendsList != null && !userFriendsList.contains(phoneNumber)) {
                            userFriendsList.add(phoneNumber);
                            // update the friends list
                            addFriendToList(userPhone, userFriendsList);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Get failed with " + task.getException(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    /**
     * Update current user's contacts list
     * @param userPhone current user's phone number
     * @param friendsList contacts list to be updated to database
     */
    public void addFriendToList(String userPhone, List<String> friendsList) {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(userPhone) // current user
                .update(Constants.KEY_FRIENDS, friendsList);
    }

    /**
     * delete the request after accepting or removing
     * @param senderPhone
     */
    public void deleteRequestAndUpdateRequestNumber(String senderPhone) {
        // delete the request from the database
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_PHONE))
                .collection("requests")
                .document(senderPhone).delete();
        // update the requests number
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .update(Constants.KEY_NUM_OF_REQUESTS, FieldValue.increment(-1));
    }
}