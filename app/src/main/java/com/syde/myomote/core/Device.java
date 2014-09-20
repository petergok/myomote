package com.syde.myomote.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by pgokhshteyn on 9/20/14.
 */
public class Device implements Serializable {

    public String name;
    public int id;
    public ArrayList<Control> controls;

    public String toString() {
        String string = name + " " + id + " ";
        for (Control control : controls) {
            string += control.toString() + " ";
        }
        return string;
    }

    public static Device parseString (String input) {
        StringTokenizer st = new StringTokenizer(input);
        Device newDevice = new Device();
        newDevice.name = st.nextToken();
        newDevice.id = Integer.parseInt(st.nextToken());
        newDevice.controls = new ArrayList<Control>();
        while (st.hasMoreTokens()) {
            String controlInput = st.nextToken() + " " + st.nextToken();
            newDevice.controls.add(Control.getControl(controlInput));
        }
        return newDevice;
    }

}
