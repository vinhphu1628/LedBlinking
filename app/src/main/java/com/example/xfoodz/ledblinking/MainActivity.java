package com.example.xfoodz.ledblinking;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 1000;

    private Handler mHandler = new Handler();
    private Gpio mLedGpio;
    private Gpio mLedGpio1;
    private Gpio mLedGpio2;
    private boolean mLedState = false;

    private static final String PWM_NAME = "PWM1";
    private Pwm mPwm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManager manager = PeripheralManager.getInstance();
        List<String> portList = manager.getPwmList();
        if (portList.isEmpty()) {
            Log.i(TAG, "No PWM port available on this device.");
        } else {
            Log.i(TAG, "List of available ports: " + portList);
        }

        try {
            mPwm = manager.openPwm(PWM_NAME);
            initializePwm(mPwm);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access PWM", e);
        }

        try{
            String pinName = "BCM26";
            mLedGpio = PeripheralManager.getInstance().openGpio(pinName);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpio1 = PeripheralManager.getInstance().openGpio("BCM12");
            mLedGpio1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpio2 = PeripheralManager.getInstance().openGpio("BCM16");
            mLedGpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mHandler.post(mBlinkRunnable);
        }
        catch(IOException e){
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mBlinkRunnable);
        try {
            mLedGpio.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpio = null;
        }

        if (mPwm != null) {
            try {
                mPwm.close();
                mPwm = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close PWM", e);
            }
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLedGpio == null) {
                return;
            }
            try {
                mLedState = !mLedState;
                mLedGpio.setValue(mLedState);
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    public void initializePwm(Pwm pwm) throws IOException {
        pwm.setPwmFrequencyHz(120);
        pwm.setPwmDutyCycle(10);

        // Enable the PWM signal
        pwm.setEnabled(true);
    }
}
