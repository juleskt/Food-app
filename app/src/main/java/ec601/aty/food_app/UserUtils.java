package ec601.aty.food_app;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserUtils {
    private static final String PRODUCER_DATA_NODE_PATH = "producerData";
    private static final String CONSUMER_DATA_NODE_PATH = "consumerData";
    private static final String USER_DATA_NODE_PATH = "userData";

    public static User currentUserSingleton;

    public static void addProducer(String key, String name) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(PRODUCER_DATA_NODE_PATH).child(key);
        ref.setValue(new ProducerUser(name));
    }

    public static void addConsumer(String key, String name) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(CONSUMER_DATA_NODE_PATH).child(key);
        ref.setValue(new ConsumerUser(name));
    }

    public static void searchForForUserTypeData(FirebaseAuth mAuth, User userToFind) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (userToFind instanceof ProducerUser) {
            DatabaseReference ref = database.getReference(PRODUCER_DATA_NODE_PATH);
            ref.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                   currentUserSingleton = dataSnapshot.getValue(ProducerUser.class);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        } else if (userToFind instanceof ConsumerUser) {
            DatabaseReference ref = database.getReference(CONSUMER_DATA_NODE_PATH);
            ref.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currentUserSingleton = dataSnapshot.getValue(ConsumerUser.class);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
    }

    // Pretty hacky, but need a way to call searchForUser from MapsActivity to handle already logged in people
    // Essentially same functionality as LoginActivity::searchForExistingUser
    public static void getCurrentUserDetails(FirebaseAuth mAuth) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(USER_DATA_NODE_PATH);
        ref.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User foundUser = dataSnapshot.getValue(User.class);

                switch (foundUser.getAccountType()) {
                    case PRODUCER: {
                        currentUserSingleton = new ProducerUser(foundUser);
                        searchForForUserTypeData(mAuth, currentUserSingleton);
                        break;
                    }
                    case CONSUMER: {
                        currentUserSingleton = new ConsumerUser(foundUser);
                        searchForForUserTypeData(mAuth, currentUserSingleton);
                        break;
                    }
                    default: {
                        // whoops
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }
}
