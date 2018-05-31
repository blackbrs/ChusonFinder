package grupo14.chusonfinder;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DatosPersonalesActivity extends AppCompatActivity {
    private EditText nNombre,nTelefono;

    private Button nBack,nConfirm;

    private FirebaseAuth nAuth;
    private DatabaseReference mUsuarioDatabase;

    private String userID;
    private String nName;
    private String nTel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_personales);

        nNombre = (EditText)findViewById(R.id.nombre);
        nTelefono = (EditText)findViewById(R.id.tel);

        nBack = (Button)findViewById(R.id.volv);
        nConfirm = (Button)findViewById(R.id.confirm);

        nAuth = FirebaseAuth.getInstance();
        userID = nAuth.getCurrentUser().getUid();

        mUsuarioDatabase = FirebaseDatabase.getInstance().getReference().child("usuarios").child("usern").child(userID);
        getUserInfo();

        nConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveUserInformation();
            }
        });

        nBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
    }
    private void getUserInfo(){
    mUsuarioDatabase.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                Map<String, Object> map = (Map<String,Object>)dataSnapshot.getValue();
                if(map.get("name")!=null){
                    nName = map.get("name").toString();
                    nNombre.setText(nName);
                }
                if(map.get("telefono")!=null){
                    nTel = map.get("telefono").toString();
                    nTelefono.setText(nTel);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
    }
    private void SaveUserInformation() {
        nName = nNombre.getText().toString();
        nTel = nTelefono.getText().toString();
        Map userInfor = new HashMap();
        userInfor.put("name",nName);
        userInfor.put("telefono",nTel);

        mUsuarioDatabase.updateChildren(userInfor);


    }
}
