package com.example.android.doggie.models;

import android.location.Location;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.doggie.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;

@IgnoreExtraProperties
public class Dog {
    public static final String FIELD_DISTANCE = "distance";
    public static final String FIELD_GENDER = "gender";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_BREED = "breed";
    public static final String FIELD_WEIGHT = "weight";

    private String userId;
    private String userName;
    private String dogId;
    private String dogName;
    private double weight;
    private String breed;
    private  String image;
    private double distance;
    private String gender;
    private GeoPoint location;
    private int yearOfBirth, monthOfBirth, dayOfBirth;

    public Dog() {}

    public Dog(FirebaseUser user, String dogName, double weight, String breed,
               double distance, String gender, LocalDate dateOfBirth) {
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        if (TextUtils.isEmpty(this.userName)) {
            this.userName = user.getEmail();
        }
        this.dogName = dogName;
        this.weight = weight;
        this.breed = breed;
        this.distance = distance;
        this.gender = gender;

        this.yearOfBirth = dateOfBirth.getYear();
        this.monthOfBirth = dateOfBirth.getMonthOfYear();
        this.dayOfBirth = dateOfBirth.getDayOfMonth();
        Log.d("DOG", dayOfBirth + "/" + monthOfBirth + "/" + yearOfBirth);
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getDogId()
    {
        return dogId;
    }
    public void setDogId(String dogId)
    {
        this.dogId = dogId;
    }
    public String getDogName() {
        return dogName;
    }

    public void setDogName(String dogName) {
        this.dogName = dogName;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getGender() { return gender; }

    public GeoPoint getLocation()
    {
        return location;
    }
    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public void setGender(String gender) { this.gender = gender; }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distanceHelper(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public double calculateDistance(GeoPoint curLocUser)
    {
        if (this.location == null || curLocUser == null) {
            distance = -1;
            return -1;
        }


        distance = distanceHelper(curLocUser.getLatitude(), location.getLatitude(),
                curLocUser.getLongitude(),location.getLongitude(),0,0);

        return distance;
    }

    public int getDayOfBirth() {
        return dayOfBirth;
    }

    public int getMonthOfBirth() {
        return monthOfBirth;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public int getAgeInMonths() {
        LocalDate birthDate = new LocalDate(yearOfBirth, monthOfBirth, dayOfBirth);
        return Months.monthsBetween(birthDate, LocalDate.now()).getMonths();
    }
//    private void getDogLocation(String userId) {
//        DocumentReference locationsRef = FirebaseFirestore.getInstance()
//                .collection("User Locations")
//                .document(userId);
//
//        locationsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                if (task.isSuccessful()) {
//                    if (task.getResult() != null && task.getResult().toObject(UserLocation.class) != null) {
//
//                        UserLocation userLocation = task.getResult().toObject(UserLocation.class);
//                        if (userLocation != null) {
//                            location = userLocation.getGeo_point();
//                            setLocation(location);
//                        }
//                    }
//                }
//            }
//        });
//    }
}
