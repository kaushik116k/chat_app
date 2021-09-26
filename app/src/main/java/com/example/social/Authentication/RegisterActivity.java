package com.example.social.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.social.dashboard.DashboardActivity;
import com.example.social.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText email,password;
    Button register;
    TextView sign_in;
    ImageView facebook,google;
    FirebaseAuth auth;
    ProgressDialog mloadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        onResume();

        email = findViewById(R.id.email_id_1);
        password = findViewById(R.id.password_id_1);
        auth = FirebaseAuth.getInstance();
        mloadingBar = new ProgressDialog(RegisterActivity.this);

        sign_in = findViewById(R.id.sign_in_activity);
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        register = findViewById(R.id.Register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remail = email.getText().toString();
                String rpassword = password.getText().toString();
                if(TextUtils.isEmpty(remail) | rpassword.length() <= 4)
                {
                    email.setError("This field is required");
                    password.setError("Password should be minimum length of 5");
                }
                else
                {
                    mloadingBar.setTitle("Registration");
                    mloadingBar.setMessage("Please wait, while we are checking your credentials.");
                    mloadingBar.setCanceledOnTouchOutside(false);
                    mloadingBar.show();

                    auth.createUserWithEmailAndPassword(remail,rpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                FirebaseUser user = auth.getCurrentUser();
                                String email = user.getEmail();
                                String uid = user.getUid();

                                HashMap<Object,String> hashMap = new HashMap<>();
                                // put info in hash map
                                hashMap.put("email",email);
                                hashMap.put("uid",uid);
                                hashMap.put("name","");
                                hashMap.put("phone","");
                                hashMap.put("onlineStatus","online");
                                hashMap.put("image","");
                                hashMap.put("cover","");

                                //firebase database instance
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("Users");
                                reference.child(uid).setValue(hashMap);

                                Toast.makeText(RegisterActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                                finish();
                            }
                            else
                            {
                                Toast.makeText(RegisterActivity.this, "Registration failed! try again.", Toast.LENGTH_SHORT).show();
                                mloadingBar.dismiss();
                            }
                        }
                    });
                }
            }
        });

        facebook = findViewById(R.id.facebook_1);
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        google = findViewById(R.id.google_1);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, GoogleSignInActivity.class));
            }
        });
    }
    protected void onResume() {
        super.onResume();
        this.getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
    }
}