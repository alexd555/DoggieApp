package com.example.android.doggie;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.doggie.models.Dog;
import com.example.android.doggie.models.User;
import com.example.android.doggie.models.UserLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.joda.time.LocalDate;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity{

    private static final String TAG = "SignUpActivity";

    private static final int GALLARY_INTENT = 2;

    @BindView(R.id.input_dog_name)
    EditText mDogNameView;

    @BindView(R.id.input_dog_weight)
    EditText mDogWeightView;

    @BindView(R.id.dog_birth_date)
    TextView mDisplayDate;





    @BindView(R.id.gender_button)
    RadioGroup mGenderRadioGroup;

    @BindView(R.id.input_dog_breed)
    Spinner mBreedSpinner;

    private FirebaseFirestore mFirestore;

    private FirebaseStorage storage;

    private StorageReference storageRef;
    private StorageReference imageRef;

    private String imageUrl;

    private UserLocation mUserLocation;
    private LocalDate dateOfBirth;

    private DatePickerDialog.OnDateSetListener mDateSetListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();

        // Present the selected dog's birth date on the screen
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                dateOfBirth = new LocalDate(year, month, day);
                String date = "Dog's Birth Date: " + day + "/" + month + "/" + year;
                mDisplayDate.setText(date);
            }
        };
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Get the Uri of the image from the gallery intent data
        if (requestCode == GALLARY_INTENT && resultCode == RESULT_OK) {

            Uri file = data.getData();

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
                            Log.d(TAG, "successfully uploaded dog's image from gallery to Firebase Storage. uri= "+ uri.toString());
                            imageUrl = uri.toString();
                        }
                    });
                }
            });
        }
    }


    @OnClick(R.id.upload_image_button)
    public void onButtonImageClicked() {
        Log.d(TAG, "launching gallery");

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLARY_INTENT);
    }


    @OnClick(R.id.dog_birth_date)
    public void onSelectDateClicked() {
        Log.d(TAG, "Date Selection has been clicked");

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateSetListener,
                year, month, day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    @OnClick(R.id.button_cancel)
    public void onCancelClicked() {
        Log.d(TAG, "canceling dog's registration");
        onBackPressed();
    }


    @OnClick(R.id.button_apply)
    public void onApplyClicked() {

        String name, gender, breed;
        double weight;

        // Get the dog's name
        name = mDogNameView.getText().toString();
        if (name.isEmpty()) {
            // No name is given. Notify the user he MUST specify name
            Toast.makeText(this, "You must specify your dog's name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the dog's weight
        weight = Double.parseDouble(mDogWeightView.getText().toString());

        // can't set a future date of birth
        if (dateOfBirth.isAfter(LocalDate.now())) {
            Toast.makeText(this, "You can't select a future date", Toast.LENGTH_SHORT).show();
            return;
        }

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

        // TODO: fix distance value


        final Dog dog = new Dog(FirebaseAuth.getInstance().getCurrentUser(),
                name, weight, breed, 0, gender, dateOfBirth);

        // Handle dog's image
        dog.setImage(imageUrl);

//        DocumentReference dogRef = mFirestore
//                .collection(getString(R.string.collection_dogs))
//                .document();
//        dog.setDogId(dogRef.getId());
//        User user = ((UserClient)(getApplicationContext())).getUser();
//        DocumentReference newDogRef = mFirestore
//                .collection(getString(R.string.collection_user_locations))
//                .document(user.getUserId())
//                .collection(getString(R.string.collection_user_dogs))
//                .document(dogRef.getId());
//        newDogRef.set(dog);
        // Create reference for new dog, for use inside the transaction
        DocumentReference dogRef = mFirestore
                .collection(getString(R.string.collection_dogs))
                .document();
        dog.setDogId(dogRef.getId());
        dogRef.set(dog);

        onBackPressed();
    }

}
