package com.nurettingorsoy.arwithandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView textView1;
    CameraSource cameraSource;
    ImageButton swapButton;
    EditText expressionEditText;
    RelativeLayout layout;

    private static final int requestPermissionID = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.surfaceView);
        textView1 = findViewById(R.id.textView);
        swapButton = findViewById(R.id.swapButton);
        expressionEditText = findViewById(R.id.expressionEditText);
        layout = findViewById(R.id.relativeLayout);

        swapButton.setOnClickListener(view -> {

            if (cameraView.getVisibility() == View.VISIBLE) {
                cameraSource.stop();
                cameraView.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
            } else {
                cameraView.setVisibility(View.VISIBLE);
                layout.setVisibility(View.GONE);
            }

            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraSource.start(cameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }

            textView1.setText("");
        });

        expressionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    textView1.setText(new MathEvaluation(s.toString().replace("\n", "")).parse());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        startCameraSource();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == requestPermissionID && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            try {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraSource.start(cameraView.getHolder());

            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(this,"Permission not Granted",Toast.LENGTH_SHORT).show();
        }
    }

    private void startCameraSource(){

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if(textRecognizer.isOperational())
        {
            cameraSource = new CameraSource.Builder(getApplicationContext(),textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280,1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(4.0f)
                    .build();
            
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {

                    try {
                        if(ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CALL_COMPANION_APP)!=PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                        }
                        cameraSource.start(cameraView.getHolder());
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {

                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size()!=0)
                    {
                        textView1.post(() ->
                        {
                            try{
                                textView1.setText(new MathEvaluation(items.valueAt(0).getValue()).parse());
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        });
                    }
                }
            });
        }
    }
}