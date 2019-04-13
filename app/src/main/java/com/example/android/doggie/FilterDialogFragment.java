package com.example.android.doggie;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Spinner;

import com.example.android.doggie.models.Dog;
import com.example.android.doggie.models.User;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Dialog Fragment containing filter form.
 */
public class FilterDialogFragment extends DialogFragment {

    public static final String TAG = "FilterDialog";

    interface FilterListener {

        void onFilter(Filters filters);

    }

    private View mRootView;

    @BindView(R.id.spinner_breed)
    Spinner mBreedSpinner;

    @BindView(R.id.spinner_gender)
    Spinner mGenderSpinner;

    @BindView(R.id.spinner_sort)
    Spinner mSortSpinner;

    @BindView(R.id.checkBox)
    CheckBox mCheckBox;


    private FilterListener mFilterListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);
        ButterKnife.bind(this, mRootView);

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FilterListener) {
            mFilterListener = (FilterListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        if (window == null)
            return;
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.button_search)
    public void onSearchClicked() {
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
        }

        dismiss();
    }

    @OnClick(R.id.button_cancel)
    public void onCancelClicked() {
        dismiss();
    }


    /*
     * If 'my dogs only' is checked returns the userId of current user,
     * otherwise returns null.
     */

    @Nullable
    private String getUsersId() {
        if (mCheckBox.isChecked()) {
            return ((UserClient)(getApplicationContext())).getUser().getUserId();
        }
        return null;
    }

    @Nullable
    private String getSelectedBreed() {
        String selected = (String) mBreedSpinner.getSelectedItem();
        if (getString(R.string.value_any_breed).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedGender() {
        String selected = (String) mGenderSpinner.getSelectedItem();
        if (getString(R.string.value_any_gender).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedSortBy() {
        String selected = (String) mSortSpinner.getSelectedItem();
        if (getString(R.string.sort_by_distance).equals(selected)) {
            return Dog.FIELD_DISTANCE;
        } if (getString(R.string.sort_by_age).equals(selected)) {
            return Dog.FIELD_AGE;
        } if (getString(R.string.sort_by_weight).equals(selected)) {
            return Dog.FIELD_WEIGHT;
        }

        return null;
    }


    @Nullable
    private Query.Direction getSortDirection() {
        return Query.Direction.ASCENDING;
    }

    public void resetFilters() {
        if (mRootView != null) {
            mBreedSpinner.setSelection(0);
            mGenderSpinner.setSelection(0);
            mSortSpinner.setSelection(0);
        }
    }

    public Filters getFilters() {
        Filters filters = new Filters();

        if (mRootView != null) {

            filters.setUserId(getUsersId());
            filters.setBreed(getSelectedBreed());
            filters.setGender(getSelectedGender());
            filters.setSortBy(getSelectedSortBy());
            filters.setSortDirection(getSortDirection());
        }

        return filters;
    }
}
