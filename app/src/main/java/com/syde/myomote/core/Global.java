package com.syde.myomote.core;

import com.syde.myomote.ui.CreateControlActivity;
import com.syde.myomote.ui.MainActivity;

import java.util.ArrayList;

/**
 * Created by pgokhshteyn on 9/20/14.
 */
public class Global {
    public static String DEVICES = "devices";
    public static String NUM_DEVICES = "numDevices";
    public static String PREFERENCES = "Preferences";
    public static String lastSignal;
    public static CreateControlActivity createControlActivity;
    public static ArrayList<Device> currentDevices;
    public static MainActivity mainActivity;
}
