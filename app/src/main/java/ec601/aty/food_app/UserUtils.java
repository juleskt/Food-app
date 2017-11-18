package ec601.aty.food_app;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserUtils
{
    private static final String PRODUCER_DATA_NODE_PATH = "producerData";
    private static final String CONSUMER_DATA_NODE_PATH = "consumerData";
    private static final String USER_DATA_NODE_PATH = "userData";
    private static final String PRODUCER_LOCATION_KEY_CHILD_PATH = "locationKeys";
    private static final String PRODUCER_INTERESTED_CONSUMERS_CHILD_PATH = "interestedConsumers";
    private static final String CONSUMER_INTERESTED_IN_PRODUCERS_CHILD_PATH = "interestedInProducerList";

    private static FirebaseAuth mAuth;

    public static User currentUserSingleton = null;

    public static void setmAuth(FirebaseAuth mAuth)
    {
        UserUtils.mAuth = mAuth;
    }

    public static boolean isProducer(User user)
    {
        return user instanceof ProducerUser;
    }

    public static boolean isConsumer(User user)
    {
        return user instanceof ConsumerUser;
    }

    public static boolean isCurrentUserProducer()
    {
        return currentUserSingleton instanceof ProducerUser;
    }

    public static boolean isCurrentUserConsumer()
    {
        return currentUserSingleton instanceof ConsumerUser;
    }

    public static void addProducer(String key, String name)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(PRODUCER_DATA_NODE_PATH).child(key);
        ref.setValue(new ProducerUser(name));
    }

    public static void addConsumer(String key, String name)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(CONSUMER_DATA_NODE_PATH).child(key);
        ref.setValue(new ConsumerUser(name));
    }

    public static void searchForForUserTypeData(FirebaseAuth mAuth, User userToFind)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (isProducer(userToFind))
        {
            DatabaseReference ref = database.getReference(PRODUCER_DATA_NODE_PATH);
            ref.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    Log.e("foor", "onDataChange: " + ProducerUser.class.getName());
                    currentUserSingleton = dataSnapshot.getValue(ProducerUser.class);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError)
                {
                    // @TODO: No network connectivity
                }
            });
        }
        else if (isConsumer(userToFind))
        {
            DatabaseReference ref = database.getReference(CONSUMER_DATA_NODE_PATH);
            ref.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    currentUserSingleton = dataSnapshot.getValue(ConsumerUser.class);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError)
                {
                    // @TODO: No network connectivity
                }
            });
        }
    }

    // Pretty hacky, but need a way to call searchForUser from MapsActivity to handle already logged in people
    // Essentially same functionality as LoginActivity::searchForExistingUser but called from static context
    public static void getCurrentUserDetails(FirebaseAuth mAuth)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(USER_DATA_NODE_PATH);
        ref.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                User foundUser = dataSnapshot.getValue(User.class);

                switch (foundUser.getAccountType())
                {
                    case PRODUCER:
                    {
                        currentUserSingleton = new ProducerUser(foundUser);
                        searchForForUserTypeData(mAuth, currentUserSingleton);
                        break;
                    }
                    case CONSUMER:
                    {
                        currentUserSingleton = new ConsumerUser(foundUser);
                        searchForForUserTypeData(mAuth, currentUserSingleton);
                        break;
                    }
                    default:
                    {
                        // whoops
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError)
            {
                // @TODO: No network connectivity
            }
        });
    }

    public static void safeSignOut(FirebaseAuth mAuth)
    {
        mAuth.signOut();
        currentUserSingleton = null;
    }

    public static void addPointForCurrentProducer(String geoFireKey, FirebaseAuth mAuth)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(PRODUCER_DATA_NODE_PATH)
                .child(mAuth.getCurrentUser().getUid())
                .child(PRODUCER_LOCATION_KEY_CHILD_PATH);

        Map<String, Object> pointKeyMap = new HashMap<>();
        // @TODO: make different point classes
        pointKeyMap.put(geoFireKey, "Food MapPoint");
        ref.updateChildren(pointKeyMap);
    }

    public static Map<String, Object> getPointsForCurrentProducer()
    {
        return ((ProducerUser)currentUserSingleton).getLocationKeys();
    }

    public static void addConsumerAsInterestedInProducerFromPoint(String geofireKey, String producerKey, FirebaseAuth mAuth)
    {
        // Registering the consumer as interested under the producer node in DB
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(PRODUCER_DATA_NODE_PATH)
                .child(producerKey)
                .child(PRODUCER_INTERESTED_CONSUMERS_CHILD_PATH);

        Map<String, Object> consumerKeyToGeoFireKeyMap = new HashMap<>();
        consumerKeyToGeoFireKeyMap.put(mAuth.getCurrentUser().getUid(), geofireKey);
        ref.updateChildren(consumerKeyToGeoFireKeyMap);

        // Registering the producer under the consumer node as an interest in DB
        DatabaseReference newRef = database
                .getReference(CONSUMER_DATA_NODE_PATH)
                .child(mAuth.getCurrentUser().getUid())
                .child(CONSUMER_INTERESTED_IN_PRODUCERS_CHILD_PATH);

        Map<String, Object> producerKeyToGeoFireKeyMap;
        if ( ((ConsumerUser)currentUserSingleton).getInterestedPointKeys() == null )
        {
            producerKeyToGeoFireKeyMap = new HashMap<>();
        }
        else
        {
            producerKeyToGeoFireKeyMap = ((ConsumerUser)currentUserSingleton).getInterestedPointKeys();
        }

        producerKeyToGeoFireKeyMap.put(producerKey, geofireKey);
        newRef.updateChildren(producerKeyToGeoFireKeyMap);

        ((ConsumerUser)currentUserSingleton).setInterestedPointKeys(producerKeyToGeoFireKeyMap);
    }
}
