package com.example.android.doggie;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.doggie.adapter.DogAdapter;
import com.example.android.doggie.models.Dog;
import com.example.android.doggie.models.User;
import com.example.android.doggie.models.UserLocation;
import com.example.android.doggie.services.LocationService;
import com.example.android.doggie.viewmodel.MainActivityViewModel;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.android.doggie.Constants.ERROR_DIALOG_REQUEST;
import static com.example.android.doggie.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.android.doggie.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity implements
        FilterDialogFragment.FilterListener,
        DogAdapter.OnDogSelectedListener {

    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 2810;

    private static final int LIMIT = 50;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.text_current_search)
    TextView mCurrentSearchView;

    @BindView(R.id.text_current_sort_by)
    TextView mCurrentSortByView;

    @BindView(R.id.recycler_dogs)
    RecyclerView mDogsRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth firebaseAuth;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;
    private DogAdapter mAdapter;

    private MainActivityViewModel mViewModel;

    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private ListenerRegistration mUserListEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);


        // Access a Cloud Firestore instance from MainActivity
        mFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
//        // Get ${LIMIT} dogs
        mQuery = mFirestore.collection(getString(R.string.collection_dogs))
                .orderBy("distance", Query.Direction.ASCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new DogAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mDogsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mDogsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mDogsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mDogsRecycler.setAdapter(mAdapter);

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();

        getUsers();
    }

    @Override
    public void onStart() {
        super.onStart();
//
//        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }
//
//        // Apply filters
        onFilter(mViewModel.getFilters());

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }
//
    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_map:
                show_on_map();
                break;
            case R.id.menu_add_dog:
                onAddDogClicked();
                break;
            case R.id.menu_my_dogs:
                onMyDogsClicked();
                break;
            case R.id.menu_sign_out:
                signOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void show_on_map()
    {
        UserListFragment fragment = UserListFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.intent_user_list), mUserList);
        bundle.putParcelableArrayList(getString(R.string.intent_user_locations), mUserLocations);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.user_list_container, fragment, getString(R.string.fragment_user_list));
        transaction.addToBackStack(getString(R.string.fragment_user_list));
        transaction.commit();
    }
    private void getUsers(){

        CollectionReference usersRef = mFirestore
                .collection(getString(R.string.collection_users));

        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){

                            // Clear the list and add all the users again
                            mUserList.clear();
                            mUserList = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                User user = doc.toObject(User.class);
                                mUserList.add(user);
                                getUserLocation(user);
                            }

                            Log.d(TAG, "onEvent: user list size: " + mUserList.size());
                        }
                    }
                });
    }
    private void getUserLocation(User user){
        DocumentReference locationsRef = mFirestore
                .collection(getString(R.string.collection_user_locations))
                .document(user.getUser_id());

        locationsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    if(task.getResult().toObject(UserLocation.class) != null){

                        mUserLocations.add(task.getResult().toObject(UserLocation.class));
                    }
                }
            }
        });

    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == RC_SIGN_IN) {
////            IdpResponse response = IdpResponse.fromResultIntent(data);
//            mViewModel.setIsSigningIn(false);
//
//            if (resultCode != RESULT_OK) {
//                if (response == null) {
//                    // User pressed the back button.
//                    finish();
//                } else if (response.getError() != null
//                        && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
//                    showSignInErrorDialog(R.string.message_no_network);
//                } else {
//                    showSignInErrorDialog(R.string.message_unknown);
//                }
//            }
//        }
//    }

    @OnClick(R.id.filter_bar)
    public void onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);
    }

    @OnClick(R.id.button_clear_filter)
    public void onClearFilterClicked() {
        mFilterDialog.resetFilters();

        onFilter(Filters.getDefault());
    }

    @Override
    public void onDogSelected(DocumentSnapshot dog) {

    }

    @Override
    public void onFilter(Filters filters) {
        // Construct query basic query
        Query query = mFirestore.collection(getString(R.string.collection_dogs));

        // Breed (equality filter)
        if (filters.hasBreed()) {
            query = query.whereEqualTo(Dog.FIELD_BREED, filters.getBreed());
        }

        // Gender (equality filter)
        if (filters.hasGender()) {
            query = query.whereEqualTo(Dog.FIELD_GENDER, filters.getGender());
        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        // Limit items
        query = query.limit(LIMIT);

        // Update the query
        mAdapter.setQuery(query);

        // Set header
        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));
        mCurrentSortByView.setText(filters.getOrderDescription(this));

        // Save filters
        mViewModel.setFilters(filters);
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }
//
    private void startSignIn() {
//        // Sign in with FirebaseUI
//        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
//                .setAvailableProviders(Collections.singletonList(
//                        new AuthUI.IdpConfig.EmailBuilder().build()))
//                .setIsSmartLockEnabled(false)
//                .build();
//
//        startActivityForResult(intent, RC_SIGN_IN);
        mViewModel.setIsSigningIn(true);
    }
    private boolean isFacebookSigned()
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.getProviders() == null)
            return false;
        String provider = user.getProviders().get(0);
        return provider.equals("facebook.com");
    }
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        if (isFacebookSigned()) {
            LoginManager.getInstance().logOut();
        }
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void onAddDogClicked() {
        // Go to registration page
        Intent intent = new Intent(this, SignUpActivity.class);

        startActivity(intent);
//        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

    }

    private void onMyDogsClicked() {

    }



//    private void showSignInErrorDialog(@StringRes int message) {
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle(R.string.title_sign_in_error)
//                .setMessage(message)
//                .setCancelable(false)
//                .setPositiveButton(R.string.option_retry, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        startSignIn();
//                    }
//                })
//                .setNegativeButton(R.string.option_exit, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        finish();
//                    }
//                }).create();
//
//        dialog.show();
//    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                MainActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }
    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.android.doggie.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }
    private void getUserDetails(){
        if(mUserLocation == null){
            mUserLocation = new UserLocation();
            DocumentReference userRef = mFirestore.collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        User user = task.getResult().toObject(User.class);
                        mUserLocation.setUser(user);
                        ((UserClient)(getApplicationContext())).setUser(user);
                        getLastKnownLocation();
                    }
                }
            });
        }
        else{
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mUserLocation.setGeo_point(geoPoint);
                    mUserLocation.setTimestamp(null);
                    saveUserLocation();
                    startLocationService();

                }
            }
        });

    }

    private void saveUserLocation(){

        if(mUserLocation != null){
            DocumentReference locationRef = mFirestore
                    .collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + mUserLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + mUserLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }
    }

    private void buildAlertMessageNoGps() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final android.app.AlertDialog alert = builder.create();
        alert.show();
    }
    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (manager == null) {
            buildAlertMessageNoGps();
            return false;
        }
        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }
    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                getUserDetails();
            }
            else{
                getLocationPermission();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mUserListEventListener != null){
            mUserListEventListener.remove();
        }
    }
}
