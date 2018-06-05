package grupo14.chusonfinder;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button administrador,user;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;
    int accion=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        administrador = (Button)findViewById(R.id.admin);
        user = (Button)findViewById(R.id.usuario);
        administrador.setVisibility(View.INVISIBLE);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sensor == null){ finish(); }

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float x = sensorEvent.values[0];
                if(x<-5){
                        accion++;
                        administrador.setVisibility(View.VISIBLE);
                        onResume();
                }else if(x>5){
                        accion++;
                    administrador.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };


        administrador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AdministradorLoginActivity.class);
                startActivity(intent);
                finish();
                return;

            }
        });
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,UsuarioLoginActivity.class);
                startActivity(intent);
                Sound();
                finish();
                return;

            }
        });
    }

    private void start(){
        sensorManager.registerListener(sensorEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
    }
private void Sound() {
    MediaPlayer mediaPlayer =  MediaPlayer.create(this, R.raw.exito);
    mediaPlayer.start();
}

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        start();
        super.onResume();
    }
}
