package com.example.movie;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SignUpActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1000;
    private CallbackManager mCallbackManager;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setTitle("Sign Up");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ImageButton btn_continue = (ImageButton)findViewById(R.id.btn_sign_in);
        ImageButton btn_login = (ImageButton)findViewById(R.id.btn_login);
        ImageButton btn_google = (ImageButton)findViewById(R.id.btn_google);
        ImageButton btn_facebook = (ImageButton)findViewById(R.id.btn_facebook);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        getHashKey();

        //Continue ??????(????????? ???????????? ??????)??? ????????? ???
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
        //????????? ????????? ????????? ???
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //???????????? ????????? ????????? ???
        btn_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallbackManager = CallbackManager.Factory.create(); //????????? ????????? ????????? ?????? ?????????
                LoginManager.getInstance().logInWithReadPermissions(com.example.movie.SignUpActivity.this,
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
                    }
                });

            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });
    }

    @Override// ?????? ????????? ?????? ?????? ?????? ??? ??? ?????? + ???????????? ????????????
    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);//?????? ??????
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
        else{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);//???????????? ??????
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            DatabaseReference conditionRef = mDatabase.child("user").child(user.getUid());
                            String email = user.getEmail();
                            conditionRef.child("email").setValue(email);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(com.example.movie.SignUpActivity.this, "???????????? ??????",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(com.example.movie.SignUpActivity.this, "???????????? ??????",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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

    /*?????? ????????? ????????? ?????? ?????????*/
    private void createAccount(){
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        SplashActivity SA = (SplashActivity)SplashActivity.Splash_Activity;//???????????? ????????????
        if(email.isEmpty()){
            Toast.makeText(com.example.movie.SignUpActivity.this, "???????????? ????????? ?????????",
                    Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty()){
            Toast.makeText(com.example.movie.SignUpActivity.this, "??????????????? ????????? ?????????",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                DatabaseReference conditionRef = mDatabase.child("user").child(user.getUid());
                                String email = user.getEmail();
                                conditionRef.child("email").setValue(email);
                                Log.d(TAG, "createUserWithEmail:success");
                                Toast.makeText(com.example.movie.SignUpActivity.this, "???????????????",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                                startActivity(intent);
                                SA.finish();
                                finish();

                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(com.example.movie.SignUpActivity.this, "????????? ???????????? ???????????????",
                                        Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(com.example.movie.SignUpActivity.this, "??????????????? 6?????? ??????????????? ?????????",
                                        Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(com.example.movie.SignUpActivity.this, "???????????? ?????? ????????? ???????????????",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(com.example.movie.SignUpActivity.this, "?????? ???????????????",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }


    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    /*???????????? ???????????? ?????????*/
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            DatabaseReference conditionRef = mDatabase.child("user").child(user.getUid());
                            String email = user.getEmail();
                            conditionRef.child("email").setValue(email);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(com.example.movie.SignUpActivity.this, "???????????? ??????",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                            startActivity(intent);
                            finish();

                        }
                        else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                            Toast.makeText(com.example.movie.SignUpActivity.this, "????????? ???????????? ???????????????",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(com.example.movie.SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }
}
