package com.syde.myomote.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.syde.myomote.R;
import com.syde.myomote.core.Constants;
import com.syde.myomote.core.Control;
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
    private Button doneButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        control = new Control();
        control.setPose = Pose.THUMB_TO_PINKY;
        control.customPose = "";
        control.deviceName = "HALLO";
        control.signal = "sdfsdfs";

        setContentView(R.layout.create_control_activity);

        if (getIntent() != null && getIntent().getExtras() != null) {
            control = (Control) getIntent().getExtras().getSerializable(Constants.Extra.CONTROL);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        selectDeviceButton = (Button) findViewById(R.id.deviceButton);
        selectDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        assignGestureButton = (Button) findViewById(R.id.assignGesture);
        assignGestureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        recordSignalButton = (Button) findViewById(R.id.recordSignal);
        recordSignalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

}
