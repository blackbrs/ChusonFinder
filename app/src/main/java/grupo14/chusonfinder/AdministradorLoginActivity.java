package grupo14.chusonfinder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdministradorLoginActivity extends AppCompatActivity {
    private EditText correo,contra;
    private Button log,regis;

    private FirebaseAuth nAuth;
    private FirebaseAuth.AuthStateListener firebaseAutListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrador_login);

        nAuth = FirebaseAuth.getInstance();
        firebaseAutListener =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null){
                    Intent intent = new Intent(AdministradorLoginActivity.this,BlogActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

            }
        };


        log = (Button)findViewById(R.id.login);
        regis = (Button)findViewById(R.id.Regis);

        correo = (EditText)findViewById(R.id.editCorreo);
        contra = (EditText)findViewById(R.id.editContra);

        regis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               final String email = correo.getText().toString();
               final String pass = contra.getText().toString();

               nAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(AdministradorLoginActivity.this, new OnCompleteListener<AuthResult>() {
                   @Override

                   public void onComplete(@NonNull Task<AuthResult> task) {
                       if(!task.isSuccessful()){
                           Toast.makeText(AdministradorLoginActivity.this, "ERROR DE REGISTRO",Toast.LENGTH_SHORT).show();
                       }else{
                           String user_id = nAuth.getCurrentUser().getUid();
                           DatabaseReference current_user_db = FirebaseDatabase .getInstance().getReference().child("usuarios").child("Admin").child(user_id);
                           current_user_db.setValue(true);
                       }
                   }
               });

            }
        });

        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = correo.getText().toString();
                final String pass = contra.getText().toString();
                nAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(AdministradorLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(AdministradorLoginActivity.this, "ERROR DE INICIO DESESION",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        nAuth.addAuthStateListener(firebaseAutListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        nAuth.removeAuthStateListener(firebaseAutListener);
    }
}