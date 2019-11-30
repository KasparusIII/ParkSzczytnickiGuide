package com.guide.park_szczytnicki;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class SignIn extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{
    GoogleApiClient gClient;
    SignInButton bGoogleSignIn;
    Button bSignOut;
    TextView tvStatus;
    private static final int RC_SIGN_IN = 9001;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        GoogleSignInOptions gsio = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();

        gClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gsio)
                .build();

        tvStatus = findViewById(R.id.tvStatus);

        bGoogleSignIn = findViewById(R.id.bGoogleSignIn);
        bGoogleSignIn.setOnClickListener(this);

        bSignOut = findViewById(R.id.bSignOut);
        bSignOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.bGoogleSignIn:
                signIn();
                break;
            case R.id.bSignOut:
                signOut();
                break;
        }
    }

    private void signIn()
    {    startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(gClient), RC_SIGN_IN);    }

    @Override
    public void onActivityResult(int requestC, int resultC, Intent data)
    {
        super.onActivityResult(requestC, resultC, data);

        if (requestC == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            Log.d("Sign In Activity", "handleSignInResult: " + result.isSuccess());
            if (result.isSuccess())
            {
                GoogleSignInAccount gAccount = result.getSignInAccount();
                tvStatus.setText("Halo" + gAccount.getDisplayName());
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        Log.d("Sign In Activity", "onConnectionFailed: " + result);
    }

    private void signOut()
    {
        Auth.GoogleSignInApi.signOut(gClient).setResultCallback(new ResultCallback<Status>()
        {
            @Override
            public void onResult(@NonNull Status status)
            {    tvStatus.setText("signed out");}
        });
    }
}