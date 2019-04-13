package com.example.android.doggie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.doggie.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class UserProfileActivity extends AppCompatActivity {
    //Variables
    private Button btnChoose, btnUpload;
    private ImageView imageView;

    private static final String TAG = "ProfileActivity";

    private Uri filePath;
    StorageReference ref;
    private final int PICK_IMAGE_REQUEST = 10;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_profile);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Initialize Views
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        imageView = (ImageView) findViewById(R.id.imgView);

        User user = ((UserClient)getApplicationContext()).getUser();
//        String imagePath = "https://firebasestorage.googleapis.com/v0/b/mynewproject-cff19.appspot.com/o/images%2F44e096e1-e8ef-4f93-8103-80ee66822dde?alt=media&token=8f8d6e18-149b-46a2-afb0-c6ec5536c77d";
        // Load image
                Glide.with(imageView.getContext()).
                        load(user.getImagePath()).
                        into(imageView);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);

                User user = ((UserClient) getApplicationContext()).getUser();
                user.setImagePath(filePath.getPath());
                ((UserClient) getApplicationContext()).setUser(user);
//                FirebaseFirestore.getInstance()
//                        .collection(getString(R.string.collection_users))
//                        .document(user.getUserId())
//                        .set(user);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfileActivity.this, "Uploaded",
                                    Toast.LENGTH_SHORT).show();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "successfully uploaded dog's image from gallery to Firebase Storage. uri= " + uri.toString());
                                    String imageUrl = uri.toString();
                                    // update the client and database
                                    User user = ((UserClient) getApplicationContext()).getUser();
                                    user.setImagePath(imageUrl);
                                    ((UserClient) getApplicationContext()).setUser(user);
//                                    FirebaseFirestore.getInstance()
//                                            .collection(getString(R.string.collection_users))
//                                            .document(user.getUserId())
//                                            .set(user);
                                }
                            });
                        }})
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfileActivity.this,
                                    "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
}
