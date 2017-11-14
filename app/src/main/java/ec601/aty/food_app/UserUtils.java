package ec601.aty.food_app;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserUtils {
    private static final String PRODUCER_DATA_NODE_PATH = "producerData";
    private static final String CONSUMER_DATA_NODE_PATH = "consumerData";

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

    public static void searchForForUserTypeData(FirebaseAuth mAuth, User userToFind, String uuid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (userToFind instanceof ProducerUser) {
            DatabaseReference ref = database.getReference(PRODUCER_DATA_NODE_PATH);

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot data: dataSnapshot.getChildren()){
                        if (data.getKey().equals(mAuth.getCurrentUser().getUid())) {

                            ProducerUser foundUser = dataSnapshot.getValue(ProducerUser.class);

                            return;
                        } else {
                            continue;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        } else if (userToFind instanceof ConsumerUser) {
            DatabaseReference ref = database.getReference(CONSUMER_DATA_NODE_PATH);

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot data: dataSnapshot.getChildren()){
                        if (data.getKey().equals(mAuth.getCurrentUser().getUid())) {

                            ConsumerUser foundUser = dataSnapshot.getValue(ConsumerUser.class);

                            return;
                        } else {
                            continue;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
    }
}
