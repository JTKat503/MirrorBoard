package com.teamcreators.mirrorboard.activitiesforfamily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.teamcreators.mirrorboard.R;
import com.teamcreators.mirrorboard.utilities.Constants;
import com.teamcreators.mirrorboard.utilities.PreferenceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Page is about searching a new user
 * and send the request to the new user
 *
 * @author Xuannan Huang
 */
public class AddContactActivityFamily extends AppCompatActivity {
    private String receiverUserPhone;
    private String senderUserPhone;
    private Button sendRequest;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_family);

        // get the database instance
        db = FirebaseFirestore.getInstance();
        // get the button by ID
        ImageView goBack = findViewById(R.id.family_addContact_back);
        sendRequest = findViewById(R.id.family_addContact_sendRequest);
        sendRequest.setSelected(false);
        // save the current user information
        preferenceManager = new PreferenceManager(getApplicationContext());
        // current user's phone
        senderUserPhone = preferenceManager.getString(Constants.KEY_PHONE);

        // Click the send request button
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the receiver's Phone
                EditText getPhone = findViewById(R.id.family_addContact_phoneNum);
                receiverUserPhone = getPhone.getText().toString();
                // get the friend list and Check if you are friends
                // if not, then send the request
                // otherwise, show the message to current user
                getFriendList();
            }
        });

        // go back button
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
    }

    /**
     * get the current user's friend list from the database
     * and try to send the new request
     */
    private void getFriendList() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> myFriendsIDs = (List<String>) document.get(Constants.KEY_FRIENDS);
                                manageRequest(myFriendsIDs);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Failed to get contacts list", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "Get failed with " +
                                    task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * manage a request
     * if input number is empty, then show "Please enter a phone number"
     * if input number is current user phone, then show "You cannot add yourself"
     * if input number is current user's friend, then show "You are already friends."
     * otherwise, send the request
     *
     * @param myFriendsIDs
     */
    private void manageRequest(List<String> myFriendsIDs) {
        // search user from the data base
        if (TextUtils.isEmpty(receiverUserPhone)) {         // check the input number
            Toast.makeText(AddContactActivityFamily.this, "Please enter a phone number.", Toast.LENGTH_SHORT).show();
        } else if (receiverUserPhone.equals(senderUserPhone)) {     // check the input number == own number
            Toast.makeText(AddContactActivityFamily.this, "You cannot add yourself.", Toast.LENGTH_SHORT).show();
        } else if (myFriendsIDs.contains(receiverUserPhone)) {      // Check if the number is your friend
            Toast.makeText(AddContactActivityFamily.this, "You are already friends.", Toast.LENGTH_SHORT).show();
        } else {
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_PHONE, receiverUserPhone)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                // user does not exist
                                Toast.makeText(AddContactActivityFamily.this,
                                        "User does not exist", Toast.LENGTH_SHORT).show();
                            } else {
                                // update the number of the request to database
                                ifTheRequestNumIsZero();
                                // user exist, send the request
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "onFailure: " + e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * check the number of the requests
     * if the number is 0, then add 1
     * else need to check if the phone number is at Request
     */
    private void ifTheRequestNumIsZero() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiverUserPhone)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        if ((long) document.get(Constants.KEY_NUM_OF_REQUESTS) == 0) {
                            trueToAddRequestNum(false);
                        } else {
                            Toast.makeText(AddContactActivityFamily.this,
                                    "count " + document.get(Constants.KEY_NUM_OF_REQUESTS), Toast.LENGTH_SHORT).show();
                            increaseRequests();
                        }
                    }
                });
    }

    /**
     * if the user phone is not in the request, then add 1 to the request number
     * else, not add 1
     */
    private void increaseRequests() {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiverUserPhone)
                .collection("requests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            boolean flag = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getId().equals(senderUserPhone)){
                                    flag = true;
                                }
                            }
                            Toast.makeText(AddContactActivityFamily.this, "inside " + flag, Toast.LENGTH_SHORT).show();
                            trueToAddRequestNum(flag);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * if the flag is false, then add 1 to request number
     * @param flag
     */
    public void trueToAddRequestNum(boolean flag) {
        if (!flag) {
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .document(receiverUserPhone)
                    .update(Constants.KEY_NUM_OF_REQUESTS, FieldValue.increment(1));
        }
        sendRequests();
    }

    /**
     * send the request to new friend
     * store the information, add the information to database
     * show the message to user "Successfully send"
     */
    private void sendRequests() {
        sendRequest.setEnabled(false);
        Map<String, Object> sender = new HashMap<>();
        sender.put(Constants.KEY_PHONE, preferenceManager.getString(Constants.KEY_PHONE));
        sender.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
        sender.put(Constants.KEY_AVATAR_URI, preferenceManager.getString(Constants.KEY_AVATAR_URI));
        db.collection("users").document(receiverUserPhone).collection("requests").document(senderUserPhone)
                .set(sender)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
//                            Toast.makeText(AddContactActivity.this, "Successfully send", Toast.LENGTH_SHORT).show();
                            sendRequest.setEnabled(true);
                            sendRequest.setText(R.string.request_sent);
                            sendRequest.setSelected(true);
                        }
                    }
                });
    }
}