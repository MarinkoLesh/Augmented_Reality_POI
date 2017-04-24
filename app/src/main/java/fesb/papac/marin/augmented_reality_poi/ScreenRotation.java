package fesb.papac.marin.augmented_reality_poi;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by Marin on 19.4.2017..
 */

/**
 * Simple class to check for screen rotation.
 *
 * If the screen rotation is changed, then the phone x and y axis are changed as well.
 *
 * Our coordinate system uses 3 axis, but the screen of the phone uses only 2. If the phone is parallel to the ground,
 * x and y axis of the phone and the sensor are the same, because the phone then doesn't need to use the z axis.
 * But for AR (augmented reality) applications the phone is vertical to the ground, so we need to change axis.
 * Usually the only axis we need to change is the y axis. If the phone is vertical to the ground,
 * then the y axis of the phone is equal to z axis of the sensor.
 *
 * The second thing we need to changed in this class is the axis if the phone has been rotated.
 * In our application the phone will always be vertical to the ground, so lets put the phone vertical and try to explain
 * what phone rotation means.
 * ROTATION_0   means that the bottom of the phone is closest to the ground
 * ROTATION_90  means that we rotated phone to the left, so the left side of the phone is closest to the ground
 * ROTATION_180 means that the up side of the phone is closest to the ground, so we won't use this rotation
 * ROTATION_270 means that the phone is rotate to the right, so the right side of the phone is closest to the ground
 * Now that we know which rotation means what position of the phone as opposed to the ground now we need to change
 * phones axis with the right sensor axis.
 *
 */

public class ScreenRotation {

    Context context;
    int axisX, axisY;
    int screenRotation;

    public ScreenRotation(Context context) {
        this.context = context;

        WindowManager wm = (WindowManager) context.getSystemService(Activity.WINDOW_SERVICE);
        screenRotation = wm.getDefaultDisplay().getRotation();

        switch (screenRotation) {
            case Surface.ROTATION_0:
                axisX = SensorManager.AXIS_X;
                axisY = SensorManager.AXIS_Z;
                break;
            case Surface.ROTATION_90: // rotation to left
                axisX = SensorManager.AXIS_Z;
                axisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                axisX = SensorManager.AXIS_MINUS_X;
                axisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270: // rotation to right
                axisX = SensorManager.AXIS_MINUS_Z;
                axisY = SensorManager.AXIS_X;
                break;
            default:
                axisX = SensorManager.AXIS_X;
                axisY = SensorManager.AXIS_Z;
                break;
        }
    }

    public int getAxisX() { return axisX;}
    public int getAxisY() { return axisY;}
    public int getScreenRotation() { return screenRotation;}
}
