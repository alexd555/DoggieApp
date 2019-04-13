package com.example.android.doggie.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import com.example.android.doggie.R;
import com.example.android.doggie.models.Dog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.joda.time.LocalDate;
import org.joda.time.Months;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * RecyclerView adapter for a list of Dogs.
 */
public class DogAdapter extends FirestoreAdapter<DogAdapter.ViewHolder> {

    public interface OnDogSelectedListener {

        void onDogSelected(DocumentSnapshot dog);

    }

    private OnDogSelectedListener mListener;

    public DogAdapter(Query query, OnDogSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_dog, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.dog_item_image)
        ImageView imageView;

        @BindView(R.id.dog_item_name)
        TextView nameView;

        @BindView(R.id.dog_item_gender)
        TextView genderView;

        @BindView(R.id.dog_item_age)
        TextView ageView;

        @BindView(R.id.dog_item_distance)
        TextView distanceView;

        @BindView(R.id.breed_item_category)
        TextView breedView;

        @BindView(R.id.dog_item_weight)
        TextView weightView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnDogSelectedListener listener) {

            Dog dog = snapshot.toObject(Dog.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(imageView.getContext()).load(dog.getImage()).into(imageView);

            nameView.setText(dog.getDogName());
            genderView.setText(dog.getGender());


            int year, month, day;
            year = dog.getYearOfBirth();
            month = dog.getMonthOfBirth();
            day = dog.getDayOfBirth();

            Log.d("DOGDATE", dog.getDogName() + ":" + day +"/"+month+"/"+year);


            int months = dog.getAgeInMonths();
            String ageOfDog;
            if (months < 12) {
                ageOfDog = months + " months old";
            } else {
                ageOfDog = months/12 + " years old";
            }
            ageView.setText(ageOfDog);


            distanceView.setText(resources.getString(R.string.fmt_distance,
                    new Object[]{dog.getDistance()}));
            breedView.setText(dog.getBreed());
            weightView.setText(resources.getString(R.string.fmt_weight, dog.getWeight()));


            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onDogSelected(snapshot);
                    }
                }
            });
        }

    }
}
