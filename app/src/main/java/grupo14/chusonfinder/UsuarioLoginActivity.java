package grupo14.chusonfinder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class UsuarioLoginActivity extends AppCompatActivity {
    private EditText correo,contra;
    private Button log,regis;

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private FirebaseAuth nAuth;
    private FirebaseAuth.AuthStateListener firebaseAutListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_login);

        nAuth = FirebaseAuth.getInstance();
        firebaseAutListener =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null){
                    Intent intent = new Intent(UsuarioLoginActivity.this,UserMapsActivity.class);
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

                nAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(UsuarioLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override

                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(UsuarioLoginActivity.this, "ERROR DE REGISTRO",Toast.LENGTH_SHORT).show();
                        }else{
                            String user_id = nAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("usuarios").child("usern").child(user_id);
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
                nAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(UsuarioLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(UsuarioLoginActivity.this, "ERROR DE INICIO DESESION",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //goMainScreen();
                handleFacebookAccesToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), R.string.cancel_login, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(),R.string.error_login,Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void handleFacebookAccesToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        nAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),R.string.firebase_error_login,Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void goMainScreen(){
            Intent intent = new Intent(this,AdminMenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
    }

  protected void onActivityResult(int requestCode,int resultCode,Intent data ){
        super.onActivityResult(requestCode,resultCode,data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
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
