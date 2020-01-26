package com.ssindher11.chirptut.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.ssindher11.chirptut.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.chirp.chirpsdk.ChirpSDK;
import io.chirp.chirpsdk.interfaces.ChirpEventListener;
import io.chirp.chirpsdk.models.ChirpError;
import io.chirp.chirpsdk.models.ChirpSDKState;

import static com.ssindher11.chirptut.Constants.CHIRP_APP_CONFIG;
import static com.ssindher11.chirptut.Constants.CHIRP_APP_KEY;
import static com.ssindher11.chirptut.Constants.CHIRP_APP_SECRET;
import static com.ssindher11.chirptut.Constants.RESULT_REQUEST_RECORD_AUDIO;
import static com.ssindher11.chirptut.Constants.iTAG;

public class ReceiveActivity extends AppCompatActivity {

    private ConstraintLayout constraintLayout;
    private TextView receivedKey;
    ChirpEventListener connectEventListener = new ChirpEventListener() {
        @Override
        public void onReceived(@Nullable byte[] data, int i) {
            if (data != null) {
                String identifier = new String(data);
                Log.v("ChirpSDK: ", "Received " + identifier);
                updateStatus(identifier);
            } else {
                Log.e("ChirpError: ", "Decode failed");
            }
        }

        @Override
        public void onReceiving(int i) {

        }

        @Override
        public void onSending(@NotNull byte[] bytes, int channel) {
        }

        @Override
        public void onSent(@NotNull byte[] bytes, int i) {
        }

        @Override
        public void onStateChanged(int oldState, int newState) {
            Log.v(iTAG, "ConnectCallback: onStateChanged " + oldState + " -> " + newState);
            if (newState == ChirpSDKState.CHIRP_SDK_STATE_NOT_CREATED.getCode()) {
                updateStatus("NotCreated");
            } else if (newState == ChirpSDKState.CHIRP_SDK_STATE_STOPPED.getCode()) {
                updateStatus("Stopped");
            } else if (newState == ChirpSDKState.CHIRP_SDK_STATE_RUNNING.getCode()) {
                updateStatus("Running");
            } else if (newState == ChirpSDKState.CHIRP_SDK_STATE_SENDING.getCode()) {
                updateStatus("Sending");
            } else if (newState == ChirpSDKState.CHIRP_SDK_STATE_RECEIVING.getCode()) {
                updateStatus("Receiving");
            }
        }

        @Override
        public void onSystemVolumeChanged(float oldVolume, float newVolume) {
            showConfirmationSnackbar("System volume has been changed to: " + newVolume);
        }
    };
    private ChirpSDK chirp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        initViews();

        //------Initialising Chirp SDK------
        chirp = new ChirpSDK(this, CHIRP_APP_KEY, CHIRP_APP_SECRET);
        ChirpError setConfigError = chirp.setConfig(CHIRP_APP_CONFIG);
        if (setConfigError.getCode() == 0) {
            Log.v(iTAG, "SDK service started!");
        } else {
            Log.e(iTAG, setConfigError.getMessage());
            Toast.makeText(ReceiveActivity.this, setConfigError.getMessage(), Toast.LENGTH_SHORT).show();
        }

        chirp.start();
        chirp.setListener(connectEventListener);
    }

    private void initViews() {
        constraintLayout = findViewById(R.id.container_receive_key);
        receivedKey = findViewById(R.id.tv_received_key);
    }

    public void updateStatus(final String newStatus) {
        runOnUiThread(() -> receivedKey.setText(newStatus));
    }

    void showConfirmationSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(constraintLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundResource(R.color.colorAccent);
        snackbar.show();
    }

    public void startSdk() {
        ChirpError error = chirp.start();
        if (error.getCode() > 0) {
            Log.e(iTAG, error.getMessage());
            return;
        }
        Log.v("Chirp", "SDK Started!!");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RESULT_REQUEST_RECORD_AUDIO);
        } else {
            startSdk();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RESULT_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSdk();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        chirp.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            chirp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
