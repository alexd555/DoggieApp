package com.example.android.doggie.models;

import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Dog {//implements Parcelable {
    public static final String FIELD_DISTANCE = "distance";
    public static final String FIELD_GENDER = "gender";
    public static final String FIELD_AGE = "age";
    public static final String FIELD_BREED = "breed";
    public static final String FIELD_WEIGHT = "weight";

    private String userId;
    private String userName;
    private String dogName;
    private int weight;
    private String breed;
    private  String image;
//    private StorageReference imageRef;
    private double distance;
    private int age;
    private String gender;
    private double latitude;
    private double longitude;
    public Dog() {}

    public Dog(FirebaseUser user, String dogName, int weight, String breed,
               double distance, int age, String gender) {
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        if (TextUtils.isEmpty(this.userName)) {
            this.userName = user.getEmail();
        }

        this.dogName = dogName;
        this.weight = weight;
        this.breed = breed;
        this.distance = distance;
        this.age = age;
        this.gender = gender;
        this.latitude = latitude;
        this.longitude = longitude;
    }
//    protected Dog(Parcel in) {
//        this.dogName = dogName;
//        this.weight = weight;
//        this.breed = breed;
//        this.distance = distance;
//        this.age = age;
//        this.gender = gender;
//        this.latitude = latitude;
//        this.longitude = longitude;
//    }
//
//    public static final Creator<Dog> CREATOR = new Creator<Dog>() {
//        @Override
//        public Dog createFromParcel(Parcel in) {
//            return new Dog(in);
//        }
//
//        @Override
//        public User[] newArray(int size) {
//            return new User[size];
//        }
//    };
//public static Creator<User> getCREATOR() {
//    return CREATOR;
//}

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDogName() {
        return dogName;
    }

    public void setDogName(String dogName) {
        this.dogName = dogName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
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

    //    public StorageReference getImageRef() { return imageRef; }

//    public void setImageRef(StorageReference imageRef) { this.imageRef = imageRef; }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }

    public void setGender(String gender) { this.gender = gender; }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
////        dest.writeParcelable(user, flags);
//    }
}
