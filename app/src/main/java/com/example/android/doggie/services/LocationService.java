package com.example.android.doggie.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

//import com.example.android.doggie.FireBaseCommon;
import com.example.android.doggie.R;
import com.example.android.doggie.UserClient;
import com.example.android.doggie.models.Dog;
import com.example.android.doggie.models.User;
import com.example.android.doggie.models.UserLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.protobuf.DescriptorProtos;

import java.util.ArrayList;
import java.util.Date;


public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */

    private ArrayList <UserLocation> mUserLocations = new ArrayList<>();
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<Dog> mDogList = new ArrayList<>();

    private UserLocation mUserLocation;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
            if (manager!=null) {
                manager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }
    private void updateSignStatus(User user) {
        try {
            DocumentReference userRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_users))
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
    private void getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d(TAG, "onLocationResult: got location result.");

                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            User user = ((UserClient)(getApplicationContext())).getUser();
//                            if (user!=null) {
//                                user.setRunning("Yes");
//                                user.updateOnlineStatus();
//                            }
//                            ((UserClient)(getApplicationContext())).setUser(user);
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            UserLocation userLocation = new UserLocation(user, geoPoint, null);

//                            if (user !=null)
//                            {
//                                userLocation.getUser().setRunning("Yes");
//                                userLocation.getUser().setLogOut(user.getIsLogOut());
//                                userLocation.getUser().updateOnlineStatus();
//                            }
//                            updateSignStatus(user);
//                            FireBaseCommon.updateSignStatus(user);
                            saveUserLocation(userLocation);
                            saveUser(user);
                            calculateDogDistrance(userLocation);
//                            UpdateUsersStatus();

                        }
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }


    private void UpdateUsersStatus()
    {
        getUsers();
        for (UserLocation userLocation: mUserLocations)
        {
            Long currentTime = System.currentTimeMillis();
            Long userTime = currentTime;
            if (userLocation!=null &&  userLocation.getTimestamp()!=null)
                userTime = userLocation.getTimestamp().getTime();
            if ((userLocation!=null &&  userLocation.getTimestamp()!=null))
            {
                if (currentTime - userTime > 60 * 1000) {
                    userLocation.getUser().setRunning("No");
                    userLocation.getUser().updateOnlineStatus();
                }
            }
        }
        for (UserLocation userLocation: mUserLocations) {
            saveUserLocation(userLocation);
            updateSignStatus(userLocation.getUser());
        }
    }
    private void saveUserLocation(final UserLocation userLocation){

        try{
            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_user_locations))
                    .document(userLocation.getUser().getUserId());

            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Log.d(TAG, "onComplete: \ninserted user location into database." +
                                "\n latitude: " + userLocation.getGeo_point().getLatitude() +
                                "\n longitude: " + userLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        }

    }
    private void saveUser(final User user){

        try{
            DocumentReference userRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_users))
                    .document(user.getUserId());

            userRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                    }
                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        }

    }
    private void getUsers(){

        final CollectionReference usersRef = FirebaseFirestore.getInstance()
                .collection(getString(R.string.collection_users));

        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
        DocumentReference locationsRef = FirebaseFirestore.getInstance()
                .collection(getString(R.string.collection_user_locations))
                .document(user.getUserId());

        locationsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    if(task.getResult()!=null && task.getResult().toObject(UserLocation.class) != null){

                        mUserLocations.add(task.getResult().toObject(UserLocation.class));
                    }
                }
            }
        });

    }
    private void saveDog(final Dog dog)
    {
        try{
            DocumentReference locationDogRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_dogs))
                    .document(dog.getDogId());

            locationDogRef.set(dog).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Log.d(TAG, "position dog is updated");
//                        Log.d(TAG, "onComplete: \ninserted user location into database." +
//                                "\n latitude: " + userLocation.getGeo_point().getLatitude() +
//                                "\n longitude: " + userLocation.getGeo_point().getLongitude());
                    }
                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        }
    }
    private void updateDogLocation() {
        for (Dog dog :mDogList)
        {
            saveDog(dog);
        }
    }

    // Calculate the dog distances from currentUser
    private void calculateDogDistrance(final UserLocation userLocation)
    {
        CollectionReference usersRef = FirebaseFirestore.getInstance()
                .collection(getString(R.string.collection_dogs));

        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }

                if(queryDocumentSnapshots != null){

                    // Clear the list and add all the users again
                    mDogList.clear();
                    mDogList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Dog dog = doc.toObject(Dog.class);
                        if (dog == null)
                            continue;
                        if (userLocation.getUser() == null)
                            continue;
                        if (dog.getUserId().equals(userLocation.getUser().getUserId())) {
                            dog.setLocation(userLocation.getGeo_point());
                            dog.setDistance(0);
                        }
                        else {
//                            dog.setLocation(new GeoPoint(32.8117099,0));
                            double dist = dog.calculateDistance(userLocation.getGeo_point());
                            dog.setDistance(dist);
                        }

//                        updateDogLocation(dog);
                        mDogList.add(dog);
                    }

                    Log.d(TAG, "onEvent: user list size: " + mDogList.size());
                }
                updateDogLocation();
            }
        });

    }

}