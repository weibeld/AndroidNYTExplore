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
import org.weibeld.nytexplore.activity.MainActivity;
import org.weibeld.nytexplore.databinding.DialogFilterBinding;
import org.weibeld.nytexplore.util.MyDate;
import org.weibeld.nytexplore.util.Util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by dw on 23/02/17.
 */

public class FilterDialogFragment extends DialogFragment {

    public static final String TAG_BEGIN_DATE = "DatePickerBeginDate";
    public static final String TAG_END_DATE = "DatePickerEndDate";

    DialogFilterBinding b;
    SharedPreferences mPref;
    ArrayList<CheckBox> mNewsDeskCheckboxes;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        b = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_filter, null, false);

        setupBeginDate();
        setupEndDate();
        setupSortOrder();
        setupNewsDesk();

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(b.getRoot())
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (dialog1, id) -> {}).create();

        // Override setPositiveButton in order to prevent automatic dismissal of dialog
        dialog.setOnShowListener(d -> {
            Button button = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isBeginDateInputValid()) {
                        makeVisible(b.etBeginDate);
                        b.etBeginDate.requestFocus();
                        Util.toast(getActivity(), "Please select a begin date");
                        return;
                    }
                    if (!isEndDateInputValid()) {
                        makeVisible(b.etEndDate);
                        b.etEndDate.requestFocus();
                        Util.toast(getActivity(), "Please select an end date");
                        return;
                    }
                    if (!isSortOrderInputValid()) {
                        makeVisible(b.rgSortOrder);
                        b.rgSortOrder.requestFocus();
                        Util.toast(getActivity(), "Please select a sort order");
                        return;
                    }
                    if (!isNewsDeskInputValid()) {
                        makeVisible(b.newsDeskContainer);
                        b.newsDeskContainer.requestFocus();
                        Util.toast(getActivity(), "Please select at least one category");
                        return;
                    }

                    // Save the current value for each filter in the SharedPreferences
                    SharedPreferences.Editor e = mPref.edit();
                    writeBeginDateValue(e);
                    writeEndDateValue(e);
                    writeSortOrderValue(e);
                    writeNewsDeskValue(e);
                    e.apply();

                    // Tint icon of the "Filter" menu item if and only if any filters are set
                    MainActivity a = (MainActivity) getActivity();
                    if (a.isAnyFilterSet()) a.tintFilterIcon(true);
                    else a.tintFilterIcon(false);

                    dialog.dismiss();
                }

                private boolean isBeginDateInputValid() {
                    return !(b.cbBeginDate.isChecked() && b.etBeginDate.getTag() == null);
                }
                private boolean isEndDateInputValid() {
                    return !(b.cbEndDate.isChecked() && b.etEndDate.getTag() == null);
                }
                private boolean isSortOrderInputValid() {
                    return !(b.cbSortOrder.isChecked() && !b.rbSortOrderNewest.isChecked() && !b.rbSortOrderOldest.isChecked());
                }
                private boolean isNewsDeskInputValid() {
                    if (b.cbNewsDesk.isChecked()) {
                        for (CheckBox cb : mNewsDeskCheckboxes)
                            if (cb.isChecked()) return true;
                        return false;
                    }
                    return true;
                }
                
                private void writeBeginDateValue(SharedPreferences.Editor e) {
                    String value = "";
                    if (b.cbBeginDate.isChecked())
                        value = ((MyDate) b.etBeginDate.getTag()).format4();
                    e.putString(getString(R.string.pref_begin_date), value);
                }
                private void writeEndDateValue(SharedPreferences.Editor e) {
                    String value = "";
                    if (b.cbEndDate.isChecked())
                        value = ((MyDate) b.etEndDate.getTag()).format4();
                    e.putString(getString(R.string.pref_end_date), value);
                }
                private void writeSortOrderValue(SharedPreferences.Editor e) {
                    String value = "";
                    if (b.cbSortOrder.isChecked()) {
                        int id = b.rgSortOrder.getCheckedRadioButtonId();
                        value = b.rgSortOrder.findViewById(id).getTag().toString();
                    }
                    e.putString(getString(R.string.pref_sort_order), value);
                }
                private void writeNewsDeskValue(SharedPreferences.Editor e) {
                    String value = "";
                    if (b.cbNewsDesk.isChecked()) {
                        Iterator itr = mNewsDeskCheckboxes.iterator();
                        while (itr.hasNext()) {
                            CheckBox cb = (CheckBox) itr.next();
                            if (cb.isChecked()) {
                                value += cb.getTag();
                                if (itr.hasNext()) value += ":";
                            }
                        }
                    }
                    e.putString(getString(R.string.pref_news_desk), value);
                }
            });
        });

        // Return the AlertDialog
        return dialog;
    }

    /*--------------------------------------------------------------------------------------------*
     * Shorthand methods
     *--------------------------------------------------------------------------------------------*/
    private void setupBeginDate() {
        // Make EditText non-editable
        b.etBeginDate.setKeyListener(null);
        // Read current value for this filter from SharedPreferences
        String val = mPref.getString(getString(R.string.pref_begin_date), "");
        setInitialState(b.cbBeginDate, b.etBeginDate, val, this::setBeginDateInput);
        setCheckBoxListener(b.cbBeginDate, b.etBeginDate, this::showDatePickerBeginDateCond, this::clearBeginDateInput);
        setTitleListener(b.tvBeginDate, b.cbBeginDate, b.etBeginDate);
        setCompoundDrawableListener(b.etBeginDate, this::showDatePickerBeginDate);
    }

    private void setupEndDate() {
        // Make EditText non-editable
        b.etEndDate.setKeyListener(null);
        // Read current value for this filter from SharedPreferences
        String val = mPref.getString(getString(R.string.pref_end_date), "");
        setInitialState(b.cbEndDate, b.etEndDate, val, this::setEndDateInput);
        setCheckBoxListener(b.cbEndDate, b.etEndDate, this::showDatePickerEndDateCond, this::clearEndDateInput);
        setTitleListener(b.tvEndDate, b.cbEndDate, b.etEndDate);
        setCompoundDrawableListener(b.etEndDate, this::showDatePickerEndDate);
    }

    private void setupSortOrder() {
        // Associate a value with each radio button (value for the begin_date end_date API param)
        b.rbSortOrderNewest.setTag("newest");
        b.rbSortOrderOldest.setTag("oldest");

        // Read currently saved value from SharedPreferences
        String val = mPref.getString(getString(R.string.pref_sort_order), "");

        // Set initial state of widgets
        setInitialState(b.cbSortOrder, b.rgSortOrder, val, this::setSortOrderInput);

        setCheckBoxListener(b.cbSortOrder, b.rgSortOrder, null, this::clearSortOrderInput);
        setTitleListener(b.tvSortOrder, b.cbSortOrder, b.rgSortOrder);
        // Check checkbox if any of the radio buttons is selected
        b.rbSortOrderNewest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !b.cbSortOrder.isChecked()) b.cbSortOrder.setChecked(true);
        });
        b.rbSortOrderOldest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !b.cbSortOrder.isChecked()) b.cbSortOrder.setChecked(true);
        });
    }

    private void setupNewsDesk() {
        // Add a checkbox for each news_desk category to the LinearLayout container
        mNewsDeskCheckboxes = new ArrayList<>();
        String[] a = getResources().getStringArray(R.array.news_desk_values);
        for (int i = 0; i < a.length; i++) {
            CheckBox cb = new CheckBox(getActivity());
            cb.setText(a[i]);
            cb.setTag(a[i]);
            b.newsDeskContainer.addView(cb);
            mNewsDeskCheckboxes.add(cb);
        }

        // Read currently saved value (colon-separated string of categories)
        String val = mPref.getString(getString(R.string.pref_news_desk), "");

        setInitialState(b.cbNewsDesk, b.newsDeskContainer, val, this::setNewsDeskInput);

        // Set up OnCheckedChangeListener for check box
        setCheckBoxListener(b.cbNewsDesk, b.newsDeskContainer, null, this::clearNewsDeskInput);
        // Set up OnClickListener for title TextView
        setTitleListener(b.tvNewsDesk, b.cbNewsDesk, b.newsDeskContainer);
        // Check filter checkbox if any of the category check boxes is checked
        for (CheckBox cb : mNewsDeskCheckboxes) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !b.cbNewsDesk.isChecked()) b.cbNewsDesk.setChecked(true);
            });
        }
    }



    
    /*--------------------------------------------------------------------------------------------*
     * Listener setup methods
     *--------------------------------------------------------------------------------------------*/
    
    private void setInitialState(CheckBox cb, View v, String value, MyFunc1<String> runIfFilterActive) {
        if (value.isEmpty()) {
            cb.setChecked(false);
            v.setVisibility(View.GONE);
        }
        else {
            cb.setChecked(true);
            v.setVisibility(View.VISIBLE);
            runIfFilterActive.run(value);
        }
    }

    private void setCheckBoxListener(CheckBox cb, View v, MyFunc0 onCheck, MyFunc0 onUncheck) {
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                makeVisible(v);
                v.requestFocus();
                if (onCheck != null) onCheck.run();
            }
            else {
                if (onUncheck != null) onUncheck.run();
            }
        });
    }

    private void setTitleListener(TextView tv, CheckBox cb, View v) {
        tv.setOnClickListener(view -> {
            toggleVisibility(v);
            if (v.getVisibility() == View.VISIBLE) {
                v.requestFocus();
            }
        });
    }

    private void setCompoundDrawableListener(EditText et, MyFunc0 onClick) {
        et.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int textFieldWidth = et.getWidth();
                int iconWidth = et.getCompoundDrawables()[2].getBounds().width();
                if (event.getX() >= textFieldWidth - iconWidth) {
                    et.requestFocus();
                    onClick.run();
                }
            }
            return false;
        });
    }

    
    /*--------------------------------------------------------------------------------------------*
     * Functional interface methods
     *--------------------------------------------------------------------------------------------*/
    
    private void clearBeginDateInput() {
        b.etBeginDate.setTag(null);
        b.etBeginDate.setText("");
    }
    private void clearEndDateInput() {
        b.etEndDate.setTag(null);
        b.etEndDate.setText("");
    }
    private void clearSortOrderInput() {
        b.rbSortOrderOldest.setChecked(false);
        b.rbSortOrderNewest.setChecked(false);
    }
    private void clearNewsDeskInput() {
        for (CheckBox cb : mNewsDeskCheckboxes) {
            cb.setChecked(false);
        }
    }
    private void setBeginDateInput(String value) {
        MyDate date = new MyDate(value);
        b.etBeginDate.setText(date.format3());
        b.etBeginDate.setTag(date);
    }
    private void setEndDateInput(String value) {
        MyDate date = new MyDate(value);
        b.etEndDate.setText(date.format3());
        b.etEndDate.setTag(date);
    }
    private void setSortOrderInput(String value) {
        b.rbSortOrderNewest.setChecked(value.equals(b.rbSortOrderNewest.getTag()));
        b.rbSortOrderOldest.setChecked(value.equals(b.rbSortOrderOldest.getTag()));
    }
    private void setNewsDeskInput(String value) {
        String[] categories = value.split(":");
        for (String category : categories) {
            CheckBox cb = (CheckBox) b.newsDeskContainer.findViewWithTag(category);
            cb.setChecked(true);
        }
    }
    private void showDatePickerBeginDateCond() {
        if (b.etBeginDate.getTag() == null) showDatePickerBeginDate();
    }
    private void showDatePickerEndDateCond() {
        if (b.etEndDate.getTag() == null) showDatePickerEndDate();
    }
    private void showDatePickerBeginDate() {
            (new DatePickerDialogFragment()).show(getFragmentManager(), TAG_BEGIN_DATE);
    }
    private void showDatePickerEndDate() {
            (new DatePickerDialogFragment()).show(getFragmentManager(), TAG_END_DATE);
    }

    
    /*--------------------------------------------------------------------------------------------*
     * Functional interfaces
     *--------------------------------------------------------------------------------------------*/
    
    @FunctionalInterface
    interface MyFunc0 {
        void run();
    }
    @FunctionalInterface
    interface MyFunc1<T> {
        void run(T t);
    }

    
    /*--------------------------------------------------------------------------------------------*
     * Utility methods
     *--------------------------------------------------------------------------------------------*/
    
    private void toggleVisibility(View v) {
        if (v.getVisibility() == View.VISIBLE)
            v.setVisibility(View.GONE);
        else if (v.getVisibility() == View.GONE)
            v.setVisibility(View.VISIBLE);
    }

    private void makeVisible(View v) {
        if (v.getVisibility() == View.GONE) v.setVisibility(View.VISIBLE);
    }
}
