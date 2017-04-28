package fesb.papac.marin.augmented_reality_poi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marin on 19.4.2017..
 */

public class ViewMain extends View implements SensorEventListener,
        LocationListener {

    double canvasWidth,canvasHeight;

    public static PointOfInterest[] pointOfInterests;

    public static final String DEBUG_TAG = "OverlayView Log";

    private final Context context;
    private Handler handler;

    /**
     * Here i put my POI (point of interest).
     * First value is the LATITUDE
     * Second value is the LONGITUDE
     * Third value is the ALTITUDE (this value is never used, and for now it's random)
     * Forth value is the NAME OF PLACE
     *
     */

    private LocationManager locationManager = null;
    private SensorManager sensors = null;

    /**
     * These variables are used to get current location and to get current sensor readings
     */
    private Location lastLocation;
    private float[] lastAccelerometer,lastGyroscope, lastCompass, lastRotationVector, lastGameRotationVector, lastGravity, lastLinearAcc;
    double endLat, endLong, endAlti;
    boolean gps_enabled = false;
    boolean network_enabled = false;

    float textHeight, textOffset;
    int textlength, POIHight;

    private float verticalFOV, horizontalFOV;

    private boolean isAccelAvailable, isCompassAvailable, isGyroAvailable, isRotateVectorAvailable, isGameRotationVectorAvailable, isGravityAvailable, isLinearAccAvailable;

    private Sensor accelSensor, compassSensor, gyroSensor, RotateVectorSensor, GameRotationVector, Gravity, LinearAcc;

    private TextPaint contentPaint, textPaint;
    private Paint targetPaint, roundRec, borderRec;

    private int axisX,axisY, screenRot;

    PaintUtils paintUtilities = new PaintUtils(this);

    float rotation[] = new float[9];
    float identity[] = new float[9];

    float cameraRotation[] = new float[9];

    List<Float> listOfBearingTo = new ArrayList<>();

    float orientation[] = new float[3];
    float orientationAplha [] = new float[3];

    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.poismaller);

    PopupWindow popupWindow;

    public ViewMain(Context context) {
        super(context);
        this.context = context;
        this.handler = new Handler();

        locationManager = (LocationManager) context .getSystemService(Context.LOCATION_SERVICE);

        sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        RotateVectorSensor = sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        GameRotationVector = sensors.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        Gravity = sensors.getDefaultSensor(Sensor.TYPE_GRAVITY);
        LinearAcc = sensors.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        /**
         * Here i call ScreenRotation class and check the screen rotation. If the screen is rotated then the
         * sensor axis need to be changed as well. For better explanation check ScreenRotation class.
         */
        ScreenRotation screenRotation = new ScreenRotation(context);
        screenRot = screenRotation.getScreenRotation();
        axisX = screenRotation.getAxisX();
        axisY = screenRotation.getAxisY();


        startSensors();
        startGPS();

        /**
         *  This is used to get field of view of the camera
         */
        Camera camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        verticalFOV = params.getVerticalViewAngle();
        horizontalFOV = params.getHorizontalViewAngle();
        camera.release();

        /**
         * This is to get paint settings i need
         */
        contentPaint = paintUtilities.getContentPaint();
        targetPaint = paintUtilities.getTargetPaint();
        textPaint = paintUtilities.getTextPaint();
        roundRec = paintUtilities.getRoundRec();
        borderRec = paintUtilities.getBorderRec();

        String json= null;
        try {
            InputStream inputStream = context.getAssets().open("poi_json.txt");
            int size = inputStream.available();
            byte [] buffer = new byte [size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");

        }
        catch (IOException ex){
            ex.printStackTrace();
        }

        pointOfInterests = new Gson().fromJson(json, PointOfInterest[].class );

    }


    private void startSensors() {
        /**
         * In here i can changed the delay of the sensors. The delay is not the same for every phone.
         * It depends on the sensors. The sensor we are using now is the RotateVectorSensor which uses
         * accelerometer, gyroscope and magnetometer.
         */
        isAccelAvailable = sensors.registerListener(this, accelSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        isCompassAvailable = sensors.registerListener(this, compassSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        isGyroAvailable = sensors.registerListener(this, gyroSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        isRotateVectorAvailable = sensors.registerListener(this, RotateVectorSensor,
                SensorManager.SENSOR_DELAY_UI);
        isGameRotationVectorAvailable = sensors.registerListener(this, GameRotationVector,
                SensorManager.SENSOR_DELAY_FASTEST);
        isGravityAvailable = sensors.registerListener(this, Gravity,
                SensorManager.SENSOR_DELAY_FASTEST);
        isLinearAccAvailable = sensors.registerListener(this, LinearAcc,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void startGPS() {
        /**
         * Here i can set the accuracy of the gps, and the power requirement
         */
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Criteria criteria = new Criteria();
        // criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // while we want fine accuracy, it's unlikely to work indoors where we
        // do our testing. :)
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String best = locationManager.getBestProvider(criteria, true);

        Log.v(DEBUG_TAG, "Best provider: " + best);

        /**
         * void requestLocationUpdates (Criteria criteria, long minTime, float minDistance, PendingIntent intent)
         * The elapsed time between location updates will never be less than minTime.
         * The minDistance parameter can also be used to control the frequency of location updates.
         * If it is greater than 0 then the location provider will only send your application an update when
         * the location has changed by at least minDistance meters, AND at least minTime milliseconds have passed.
         */

         locationManager.requestLocationUpdates(best, 50, 0, this);


        if (locationManager !=null )
        {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastLocation == null){
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

        }
        if (lastLocation !=null )
        {
            endLat = lastLocation.getLatitude();
            endLong = lastLocation.getLongitude();
        }

        /**
         *  The main problem the code in "green" is that if you have on gps and network, network will always get your location first
         *  but i can't make the phone use gps after some time, to get better accuracy. I can do that only if i have a button
         *  which will change from network to gps
         */
        /**
        Location gps_loc = null, net_loc = null;

        if (gps_enabled){
            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (network_enabled){
            net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (gps_loc != null && net_loc != null){
            if (gps_loc.getAccuracy() > net_loc.getAccuracy()){
                lastLocation = net_loc;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 0, this);
            }
            else {
                lastLocation = gps_loc;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, this);
            }
        }
        else{
            if (gps_loc != null){
                lastLocation = gps_loc;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, this);
            }

        }
        **/

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        float[] dist = new float[1];
        int [] distance = new int [ pointOfInterests.length];
        int [] counter = new int[ pointOfInterests.length];


        canvasHeight = canvas.getHeight();
        canvasWidth = canvas.getWidth();

        /**
         * This loop is used to calculate the distance between the phone and every POI( point of interest)
         * void distanceBetween (double startLatitude, double startLongitude, double endLatitude, double endLongitude, float[] results)
         *
         * The result of location.distanceBetween we need is located in the first place of variable dist( dist[0])
         *
         * Variable counter is used further in code, and will be better explained there
         *
         */
        for ( int i=0; i < pointOfInterests.length; i++)
        {
            Location.distanceBetween( pointOfInterests[i].getLatitude(),
                    pointOfInterests[i].getLongitude(), endLat, endLong, dist );
            counter[i] = i;
            distance[i] = (int) dist[0];

        }

        /**
         * This loop is used to tell the user that the GPS location isn't found, and to wait for it
         */
        if (lastLocation == null)
        {
            canvas.save();

            canvas.drawRect((canvas.getWidth() / 2) - 300, (canvas.getHeight()/2 ) - 80 , (canvas.getWidth() / 2) + 300,(canvas.getHeight()/2 ) +  80, roundRec);
            canvas.drawRect((canvas.getWidth() / 2) - 300, (canvas.getHeight()/2 ) - 80, (canvas.getWidth() / 2) + 300,(canvas.getHeight()/2 ) +  80, borderRec);
            canvas.drawText("Wait for GPS to locate you", canvas.getWidth() / 2 , canvas.getHeight()/2 , textPaint);

            canvas.restore();
        }

        if ( lastLocation != null) {

            /**
             * Because of the way canvas.draw works i need to put the farthest point first, because it will be drawn first,
             * and then over it the second point, and so on.
             * In the first place in distance[] is the value with biggest distance from the phone
             * because I need to draw the smallest distance last. That why the closest POI will be visible on the screen.
             *
             * counter[] is used to know which POI is in which place in distance[], because i want to give the right POI
             * the right value of distance
             *
             */
            for (int z=0; z < pointOfInterests.length; z++)
            {
                for (int j=z+1; j<pointOfInterests.length; j++)
                {
                    int a = distance[z];
                    int b = distance[j];

                    int c = counter[z];
                    int d = counter[j];
                    if (a < b)
                    {
                        distance[z] = b;
                        distance[j] = a;

                        counter[z] = d;
                        counter[j] = c;
                    }
                }
            }

            /**
             * float bearingTo (Location dest)
             *
             * Returns the approximate initial bearing in degrees East of true North when traveling
             * along the shortest path between this location and the given location.
             * The shortest path is defined using the WGS84 ellipsoid.
             * Locations that are (nearly) antipodal may produce meaningless results.
             *
             * Absolute bearing refers to the angle between the magnetic North (magnetic bearing)
             * or true North (true bearing) and an object.
             * For example, an object to the East would have an absolute bearing of 90 degrees.
             * Relative bearing refers to the angle between the craft's forward direction,
             * and the location of another object. For example, an object relative bearing of 0 degrees would be dead ahead;
             * an object relative bearing 180 degrees would be behind.
             * [1] Bearings can be measured in mils or degrees.
             */
        /**

        **/
            for (int i=0; i < pointOfInterests.length; i++)
            {
                Location temp = new Location("manual");
                temp.setLatitude(pointOfInterests[i].getLatitude());
                temp.setLongitude(pointOfInterests[i].getLongitude());
                temp.setAltitude(pointOfInterests[i].getAltitude());

                listOfBearingTo.add(lastLocation.bearingTo(temp));

            }



            /**
             * boolean getRotationMatrix (float[] R, float[] I, float[] gravity, float[] geomagnetic)
             *
             * Computes the inclination matrix I as well as the rotation matrix R
             * transforming a vector from the device coordinate system to the world's coordinate system
             * which is defined as a direct orthonormal basis, where:
             *
             * X is defined as the vector product Y.Z (It is tangential to the ground at the device's current location and roughly points East).
             * Y is tangential to the ground at the device's current location and points towards the magnetic North Pole.
             * Z points towards the sky and is perpendicular to the ground.
             *
             * I don't use getRotationMatrix because i get bad data.
             * We can also use lastGravity instead lastAccelerometer.
             */

            //SensorManager.getRotationMatrix(rotation, identity, lastGravity, lastCompass);

            /**
             * void getRotationMatrixFromVector (float[] R, float[] rotationVector)
             *
             * Helper function to convert a rotation vector to a rotation matrix.
             * Given a rotation vector (presumably from a ROTATION_VECTOR sensor),
             * returns a 9 or 16 element rotation matrix in the array R. R must have length 9 or 16.
             * If R.length == 9, the following matrix is returned:
             */

             SensorManager.getRotationMatrixFromVector(rotation, lastRotationVector);

            /**
             * boolean remapCoordinateSystem (float[] inR, int X, int Y, float[] outR)
             *
             * Rotates the supplied rotation matrix so it is expressed in a different coordinate system.
             * This is typically used when an application needs to compute the three orientation angles of the device
             * (see getOrientation(float[], float[])) in a different coordinate system.
             * When the rotation matrix is used for drawing (for instance with OpenGL ES),
             * it usually doesn't need to be transformed by this function,
             * unless the screen is physically rotated, in which case you can use Display.getRotation()
             * to retrieve the current rotation of the screen.
             * Note that because the user is generally free to rotate their screen,
             * you often should consider the rotation in deciding the parameters to use here.
             */

            SensorManager.remapCoordinateSystem(rotation, axisX, axisY, cameraRotation);

            /**
             * float[] getOrientation (float[] R, float[] values)
             * Computes the device's orientation based on the rotation matrix.
             *
             * When it returns, the array values are as follows:
             *
             * values[0]: Azimuth, angle of rotation about the -z axis.
             * This value represents the angle between the device's y axis and the magnetic north pole.
             * When facing north, this angle is 0, when facing south, this angle is π.
             * Likewise, when facing east, this angle is π/2, and when facing west, this angle is -π/2.
             * The range of values is -π to π.
             *
             * values[1]: Pitch, angle of rotation about the x axis.
             * This value represents the angle between a plane parallel to the device's screen
             * and a plane parallel to the ground.
             * Assuming that the bottom edge of the device faces the user and that the screen is face-up,
             * tilting the top edge of the device toward the ground creates a positive pitch angle.
             * The range of values is -π to π.
             *
             * values[2]: Roll, angle of rotation about the y axis.
             * This value represents the angle between a plane perpendicular to the device's screen
             * and a plane perpendicular to the ground.
             * Assuming that the bottom edge of the device faces the user and that the screen is face-up,
             * tilting the left edge of the device toward the ground creates a positive roll angle.
             * The range of values is -π/2 to π/2.
             *
             * Applying these three rotations in the azimuth, pitch, roll order transforms an identity matrix
             * to the rotation matrix passed into this method.
             * Also, note that all three orientation angles are expressed in radians.
             */

            SensorManager.getOrientation(cameraRotation, orientationAplha);
            orientation = lowPass(orientationAplha, orientation);



            for ( int i=0; i < pointOfInterests.length; i++)
            {
                /**
                 * Here i check the distance between the phone and POI. For every distance i have a value that
                 * is used to put the farthest point highest on the screen.
                 */
                if (distance[i]<50)
                {POIHight = 0;}
                else if (distance[i]<100)
                {POIHight = 40;}
                else if (distance[i]<150)
                {POIHight = 80;}
                else if (distance[i]<200)
                {POIHight = 120;}
                else if (distance[i]>200)
                {POIHight = 160;}

                /**
                 * This is to check the length of the text, so if the text is longer, the rectangle in which i show the texts
                 * is longer as well.
                 */
                textHeight = textPaint.descent() - textPaint.ascent();
                textOffset = (textHeight / 2) - textPaint.descent();
                textlength = pointOfInterests[counter[i]].getPlaces().length();

                canvas.save();

                /**
                 * This is to get the name of the location that the point shows, and to get distance to that point
                 */

                String mytext = pointOfInterests[counter[i]].getPlaces();
                String mytext2 = String.valueOf(distance[i]);

                /**
                 *  canvas.rotate() is used because if I apply roll to the phone i want my picture to always stay parallel
                 *  to the ground no mather the phone angle about y/z axis
                 */

                   // canvas.rotate((float) (0.0f - (Math.toDegrees(orientation[2]) /2)));

                /**
                 * Translate, but normalize for the FOV( field of view) of the camera -- basically, pixels per degree, times degrees == pixels
                 *
                 * Ne znam kako pametno napisati na engleskom, ukratko:
                 *
                 * (canvas.getWidth() / horizontalFOV) znaci da se sirina ekrana podjeli sa vrijednosti koja govori koliki je
                 * horizontali kut gledanja kamere, tu se dobije konstanta, nazovimo je konstanta ekrana.
                 *
                 * Math.toDegrees(orientation[0]) - listOfBearingTo.get(counter[i]) daje vrijednost koja je kut izmedu orijentacije mobitela
                 * i kuta na kojoj se nalazi tocka. Ako su te dvije vrijednosti jednake to znaci da se tocka nalazi na sredini ekrana.
                 * Ako je njihova vrijednost veca od horizontalnog kuta gledanja kamere to znaci da se tocka mora nacrtati van ekrana.
                 *
                 * Za y vrijednost koristim Math.toDegrees(orientation[1]) / 3. Tu vrijednost dijelim s 3 jer zelim smanjiti utjecaj mobitela
                 * u pomaku gore-dole.
                 */
                float dx = (float) ((canvas.getWidth() / horizontalFOV) * (Math.toDegrees(orientation[0]) - listOfBearingTo.get(counter[i])));
                float dy = (float) ((canvas.getHeight() / verticalFOV ) * Math.toDegrees(orientation[1]) /3);
                //float dy =  ((canvas.getHeight() / verticalFOV )   * POIHigh   ); /** this way the POI won't move about y axis, only about x axis**/


                /**
                 * Canvas.translate is used because i don't won't to calculate translation between 2 or more objects.
                 * I wont to start from the begining for every point.
                 */
                // wait to translate the dx so the horizon doesn't get pushed off
                canvas.translate(0.0f, 0.0f - dy);

                // now translate the dx
                canvas.translate(0.0f - dx, 0.0f);


                // draw rectangle  ( left , top, right, bottom)
                canvas.drawRect((canvas.getWidth() / 2) - ( textOffset * 20 ), (canvas.getHeight()/2 ) - 50 - POIHight, (canvas.getWidth() / 2) + ( textOffset * 20 ), (canvas.getHeight()/2 ) + 80 - POIHight, roundRec);

                // draw border rectangle
                canvas.drawRect((canvas.getWidth() / 2) - ( textOffset * 20 ), (canvas.getHeight()/2 ) - 50 - POIHight, (canvas.getWidth() / 2) + ( textOffset * 20 ), (canvas.getHeight()/2 ) + 80 - POIHight, borderRec);


                // draw text
                canvas.drawText(mytext, (canvas.getWidth() / 2) , (canvas.getHeight()/2 - POIHight) , textPaint);
                canvas.drawText(mytext2 +" meters ", (canvas.getWidth() / 2) , (canvas.getHeight()/ 2 ) + 40f - POIHight, textPaint);


                // draw my drawable picture
                canvas.drawBitmap ( bmp, (canvas.getWidth()/2) - 36 * 5, (canvas.getHeight()/2)- 95 * 4 - POIHight, null);

                /**
                 * canvas.save() and canvas.restore() is almost the same as the canvas.translate(). It is used so i don't
                 * need to calculate how far i need to draw one point from another, i can just start from scratch
                 *
                 */

                canvas.restore();

                /**
                canvas.save();
                canvas.translate(0.0f,0.0f);
                canvas.rotate((float) (0.0f - (Math.toDegrees(orientation[2]))));

                canvas.restore();
                 **/


            }
        }
    }

    public void showWindow(View parent, int i){

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.pop_up_show, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView opisTxt = (TextView) popupView.findViewById(R.id.opis_txt);
        TextView dataTxt = (TextView) popupView.findViewById(R.id.data_txt);

        popupWindow.showAtLocation(parent, android.view.Gravity.CENTER, android.view.Gravity.CENTER, android.view.Gravity.CENTER);
        popupWindow.setAnimationStyle(android.R.style.Animation_Toast);
        // popupWindow.getBackground();
        popupWindow.setFocusable(true);
        popupWindow.update();


        opisTxt.setText("Detalji: ");
        opisTxt.setTextSize(6 * context.getResources().getDisplayMetrics().density);
        opisTxt.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        dataTxt.setText(pointOfInterests[i].getData());
        dataTxt.setTextSize(4 * context.getResources().getDisplayMetrics().density);

        dataTxt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

    }

    public boolean onTouchEvent (MotionEvent event){
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                for (int i=0; i< pointOfInterests.length; i++){

                    float dx = (float) ((canvasWidth / horizontalFOV) * (Math.toDegrees(orientation[0]) - listOfBearingTo.get(i)));
                    float dy = (float) ((canvasHeight / verticalFOV ) * Math.toDegrees(orientation[1]) /3);

                    float[] dist = new float[1];
                    int  distance ;

                    Location.distanceBetween( pointOfInterests[i].getLatitude(),
                            pointOfInterests[i].getLongitude(), endLat, endLong, dist );
                    distance = (int) dist[0];

                    if (distance<50)
                    {POIHight = 0;}
                    else if (distance<100)
                    {POIHight = 40;}
                    else if (distance<150)
                    {POIHight = 80;}
                    else if (distance<200)
                    {POIHight = 120;}
                    else if (distance>200)
                    {POIHight = 160;}

                    textHeight = textPaint.descent() - textPaint.ascent();
                    textOffset = (textHeight / 2) - textPaint.descent();
                    textlength = pointOfInterests[i].getPlaces().length();

                 // canvas.drawBitmap ( bmp, (canvas.getWidth()/2) - 36 * 5, (canvas.getHeight()/2)- 95 * 4 - POIHight, null);

                    if (x >=( ((canvasWidth/2) -dx) - 36*5) && x < ((canvasWidth/2)-dx -(36*5)+ 200)   && y >=((canvasHeight/2)-dy - (95*4) - POIHight-50 ) && y <((canvasHeight/2)-dy - (95*4) - POIHight +400)) {

                        //  Toast.makeText(context, pointOfInterests[i].getPlaces(), Toast.LENGTH_SHORT).show();
                        showWindow(this,i);

                    }

                }
                break;

        }

        return false;
    }


    public void onAccuracyChanged(Sensor arg0, int arg1) {
        Log.d(DEBUG_TAG, "onAccuracyChanged");

    }

    public void onSensorChanged(SensorEvent event) {
        /**
         * Here i get the values of the sensors i need
         */
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                lastAccelerometer = event.values.clone();
                break;
            case Sensor.TYPE_GYROSCOPE:
                lastGyroscope = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastCompass = event.values.clone();
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                lastRotationVector = event.values.clone();
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                lastGameRotationVector = event.values.clone();
                break;
            case Sensor.TYPE_GRAVITY:
                lastGravity = event.values.clone();
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                lastLinearAcc = event.values.clone();
                break;
        }

        this.invalidate();


    }

    public void onLocationChanged(Location location) {
        /**
         * If the location of my phone is changed i need to get the new location
         */
        // store it off for use when we need it
        lastLocation = location;
        endLat = location.getLatitude();
        endLong = location.getLongitude();
        endAlti = location.getAltitude();

    }

    public void onProviderDisabled(String provider) {
        // ...
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    // this is not an override
    public void onPause() {

        locationManager.removeUpdates(this);
        sensors.unregisterListener(this);
    }

    // this is not an override
    public void onResume() {
        startSensors();
        startGPS();
    }


    // static final float ALPHA = 0.05f; // if ALPHA = 1 OR 0, no filter applies.
    static final float ALPHA = 0.3f;

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

}
