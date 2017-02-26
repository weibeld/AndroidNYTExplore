package org.weibeld.nytexplore.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.weibeld.nytexplore.R;
import org.weibeld.nytexplore.databinding.DialogFilterBinding;
import org.weibeld.nytexplore.util.MyDate;
import org.weibeld.nytexplore.util.Util;

/**
 * Created by dw on 23/02/17.
 */

public class FilterDialogFragment extends DialogFragment {

    public static final String TAG_BEGIN_DATE = "DatePickerBeginDate";
    public static final String TAG_END_DATE = "DatePickerEndDate";

    DialogFilterBinding b;
    SharedPreferences mPref;

    // TODO: add filters "sort order" (oldest|newest|none) and "news desk" (multi-selection)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        b = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_filter, null, false);

        setupBeginEndDate(b.cbBeginDate, b.tvBeginDate, b.etBeginDate, TAG_BEGIN_DATE, getString(R.string.pref_key_begin_date));
        setupBeginEndDate(b.cbEndDate, b.tvEndDate, b.etEndDate, TAG_END_DATE, getString(R.string.pref_key_end_date));

        setupSortOrder();

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(b.getRoot())
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (dialog1, id) -> {}).create();

        // Override the setPositiveButton method in order to prevent automatic dismissal of dialog
        dialog.setOnShowListener(d -> {
            Button button = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(new View.OnClickListener() {

                SharedPreferences.Editor mPrefEdit;
                @Override
                public void onClick(View view) {
                    // Read all values and save in SharedPreferences
                    mPrefEdit = mPref.edit();

                    if (!saveDate(b.cbBeginDate, b.etBeginDate, getString(R.string.pref_key_begin_date)))
                        return;

                    if (!saveDate(b.cbEndDate, b.etEndDate, getString(R.string.pref_key_end_date)))
                        return;

                    if (!saveSortOrder())
                        return;

                    dialog.dismiss();
                }

                // Save begin or end date as a string in the SharedPreferences (format YYYYMMDD
                // or empty string if this filter is not set).
                private boolean saveDate(CheckBox cb, EditText et, String prefKey) {
                    if (cb.isChecked()) {
                        if (et.getTag() == null) {
                            et.requestFocus();
                            Util.toast(getActivity(), "Please select a date.");
                            return false;
                        }
                        MyDate beginDate = (MyDate) et.getTag();
                        mPrefEdit.putString(prefKey, beginDate.format4());
                    }
                    else {
                        mPrefEdit.putString(prefKey, "");
                    }
                    mPrefEdit.apply();
                    return true;
                }

                private boolean saveSortOrder() {
                    if (b.cbSortOrder.isChecked()) {
                        if (!b.rbSortOrderNewest.isChecked() && !b.rbSortOrderOldest.isChecked()) {
                            b.rgSortOrder.requestFocus();
                            Util.toast(getActivity(), "Please select a sort order.");
                            return false;
                        }
                        String val = "";
                        if (b.rbSortOrderNewest.isChecked())
                            val = b.rbSortOrderNewest.getTag().toString();
                        else if(b.rbSortOrderOldest.isChecked())
                            val = b.rbSortOrderOldest.getTag().toString();
                        mPrefEdit.putString(getString(R.string.pref_key_sort_order), val);
                    }
                    else {
                        mPrefEdit.putString(getString(R.string.pref_key_sort_order), "");
                    }
                    mPrefEdit.apply();
                    return true;
                }
            });
        });

        // Create the AlertDialog object and return it
        return dialog;
    }

    private void setupSortOrder() {

        // Associate a value with each radio button (the value used for the API call)
        b.rbSortOrderNewest.setTag("newest");
        b.rbSortOrderOldest.setTag("oldest");

        // Read current value for this filter from SharedPreferences
        String val = mPref.getString(getString(R.string.pref_key_sort_order), "");

        // Set initial state of CheckBox
        if (val.isEmpty()) {
            b.cbSortOrder.setChecked(false);
            b.rgSortOrder.setVisibility(View.GONE);
        }
        else {
            b.cbSortOrder.setChecked(true);
            b.rgSortOrder.setVisibility(View.VISIBLE);
            b.rbSortOrderNewest.setChecked(val.equals(b.rbSortOrderNewest.getTag()));
            b.rbSortOrderOldest.setChecked(val.equals(b.rbSortOrderOldest.getTag()));
        }

        // Set up check box listener, show RadioGroup if checked
        b.cbSortOrder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                b.rgSortOrder.setVisibility(View.VISIBLE);
                b.rgSortOrder.requestFocus();
            }
            else {
                b.rgSortOrder.setVisibility(View.GONE);
                b.rbSortOrderOldest.setChecked(false);
                b.rbSortOrderNewest.setChecked(false);
            }
        });

        // Check/uncheck checkbox when title to the right of it is clicked
        b.tvSortOrder.setOnClickListener(v -> b.cbSortOrder.toggle());

    }

    private void setupBeginEndDate(final CheckBox cb, TextView tv, final EditText et, final String tag, String prefKey) {

        // Make EditText non-editable
        et.setKeyListener(null);

        // Read current value for this filter from SharedPreferences
        String val = mPref.getString(prefKey, "");

        // Set initial states of CheckBox and EditText
        if (val.isEmpty()) {
            cb.setChecked(false);
            et.setVisibility(View.GONE);
        }
        else {
            cb.setChecked(true);
            et.setVisibility(View.VISIBLE);
            MyDate date = new MyDate(val);
            et.setText(date.format3());
            et.setTag(date);
        }

        // Detect clicks on the right compound drawable of EditText. If clicked, show date picker
        et.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int textFieldWidth = et.getWidth();
                int iconWidth = et.getCompoundDrawables()[2].getBounds().width();
                if (event.getX() >= textFieldWidth - iconWidth) {
                    et.requestFocus();
                    showDatePicker(tag);
                }
            }
            return false;
        });

        // Set up check box listener, show EditText if checked
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showDatePicker(tag);
                et.setVisibility(View.VISIBLE);
                et.requestFocus();

            }
            else {
                // Make sure the EditText ha no date associated anymore
                et.setTag(null);
                et.setText("");
                et.setVisibility(View.GONE);
            }
        });

        // Check/uncheck checkbox when title to the right of it is clicked
        tv.setOnClickListener(v -> cb.toggle());
    }

    private void showDatePicker(String tag) {
        (new DatePickerDialogFragment()).show(getFragmentManager(), tag);
    }
}
