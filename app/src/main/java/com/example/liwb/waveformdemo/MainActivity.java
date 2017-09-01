package com.example.liwb.waveformdemo;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.signalwaveform.waveform.WaveForm;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    LinearLayout ll;
    WaveForm waveForm;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制横屏
        ll=findViewById(R.id.waveform);
        waveForm=new WaveForm(this);
        ll.addView(waveForm);
        button=findViewById(R.id.OK);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waveForm.clear();
                for(float i=1;i<=800;i+=1){
                    double y1 = Math.sin((double)( i/50.0f));
                    float x=i;
                    waveForm.addS1(x,(float)y1);
                    double y2=Math.cos(i/50.0f);
                    waveForm.addS2(x,(float)y2);
                    double y3=Math.sin(y1)+1;
                    waveForm.addS3(x,(float)y3);
                }
            }
        });


    }
}
