package grupo14.chusonfinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Locale;

public class PostActivity extends AppCompatActivity implements OnClickListener {

    private EditText mPostTitle;
    private EditText mPostDesc;
    private Button mSubmitBtn;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabase;
    private Button btnSpeech, btnTxtspch;
    private TextToSpeech tts;
    int check = 1111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mPostTitle = (EditText) findViewById(R.id.titleField);
        mPostDesc  = (EditText) findViewById(R.id.descField);
        mSubmitBtn = (Button) findViewById(R.id.submitBtn);
        mStorage   = FirebaseStorage.getInstance().getReference();
        mProgress  = new ProgressDialog(this);
        mDatabase  = FirebaseDatabase.getInstance().getReference().child("Blog");
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
        btnSpeech = (Button)  findViewById(R.id.spchBtn);
        btnTxtspch = (Button) findViewById(R.id.txtspchBtn);
        tts = new TextToSpeech(this,OnInit);
        btnSpeech.setOnClickListener(this);
        btnTxtspch.setOnClickListener(this);
    }

    TextToSpeech.OnInitListener OnInit = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (TextToSpeech.SUCCESS==status){
                tts.setLanguage(new Locale("spa","ESP"));
            }
            else
            {
                Toast.makeText(getApplicationContext(), "TTS no disponible",Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void startPosting() {
        mProgress.setMessage("Chuzoneando :v...");
        mProgress.setCancelable(false);
        mProgress.show();
        final String title_val=mPostTitle.getText().toString().trim();
        final String desc_val=mPostDesc.getText().toString().trim();
        mProgress.dismiss();
        DatabaseReference newPost = mDatabase.push();
        newPost.child("title").setValue(title_val);
        newPost.child("desc").setValue(desc_val);
        startActivity(new Intent(PostActivity.this,BlogActivity.class));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==check && resultCode==RESULT_OK){
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String voz = results.get(0).toString();
            mPostDesc.setText(voz);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.spchBtn) {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hable ahora ");
            startActivityForResult(i, check);
        }
        if (v.getId()==R.id.txtspchBtn){
            tts.speak(mPostDesc.getText().toString(), TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void onDestroy(){
        tts.shutdown();
        super.onDestroy();
    }

}
