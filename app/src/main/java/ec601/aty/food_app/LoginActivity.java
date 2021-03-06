package ec601.aty.food_app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity
{

    private SignInButton googleBtn;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "LOGIN_ACTIVITY";
    private static final String USER_DATA_NODE_PATH = "userData";

    private Spinner typeselect;
    private TextView accounttype, accountname;
    private EditText nameinput;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        googleBtn = (SignInButton) findViewById(R.id.googleBtn);
        typeselect = (Spinner) findViewById(R.id.typeselect);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.account_types, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeselect.setAdapter(adapter);
        accounttype = (TextView) findViewById(R.id.accounttype);
        accountname = (TextView) findViewById(R.id.accountname);
        nameinput = (EditText) findViewById(R.id.nameinput);
        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(view ->
        {
            if (nameinput.getText().toString().length() == 0)
            {
                nameinput.setError("Please enter the name of your organization");
                nameinput.requestFocus();
            } else if (typeselect.getSelectedItem().toString() == null)
            {
                Toast.makeText(LoginActivity.this, "Please select an account type", Toast.LENGTH_LONG).show();
            } else
            {
                registerUser();
            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener()
                {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Toast.makeText(LoginActivity.this, "Sign-In Error", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                if (firebaseAuth.getCurrentUser() != null)
                {
                    searchForExistingUser();
                }
                else if (firebaseAuth.getCurrentUser() == null)
                {
                    UserUtils.currentUserSingleton = null;


                }
            }
        };

        googleBtn.setOnClickListener(view ->
                signIn()
        );

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void searchForExistingUser()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(USER_DATA_NODE_PATH);
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getKey().equals(mAuth.getCurrentUser().getUid())) {
                        User foundUser = data.getValue(User.class);
                        switch (foundUser.getAccountType()) {
                            case PRODUCER: {
                                UserUtils.currentUserSingleton = new ProducerUser(foundUser);
                                UserUtils.searchForForUserTypeData(mAuth, UserUtils.currentUserSingleton);
                                break;
                            }
                            case CONSUMER: {
                                UserUtils.currentUserSingleton = new ConsumerUser(foundUser);
                                UserUtils.searchForForUserTypeData(mAuth, UserUtils.currentUserSingleton);
                                break;
                            }
                            default: {
                                // @TODO whoops
                                Toast.makeText(LoginActivity.this, "Default Case: Account Type", Toast.LENGTH_LONG).show();

                            }
                        }
                        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                        return;
                    }
                }
                toggleReg();
                //startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }

            @Override
            public void onCancelled(DatabaseError firebaseError)
            {

            }
        });
    }

    private void toggleReg()
    {
        googleBtn.setVisibility(View.GONE);
        typeselect.setVisibility(View.VISIBLE);
        accountname.setVisibility(View.VISIBLE);
        accounttype.setVisibility(View.VISIBLE);
        nameinput.setVisibility(View.VISIBLE);
        register.setVisibility(View.VISIBLE);
    }

    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess())
            {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else
            {
                // @TODO Google Sign In failed, update UI appropriately
                Toast.makeText(LoginActivity.this, "Google Sign In failed.",
                        Toast.LENGTH_SHORT).show();
            }
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                UserUtils.currentUserSingleton = null;
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else
                        {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    protected void registerUser()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(USER_DATA_NODE_PATH);
        User.AccountType accountType = User.AccountType.valueOf(typeselect.getSelectedItem().toString().toUpperCase());
        User user = new User(accountType, nameinput.getText().toString());
        ref.child(mAuth.getCurrentUser().getUid()).setValue(user);

        switch (accountType)
        {
            case PRODUCER:
            {
                UserUtils.addProducer(mAuth.getCurrentUser().getUid(), nameinput.getText().toString());
                UserUtils.getCurrentUserDetails(mAuth);
                break;
            }
            case CONSUMER:
            {
                UserUtils.addConsumer(mAuth.getCurrentUser().getUid(), nameinput.getText().toString());
                UserUtils.getCurrentUserDetails(mAuth);
                break;
            }
        }
        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
    }
}
