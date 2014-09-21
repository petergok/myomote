package com.syde.myomote.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.syde.myomote.R;
import com.syde.myomote.core.Constants;
import com.syde.myomote.core.Control;
import com.syde.myomote.core.Global;
import com.syde.myomote.core.News;
import com.thalmic.myo.Pose;

import static com.syde.myomote.core.Constants.Extra.NEWS_ITEM;

/**
 * Created by pgokhshteyn on 9/20/14.
 */
public class CreateControlActivity extends BootstrapActivity {

    private Control control;

    private Button selectDeviceButton;
    private Button assignGestureButton;
    private Button recordSignalButton;
    private Button setNameButton;
    private Button doneButton;

    private TextView gestureText;
    private TextView signalText;
    private TextView nameText;

    private ProgressDialog mProgressDialog;

    private Context context;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        control = new Control();

        setContentView(R.layout.create_control_activity);

        if (getIntent() != null && getIntent().getExtras() != null) {
            control = (Control) getIntent().getExtras().getSerializable(Constants.Extra.CONTROL);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        gestureText = (TextView) findViewById(R.id.gestureText);
        signalText = (TextView) findViewById(R.id.signalText);
        nameText = (TextView) findViewById(R.id.nameText);

        setNameButton = (Button) findViewById(R.id.setName);
        setNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the edit text view and add it to the dialog
                LayoutInflater inflater = getLayoutInflater();
                final EditText input = (EditText)(inflater.inflate(R.layout.edit_text, null).findViewById(R.id.edit_text));
                AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
                AlertDialog otherDialog = builder.setCancelable(false)
                        .setTitle("Enter a Display Name:")
                        .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // When the user finishes, update the shared preferences with the new name
                                if (input.getText() != null && !TextUtils.isEmpty(input.getText().toString())) {
                                    control.name = input.getText().toString();
                                    nameText.setVisibility(View.VISIBLE);
                                    nameText.setText("Control name set to: " + control.name);
                                }
                            }
                        }).create();
                otherDialog.setView(input);
                otherDialog.show();
            }
        });

        selectDeviceButton = (Button) findViewById(R.id.deviceButton);
        selectDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("Would you like to create a new device?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Inflate the edit text view and add it to the dialog
                        LayoutInflater inflater = getLayoutInflater();
                        final EditText input = (EditText)(inflater.inflate(R.layout.edit_text, null).findViewById(R.id.edit_text));
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
                        AlertDialog otherDialog = builder.setCancelable(false)
                                .setTitle("Enter a Display Name:")
                                .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        // When the user finishes, update the shared preferences with the new name
                                        if (input.getText() != null && !TextUtils.isEmpty(input.getText().toString())) {
                                            control.deviceName = input.getText().toString();
                                            selectDeviceButton.setText(control.deviceName);
                                        }
                                    }
                                }).create();
                        otherDialog.setView(input);
                        otherDialog.show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View gestureSelect = inflater.inflate(R.layout.gesture_select, null);

                        Spinner spinner = (Spinner) gestureSelect.findViewById(R.id.spinner_gesture);

                        Button button = (Button) gestureSelect.findViewById(R.id.done_button);

                        final String [] values = new String[Global.currentDevices.size()];
                        for (int device = 0; device < values.length; device++) {
                            values[device] = Global.currentDevices.get(device).name;
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, values);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                        spinner.setAdapter(adapter);

                        builder.setView(gestureSelect);
                        builder.setTitle("Select a Gesture:");
                        builder.setCancelable(false);

                        final AlertDialog otherDialog = builder.create();

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                otherDialog.dismiss();
                            }
                        });

                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                control.deviceName = values[position];
                                selectDeviceButton.setText(control.deviceName);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        otherDialog.show();
                    }
                }).show();
            }
        });

        assignGestureButton = (Button) findViewById(R.id.assignGesture);
        assignGestureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View gestureSelect = inflater.inflate(R.layout.gesture_select, null);

                Spinner spinner = (Spinner) gestureSelect.findViewById(R.id.spinner_gesture);

                Button button = (Button) gestureSelect.findViewById(R.id.done_button);

                final String [] values = new String[8];
                values[0] = "Gun Pose";
                values[1] = "Roll Right";
                values[2] = "Roll Left";
                values[3] = Pose.FINGERS_SPREAD.name();
                values[4] = Pose.FIST.name();
                values[5] = Pose.THUMB_TO_PINKY.name();
                values[6] = Pose.WAVE_IN.name();
                values[7] = Pose.WAVE_OUT.name();

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, values);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                spinner.setAdapter(adapter);

                builder.setView(gestureSelect);
                builder.setTitle("Select a Gesture:");
                builder.setCancelable(false);

                final AlertDialog dialog = builder.create();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        gestureText.setVisibility(View.VISIBLE);
                    }
                });

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position < 3) {
                            control.customPose = Control.customPoses[position];
                            control.setPose = null;
                            if (position == 0)
                                gestureText.setText("Gesture set to custom gesture Gun Pose");
                            if (position == 1)
                                gestureText.setText("Gesture set to custom gesture Roll Right");
                            if (position == 2)
                                gestureText.setText("Gesture set to custom gesture Roll Left");
                        } else {
                            control.setPose = Pose.valueOf(values[position]);
                            control.customPose = "";
                            gestureText.setText("Gesture set to " + control.setPose.name());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                dialog.show();
            }
        });

        recordSignalButton = (Button) findViewById(R.id.recordSignal);
        recordSignalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProgressDialog == null || !mProgressDialog.isShowing()) {
                    Global.mainActivity.sendMessage("" + -1);
                    mProgressDialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_DARK);

                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setMessage("Waiting for input signal");
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                }
            }
        });

        doneButton = (Button) findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", control);
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
    }

    public void receivedSignal(String signal) {
        control.signal = signal;
        signalText.setVisibility(View.VISIBLE);
        signalText.setText("Signal has been set");
        closeProgressDialog();
    }

    public void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

}
