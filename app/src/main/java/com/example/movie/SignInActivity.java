    package com.example.movie;

    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;

    import com.facebook.AccessToken;
    import com.facebook.CallbackManager;
    import com.facebook.FacebookCallback;
    import com.facebook.FacebookException;
    import com.facebook.login.LoginManager;
    import com.facebook.login.LoginResult;
    import com.google.android.gms.auth.api.Auth;
    import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
    import com.google.android.gms.auth.api.signin.GoogleSignInClient;
    import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
    import com.google.android.gms.auth.api.signin.GoogleSignInResult;
    import com.google.android.gms.common.ConnectionResult;
    import com.google.android.gms.common.api.GoogleApiClient;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.AuthCredential;
    import com.google.firebase.auth.AuthResult;
    import com.google.firebase.auth.FacebookAuthProvider;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseAuthUserCollisionException;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.auth.GoogleAuthProvider;

    import java.util.Arrays;

    public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SignInActivity";
    private FirebaseAuth mAuth;
    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1000;
    private CallbackManager mCallbackManager; //???????????? ????????? ?????????
    String email;
    EditText emailEditText;
    SharedPreferences sh_Pref;
    SharedPreferences.Editor toEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().setTitle("Sign In");
        ImageButton btn_login = (ImageButton)findViewById(R.id.btn_login);
        ImageButton btn_google_login = (ImageButton)findViewById(R.id.btn_google);
        ImageButton btn_facebook_signin = (ImageButton)findViewById(R.id.btn_facebook);
        emailEditText = findViewById(R.id.emailEditText);
        TextView txt_find_pw = (TextView)findViewById(R.id.txt_find_pw);
        applySharedPreference();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //????????? ????????? ????????? ???
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        //???????????? ????????? ????????? ???
        btn_facebook_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallbackManager = CallbackManager.Factory.create(); //????????? ????????? ????????? ?????? ?????????

                LoginManager.getInstance().logInWithReadPermissions(com.example.movie.SignInActivity.this,
                        Arrays.asList("public_profile", "user_friends"));//?????????, ???????????? ???????????? ?????? ??????(?????????)
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());

                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                        Toast.makeText(com.example.movie.SignInActivity.this, "????????? ??????",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        btn_google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,RC_SIGN_IN);
            }
        });
        txt_find_pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PasswordResetActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override// ?????? ????????? ?????? ?????? ?????? ??? ??? ??????
    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SplashActivity SA = (SplashActivity)SplashActivity.Splash_Activity;//???????????? ????????????
        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {//????????? ??????
                                    Toast.makeText(getApplicationContext(), "????????? ??????",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    SA.finish();
                                    finish();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "????????? ??????",
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        }
        else{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);//???????????? ??????
        }
    }

    /*????????? ???????????? ??? ???????????? ?????? ??????????????? ????????? ??????*/
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }

    /*?????? ????????? ????????? ?????????*/
    private void signIn(){
        SplashActivity SA = (SplashActivity)SplashActivity.Splash_Activity;

        EditText passwordEditText = findViewById(R.id.passwordEditText);
        email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if(email.isEmpty()){
            Toast.makeText(com.example.movie.SignInActivity.this, "???????????? ????????? ?????????",
                    Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty()){
            Toast.makeText(com.example.movie.SignInActivity.this, "??????????????? ????????? ?????????",
                    Toast.LENGTH_SHORT).show();
        }
        else{
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(com.example.movie.SignInActivity.this, "????????? ??????",
                                        Toast.LENGTH_SHORT).show();
                                sharedPreference("UserEmail", email);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                SA.finish();
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(com.example.movie.SignInActivity.this, "????????? ??????",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }
        public void sharedPreference(String key, String value) {
            sh_Pref = getSharedPreferences("Login Credentials", MODE_PRIVATE);
            toEdit = sh_Pref.edit();
            toEdit.putString(key, value);//??????
            toEdit.commit();
        }
        public void applySharedPreference(){
            sh_Pref = getSharedPreferences("Login Credentials", MODE_PRIVATE);
            if (sh_Pref!=null && sh_Pref.contains("UserEmail")){ //null?????? noname
                String userEmail = sh_Pref.getString("UserEmail", "?????????");//????????????
                emailEditText.setText(userEmail);
            }
        }
        private void handleFacebookAccessToken(AccessToken token) {
            SplashActivity SA = (SplashActivity)SplashActivity.Splash_Activity;//???????????? ????????????
            Log.d(TAG, "handleFacebookAccessToken:" + token);

            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCredential:success");
                                Toast.makeText(com.example.movie.SignInActivity.this, "????????? ??????",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                SA.finish();//???????????? ???????????? ??????
                                finish();
                            }
                            else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(com.example.movie.SignInActivity.this, "?????? ????????? ???????????????(????????? ?????? ????????????)",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithCredential:failure", task.getException());

                            }
                        }
                    });
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }
    }
