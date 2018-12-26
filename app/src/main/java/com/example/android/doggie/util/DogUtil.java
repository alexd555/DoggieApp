package com.example.android.doggie.util;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.Toolbar;

import com.example.android.doggie.R;
import com.example.android.doggie.model.Dog;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;

/**
 * Utilities for Restaurants.
 */
public class DogUtil {

    private static final String TAG = "DogUtil";

    private static final String DOG_URL_FMT = "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png";
    private static final int MAX_IMAGE_NUM = 22;

    private static final String[] NAME_WORDS = {
            "Miley",
            "Emma",
            "Bobbie",
            "Maggie",
            "Tony",
            "Max",
    };


    /**
     * Create a random Dog POJO.
     */
    public static Dog getRandom(Context context) {
        Dog dog = new Dog();
        Random random = new Random();

        // Breed (first elemnt is 'Any')
        String[] breeds = context.getResources().getStringArray(R.array.breeds);
        breeds = Arrays.copyOfRange(breeds, 1, breeds.length);

        // Genders (first element is 'Any')
        String[] genders = context.getResources().getStringArray(R.array.genders);
        genders = Arrays.copyOfRange(genders, 1, genders.length);

        int[] ages = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] weights = new int[]{3, 7, 10, 12, 15, 17, 20, 21, 24, 26, 29, 30, 31, 32, 35, 37};
        double[] distances = new double[]{0.1, 0.5, 0.7, 1.2, 2.5, 3.6, 6.9};

//        dog.setPhoto(R.drawable.miley);
//        dog.setName(getRandomName(random));
        dog.setGender(getRandomString(genders, random));
        dog.setAge(getRandomInt(ages, random));
        dog.setBreed(getRandomString(breeds, random));
        dog.setWeight(getRandomInt(weights, random));
        dog.setDistance(getRandomDouble(distances, random));

        return dog;
    }


//    /**
//     * Get a random image.
//     */
//    private static String getRandomImageUrl(Random random) {
//        // Integer between 1 and MAX_IMAGE_NUM (inclusive)
//        int id = random.nextInt(MAX_IMAGE_NUM) + 1;
//
//        return String.format(Locale.getDefault(), DOG_URL_FMT, id);
//    }


    private static String getRandomName(Random random) {
        return getRandomString(NAME_WORDS, random);

    }

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    private static int getRandomInt(int[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    private static double getRandomDouble(double[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

}
