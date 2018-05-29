package grupo14.chusonfinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button administrador,user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        administrador = (Button)findViewById(R.id.admin);
        user = (Button)findViewById(R.id.usuario);

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
                finish();
                return;

            }
        });
    }
}
