package ec601.aty.food_app;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Dennis on 12/9/2017.
 */

public class testUserData {
    public static final String USER_DATA_NODE_PATH = "userDataTest";

    private static void testUser(User dbUser, User inputUser){
        if (dbUser.getName().equals(inputUser.getName()))
            System.out.println("Names match");
        else
            System.out.println("Names don't match");
        if (dbUser.getAccountType().equals(inputUser.getAccountType()))
            System.out.println("Account Types match");
        else
            System.out.println("Account Types don't match");
    };

    private static void checkProducerPoint(String uid, User userdata){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(USER_DATA_NODE_PATH);

        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                testUserData.testUser(dataSnapshot.getValue(User.class), userdata);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
