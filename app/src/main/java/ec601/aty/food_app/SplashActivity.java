package ec601.aty.food_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class SplashActivity extends AppCompatActivity {

    private static final String USER_DATA_NODE_PATH = "userData";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    searchForExistingUser();//Delay of 10 seconds
                    sleep(500);
                } catch (Exception e) {

                } finally {

                    Intent i = new Intent(SplashActivity.this,
                            LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }
    //gimicky workaround for the async login
    public void searchForExistingUser()
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (mAuth.getCurrentUser() != null) {
            DatabaseReference ref = database.getReference(USER_DATA_NODE_PATH);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
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
                                    Toast.makeText(SplashActivity.this, "Default Case: Account Type", Toast.LENGTH_LONG).show();

                                }
                            }
                            Toast.makeText(SplashActivity.this, "Signing In", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SplashActivity.this, MapsActivity.class));
                        }
                    }
                    //startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
    }
}
