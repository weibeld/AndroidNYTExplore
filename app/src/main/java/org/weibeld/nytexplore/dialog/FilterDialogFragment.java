package org.weibeld.nytexplore.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_filter, null, false);

        setupDateFilter(b.cbBeginDate, b.tvBeginDate, b.etBeginDate, TAG_BEGIN_DATE, getString(R.string.pref_key_begin_date));
        setupDateFilter(b.cbEndDate, b.tvEndDate, b.etEndDate, TAG_END_DATE, getString(R.string.pref_key_end_date));

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(b.getRoot())
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                }).create();

        // Override the setPositiveButton method in order to prevent automatic dismissal of dialog
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button button = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Read all values and save in SharedPreferences
                        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor e = pref.edit();

                        if (!saveDate(b.cbBeginDate, b.etBeginDate, getString(R.string.pref_key_begin_date), e))
                            return;

                        if (!saveDate(b.cbEndDate, b.etEndDate, getString(R.string.pref_key_end_date), e))
                            return;

                        dialog.dismiss();
                    }

                    // Save begin or end date as a string in the SharedPreferences (format YYYYMMDD
                    // or empty string if this filter is not set).
                    private boolean saveDate(CheckBox cb, EditText et, String prefKey, SharedPreferences.Editor prefEdit) {
                        if (cb.isChecked()) {
                            if (et.getTag() == null) {
                                et.requestFocus();
                                Util.toast(getActivity(), "Please select a date.");
                                return false;
                            }
                            MyDate beginDate = (MyDate) et.getTag();
                            prefEdit.putString(prefKey, beginDate.format4());
                        }
                        else {
                            prefEdit.putString(prefKey, "");
                        }
                        prefEdit.apply();
                        return true;
                    }
                });
            }
        });

        // Create the AlertDialog object and return it
        return dialog;
    }

    private void setupDateFilter(final CheckBox cb, TextView tv, final EditText et, final String tag, String prefKey) {

        // Make EditText non-editable
        et.setKeyListener(null);

        // Read current value for this filter from SharedPreferences
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String val = pref.getString(prefKey, "");

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
        et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int textFieldWidth = et.getWidth();
                    int iconWidth = et.getCompoundDrawables()[2].getBounds().width();
                    if (event.getX() >= textFieldWidth - iconWidth) {
                        et.requestFocus();
                        showDatePicker(tag);
                    }
                }
                return false;
            }
        });

        // Set up check box listener, show EditText if checked
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
            }
        });

        // Check/uncheck checkbox when title to the right of it is clicked
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.toggle();
            }
        });
    }

    private void showDatePicker(String tag) {
        (new DatePickerDialogFragment()).show(getFragmentManager(), tag);
    }
}
