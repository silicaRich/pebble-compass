package pebblecompass.example.app;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class MainActivity extends Activity {

    private static SensorManager sensorService;
    private CompassView compassView;
    private Sensor sensor;
  //  private PowerManager.WakeLock wakeLock;

    // Time interval in which data is sent
    long intervalToSendDataToPebble = 750;
    String charDir="";
    float floatDir = 0.0f;

    public void onCreate(Bundle savedInstanceState) {


 /* Wake lock is used to prevent apps from stopping when the screen turns off.
          I'm using it here so that the pebble can be sent the cardinal direction even when the screen is off.
        */
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
       /* wakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "Mywakelock");
        Log.i("","wake lock begin.");
        wakeLock.acquire();
        Toast acquire = Toast.makeText(getApplicationContext(), "haaayy",
                Toast.LENGTH_SHORT);
        acquire.show(); */
        super.onCreate(savedInstanceState);
        compassView = new CompassView(this);
        setContentView(compassView);

        sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (sensor != null) {
            sensorService.registerListener(mySensorEventListener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
           // Log.i("Compass MainActivity", "Registerered for ORIENTATION Sensor");

        } else {
          //  Log.e("Compass MainActivity", "Registerered for ORIENTATION Sensor");
            Toast.makeText(this, "ORIENTATION Sensor not found",
                    Toast.LENGTH_LONG).show();
            finish();
        }

       testAsyncTask();

        Thread t = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    try {
                        android.os.SystemClock.sleep(intervalToSendDataToPebble);
                        sendDataToPebble(charDir, floatDir);
                    } catch(Exception e) { /*Log.i("Send Data Thread", "IDFK:" + e.toString());*/ }
                }
            }
        });
        t.start();

    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        Toast.makeText(this, "Calibrate your sensors by moving the phone in a figure-8 direction. Open your Pebble Watchapp, PebbleCompass, to transfer the cardinal direction to your Pebble.", Toast.LENGTH_LONG).show();
        return false;

    }
    public void sendDataToPebble(String charDir, float floatDir){

            PebbleDictionary data = new PebbleDictionary();
            //data.addUint8(0, (byte) 42);
            //data.addString(0, ""+position);
            data.addString(0, charDir);
            data.addString(1, ""+floatDir);
            /*data.addInt32(1, (int)position);*/
            UUID watchAppUUID = UUID.fromString("1309b19f-0a2c-4097-89f2-25a48263de32");
            PebbleKit.sendDataToPebble(getApplicationContext(), watchAppUUID, data);
    }

    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        //every time the phone is moved...
        //this function executes.
        @Override
        public void onSensorChanged(SensorEvent event) {

            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            float azimuth = event.values[0];
            //using lerp function
            //start + percent*(end - start)
            //floatDir = azimuth;
            //floatDir = start | azimuth = end
            azimuth = floatDir + 0.75f*(azimuth - floatDir);

            compassView.updateData(azimuth);
            if(azimuth >=350 || azimuth <=10)
                charDir="N";
            else if(azimuth >10 && azimuth <85)
                charDir="NE";
            else if(azimuth >85 && azimuth <= 95)
                charDir="E";
            else if(azimuth >95 && azimuth <=170)
                charDir="SE";
            else if(azimuth >170 && azimuth <=185)
                charDir="S";
            else if(azimuth > 185 && azimuth <=260)
                charDir="SW";
            else if(azimuth > 260 && azimuth <=280)
                charDir="W";
            else
                charDir="NW";
         //   Log.i("direction:", charDir);
            compassView.charDir=charDir;
            //sendDataToPebble(charDir, floatDir);

            //save azimuth
            floatDir = azimuth;
        }
    };


        /* the AsyncTask allows another thread, besides the Main thread, to run.
           Apparently this, coupled with my WakeLock, is ABSOLUTELY necessary in order to have the compass running with
           the Android's phone screen off.
         */
    private void testAsyncTask() {
        // Create an AsyncTask that counts to 10 seconds and publishes updates
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

             int d = 0;
             boolean keepRunning = true;
             while(keepRunning){
                 d++;
                 //if(d%1000 == 0)
                     //Log.i("in thread derpin", "in thread derpin");

                 android.os.SystemClock.sleep(1000);
             }
                return null;
            }

            @Override
            protected void onPreExecute() {
          //      Log.d("TASK", "PRE EXECUTE ON UI THREAD");
                super.onPreExecute();
            }

            protected void onProgressUpdate(Integer[] values) {
           //     Log.d("TASK", "PROGRESS: " + values[0]);
            };

            protected void onPostExecute(Integer result) {
                //Log.d("TASK", "POST EXECUTE ON UI THREAD: " + result);
            };
        }.execute();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensor != null) {
            sensorService.unregisterListener(mySensorEventListener);
        }
        Log.i("","wake lock exit.");
        /*wakeLock.release();
        Toast release = Toast.makeText(getApplicationContext(),
                "Wake Lock OFF", Toast.LENGTH_SHORT);
        release.show();*/
    }

}
