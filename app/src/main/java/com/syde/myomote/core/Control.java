package com.syde.myomote.core;

import com.thalmic.myo.Pose;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Created by pgokhshteyn on 9/20/14.
 */
public class Control implements Serializable {

    public static String[] customPoses = {"gunShot"};

    public Pose setPose;
    public String customPose;
    public String signal;
    public String deviceName;
    public String name;

    public String toString() {
        String string = signal;
        if (customPose != null && !customPose.isEmpty()) {
            string += " " + customPose;
        } else {
            string += " " + setPose.name();
        }
        string += " " + name;
        return string;
    }

    public static Control getControl(String input) {
        Control newControl = new Control();
        StringTokenizer st = new StringTokenizer(input);
        newControl.signal = st.nextToken();

        String pose = st.nextToken();
        for (String defPose : customPoses) {
            if (pose.equals(defPose)) {
                newControl.customPose = pose;
                newControl.name = st.nextToken();
                return newControl;
            }
        }

        newControl.customPose = "";
        newControl.setPose = Pose.valueOf(pose);
        newControl.name = st.nextToken();
        return newControl;
    }
}
