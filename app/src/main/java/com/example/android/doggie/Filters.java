package com.example.android.doggie;

import android.content.Context;
import android.text.TextUtils;

import com.example.android.doggie.models.Dog;
import com.google.firebase.firestore.Query;

/**
 * Object for passing filters around.
 */
public class Filters {

    private String breed = null;
    private String gender = null;
    private String sortBy = null;
    private Query.Direction sortDirection = null;

    private String userId = null;

    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(Dog.FIELD_DISTANCE);
        filters.setSortDirection(Query.Direction.ASCENDING);

        return filters;
    }

    public boolean hasBreed() {
        return !(TextUtils.isEmpty(breed));
    }

    public boolean hasGender() {
        return !(TextUtils.isEmpty(gender));
    }

    public boolean hasSortBy() {
        return !(TextUtils.isEmpty(sortBy));
    }

    public boolean hasUserId() { return !(TextUtils.isEmpty(userId)); }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Query.Direction getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Query.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        if (breed == null && gender == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_dogs));
            desc.append("</b>");
        }

        if (breed != null) {
            desc.append("<b>");
            desc.append(breed);
            desc.append("</b>");
        }

        if (breed != null && gender != null) {
            desc.append(" , ");
        }

        if (gender != null) {
            desc.append("<b>");
            desc.append(gender);
            desc.append("</b>");
        }

        return desc.toString();
    }

    public String getOrderDescription(Context context) {
        if (Dog.FIELD_DISTANCE.equals(sortBy)) {
            return context.getString(R.string.sorted_by_distance);
        } else if (Dog.FIELD_AGE.equals(sortBy)) {
            return context.getString(R.string.sorted_by_age);
        } else {
            return context.getString(R.string.sorted_by_weight);
        }
    }
}

