package com.example.app;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.hardware.*;
import android.content.Context;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements SensorEventListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor s, int i){
    }
    @Override
    public void onSensorChanged(SensorEvent s){
    }

    @Override
    protected void onStart () {
       super.onStart();
        //setContentView(R.layout.activity_main);

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
*/
        // First, get an instance of the SensorManager
        SensorManager sMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Second, get the sensors we're interested in
        Sensor magnetField = sMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
     //   Sensor accelerometer = sMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Third, implement a SensorEventListener class
       SensorEventListener magnetListener = new SensorEventListener(){
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // do things if you're interested in accuracy changes
            }
            public void onSensorChanged(SensorEvent event) {
                // implement what you want to do here
                TextView view = (TextView)findViewById(R.id.asd);
                view.setText("");
                for (int i=0;i<event.values.length;i++)
                {
                   view.setText(String.format("%s\nValues[%d]:%s \n",
                           view.getText(), //{0}
                           i, //{1}
                           event.values[i])//{2}
                        );

                }      // Finally, register your listener

            }
        };
        sMan.registerListener(magnetListener, magnetField, SensorManager.SENSOR_DELAY_NORMAL);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }



}
