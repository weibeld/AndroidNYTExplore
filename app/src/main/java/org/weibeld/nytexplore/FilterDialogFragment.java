package org.weibeld.nytexplore;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.weibeld.nytexplore.databinding.DialogFilterBinding;
import org.weibeld.nytexplore.util.MyDate;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by dw on 23/02/17.
 */

public class FilterDialogFragment extends DialogFragment {

    private static final String TAG_BEGIN_DATE = "DatePickerBeginDate";
    private static final String TAG_END_DATE = "DatePickerEndDate";

    DialogFilterBinding b;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        b = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_filter, null, false);

        setupDateFilter(b.cbBeginDate, b.tvBeginDate, b.etBeginDate, TAG_BEGIN_DATE);
        setupDateFilter(b.cbEndDate, b.tvEndDate, b.etEndDate, TAG_END_DATE);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setView(b.getRoot())
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void showDatePicker(String tag) {
        (new DatePickerDialogFragment()).show(getFragmentManager(), tag);
    }

    private void setupDateFilter(final CheckBox cb, TextView tv, final EditText et, final String tag) {

        // Make EditText non-editable
        et.setKeyListener(null);

        // Set initial visibility of EditText
        if (cb.isChecked())
            et.setVisibility(View.VISIBLE);
        else
            et.setVisibility(View.GONE);

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


    /**
     * Simple data picker dialog.
     */
    public static class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private final String LOG_TAG = DatePickerDialogFragment.class.getSimpleName();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Set date picker to today's date, except if there's already a date set in EditText
            TextView et = getEditText();
            Calendar c = new GregorianCalendar();
            if (et.getTag() != null) c.setTime(((MyDate) et.getTag()).getDate());
            return new DatePickerDialog(getActivity(), this,
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
        }

        // Called when the user selected a date from the date picker dialog
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            // Write date into EditText and associate a MyDate object with the same EditText
            MyDate date = new MyDate(year, month, dayOfMonth);
            TextView et = getEditText();
            et.setText(date.format3());
            et.setTag(date);
        }

        // Called when the user cancelled the date picker dialog
        @Override
        public void onCancel(DialogInterface dialog) {
        }

        private TextView getEditText() {
            FilterDialogFragment f = (FilterDialogFragment) getActivity().getFragmentManager().findFragmentByTag(MainActivity.TAG_FILTER_DIALOG);
            switch (getTag()) {
                case TAG_BEGIN_DATE:
                    return f.b.etBeginDate;
                case TAG_END_DATE:
                    return f.b.etEndDate;
                default:
                    new Exception().printStackTrace();
                    return null;
            }
        }
    }
}
