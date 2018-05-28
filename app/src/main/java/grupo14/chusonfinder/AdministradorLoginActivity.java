package grupo14.chusonfinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class AdministradorLoginActivity extends AppCompatActivity {
    private EditText correo,contra;
    private Button log,regis;

    private FirebaseAuth nAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador_login);
        log = (Button)findViewById(R.id.login);
        regis = (Button)findViewById(R.id.Regis);

        correo = (EditText)findViewById(R.id.editCorreo);
        contra = (EditText)findViewById(R.id.editContra);


    }
}