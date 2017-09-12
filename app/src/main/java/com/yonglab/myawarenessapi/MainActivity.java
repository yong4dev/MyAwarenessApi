package com.yonglab.myawarenessapi;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private static final int MY_PERMISSION_LOCATION = 100;
    private Button getWeatherButton;
    private TextView mTextStatus;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWeatherButton = (Button) findViewById(R.id.bt_getWeather);
        mTextStatus = (TextView) findViewById(R.id.TEXT_STATUS_ID);
        mScrollView = (ScrollView) findViewById(R.id.SCROLLER_ID);

        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this.getApplicationContext())
                        .addApi(Awareness.API)
                        .build();
                mGoogleApiClient.connect();

                Awareness.SnapshotApi.getDetectedActivity(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<DetectedActivityResult>() {
                            @Override
                            public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
                                if (!detectedActivityResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Could not get the current activity.");
                                    return;
                                }
                                ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
                                DetectedActivity probableActivity = ar.getMostProbableActivity();
                                Log.i(TAG, probableActivity.toString());
                            }
                        });


                if (ContextCompat.checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSION_LOCATION
                    );
                    return;
                }

                Awareness.SnapshotApi.getWeather(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<WeatherResult>() {
                            @Override
                            public void onResult(@NonNull WeatherResult weatherResult) {
                                int statusCode = weatherResult.getStatus().getStatusCode();
                                Log.i(TAG, "Status Code: " + statusCode);
                                if (!weatherResult.getStatus().isSuccess()) {
                                    Log.e(TAG,  "Could not get weather.");
                                    mTextStatus.append("\n" + statusCode + weatherResult.getStatus().getStatusMessage());
                                    return;
                                }
                                Weather weather = weatherResult.getWeather();
                                Log.i(TAG, "Weather: " + weather.toString());
                                mTextStatus.append("\n" + statusCode + "= " + weather);
                                scrollToBottom();
                            }
                        });

            }
        });


    }

    private void scrollToBottom()
    {
        mScrollView.post(new Runnable()
        {
            public void run()
            {
                mScrollView.smoothScrollTo(0, mTextStatus.getBottom());
            }
        });
    }
}
