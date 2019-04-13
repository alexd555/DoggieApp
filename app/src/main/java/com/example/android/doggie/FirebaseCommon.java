package com.example.android.doggie;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.example.android.doggie.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseCommon {
    private static final String TAG = "FirebaseCommon";

    public static void updateSignStatus(User user) {
        try {
            DocumentReference userRef = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user.getUserId());
            userRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: \nlogin status is updated into database.");
                    }
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "update status error" + e.getMessage());
        }
    }
    public static void createNewUser(FirebaseUser firebaseUser) {
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mDb.setFirestoreSettings(settings);

        User user = new User();
        user.setUsername(firebaseUser.getDisplayName());
        user.setEmail(firebaseUser.getEmail());
        user.setUserId(firebaseUser.getUid());
        if (firebaseUser.getPhotoUrl()!=null)
            user.setImagePath(firebaseUser.getPhotoUrl().toString());
        user.setLogOut("No");
        user.setOnline("Yes");
        user.updateOnlineStatus();
        DocumentReference newUserRef = mDb
                .collection("Users")
                .document(user.getUserId());

        newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                }else{
                }
            }
        });
    }
}
