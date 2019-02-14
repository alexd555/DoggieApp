package com.example.android.doggie;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.doggie.models.Dog;
import com.example.android.doggie.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private static final int GALLARY_INTENT = 2;

    private static final String RESTAURANT_URL_FMT = "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png";
    private static final int MAX_IMAGE_NUM = 22;

    @BindView(R.id.input_dog_name)
    EditText mDogNameView;

    @BindView(R.id.input_dog_age)
    EditText mDogAgeView;

    @BindView(R.id.input_dog_weight)
    EditText mDogWeightView;

    @BindView(R.id.gender_button)
    RadioGroup mGenderRadioGroup;

    @BindView(R.id.input_dog_breed)
    Spinner mBreedSpinner;

//    @BindView(R.id.upload_image_button)
//    ImageButton mImageButton;


    private FirebaseFirestore mFirestore;

    private FirebaseStorage storage;

    private StorageReference storageRef;
    private StorageReference imageRef;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Get the Uri of the image from the gallery intent data
        if (requestCode == GALLARY_INTENT && resultCode == RESULT_OK) {

            Uri file = data.getData();

            // Make the chosen image from gallery be seen to the user in the sign up form
//            mImageButton.setImageURI(file);

            // Create a storage reference to the dog's image
            storageRef = storage.getReference();

            // Create a reference to "dogs.jpg"
            imageRef = storageRef.child("images/"+file.getLastPathSegment());

            imageRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "onSuccess: uri= "+ uri.toString());
                            imageUrl = uri.toString();
                        }
                    });
                }
            });
//            UploadTask uploadTask = imageRef.putFile(file);
//
//            // Register observers to listen for when the download is done or if it fails
//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // TODO : Handle unsuccessful uploads
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                    // ...
//                    // TODO : Handle successful uploads
//                }
//            });
        }
    }

    @OnClick(R.id.upload_image_button)
    public void onButtonImageClicked() {
        Log.d(TAG, "launchCamera");

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLARY_INTENT);
    }

    @OnClick(R.id.button_cancel)
    public void onCancelClicked() { onBackPressed(); }

    @OnClick(R.id.button_apply)
    public void onApplyClicked() {

        // TODO : check the user's input
        String name, gender, breed;
        int age, weight;

        // Get the dog's name
        name = mDogNameView.getText().toString();

        // Get the dog's age
        age = Integer.parseInt(mDogAgeView.getText().toString());

        // Get the dog's weight
        weight = Integer.parseInt(mDogWeightView.getText().toString());

        // Get the dog's breed from the selected list
        breed = (String) mBreedSpinner.getSelectedItem();
        if (getString(R.string.value_any_breed).equals(breed)) {
            // No breed is selected. Notify the user he MUST select breed
            Toast.makeText(this, "You must select breed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the dog's gender. If no option is checked than 'unspecified gender' is given
        int checkedId = mGenderRadioGroup.getCheckedRadioButtonId();
        RadioButton b = (RadioButton) mGenderRadioGroup.findViewById(checkedId);
        if (b != null && checkedId > -1) {
            gender = (String) b.getText();
        } else {
            // No gender is selected. Notify the user he MUST select gender in order to proceed
            Toast.makeText(this, "You must specify gender", Toast.LENGTH_SHORT).show();
            return;
        }


        final Dog dog = new Dog(FirebaseAuth.getInstance().getCurrentUser(),
                name, weight, breed, 0, age, gender);

        // Handle dog's image

//        dog.setImage(getRandomImageUrl(new Random()));
        dog.setImage(imageUrl);

        // Create reference for new dog, for use inside the transaction
        DocumentReference dogRef = mFirestore
                .collection(getString(R.string.collection_dogs))
                .document();
        dogRef.set(dog);
        User user = ((UserClient)(getApplicationContext())).getUser();
        DocumentReference newDogRef = mFirestore
                .collection(getString(R.string.collection_users))
                .document(user.getUser_id())
                .collection(getString(R.string.collection_user_dogs))
                .document(dogRef.getId());

        newDogRef.set(dog);
//        // TODO : change from batch to single write
//        WriteBatch batch = mFirestore.batch();
//        batch.set(dogRef, dog);
//
//        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    Log.d(TAG, "Write batch succeeded.");
//                } else {
//                    Log.w(TAG, "write batch failed.", task.getException());
//                }
//            }
//        });
        // Return to previous activity
        onBackPressed();
    }

//    private static String getImageUrl() {

//    }


    /**
     * Get a random image.
     */
    private static String getRandomImageUrl(Random random) {
        // Integer between 1 and MAX_IMAGE_NUM (inclusive)
        int id = random.nextInt(MAX_IMAGE_NUM) + 1;

        return String.format(Locale.getDefault(), RESTAURANT_URL_FMT, id);
    }

}
