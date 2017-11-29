package ec601.aty.food_app;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.NOTIFICATION_SERVICE;

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

    public static void addConsumerAsInterestedInProducerFromPoint(String geofireKey, MapPoint point, FirebaseAuth mAuth, double reservationAmoount)
    {
        // Registering the consumer as interested under the producer node in DB
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(PRODUCER_DATA_NODE_PATH)
                .child(point.getPosterID())
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

        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("producerName", point.getProducerName());
        keyMap.put("mapPointKey", geofireKey);
        keyMap.put("reservationAmount", reservationAmoount);

        producerKeyToGeoFireKeyMap.put(point.getPosterID(), keyMap);
        newRef.updateChildren(producerKeyToGeoFireKeyMap);

        ((ConsumerUser)currentUserSingleton).setInterestedPointKeys(producerKeyToGeoFireKeyMap);
    }

    @TargetApi(26)
    public static void setInterestedConsumerNotificationForProducer(NotificationManager notificationManager, Context context, FirebaseAuth mAuth)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(PRODUCER_DATA_NODE_PATH);

//        if ( ((ProducerUser)currentUserSingleton).getInterestedConsumers() == null )
//        {
            ref
                .child(mAuth.getCurrentUser().getUid())
                .child(PRODUCER_INTERESTED_CONSUMERS_CHILD_PATH)
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        String channelID = "myChannel";
                        NotificationChannel mChannel = null;

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        Notification.Builder notification = new Notification.Builder(context)
                                .setSmallIcon(R.drawable.ic_account_box_black_24dp);

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                        {
                            notification.setContentTitle("Food Reservation")
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setPriority(Notification.PRIORITY_HIGH)
                                    .setAutoCancel(true);
                        }
                        else
                        {
                           mChannel = new NotificationChannel(channelID, "Food Reservation", importance);
                           mChannel.enableVibration(true);
                           mChannel.enableLights(true);
                           mChannel.setLightColor(Color.BLUE);
                           notificationManager.createNotificationChannel(mChannel);
                        }

                        notification.setChannelId(channelID);
                        getConsumerNameForReservationNotification(notificationManager, notification, dataSnapshot.getKey());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
     //   }
/*        else
        {
            TreeMap<String, Object> sortedInterestedConsumer = new TreeMap<>();
            sortedInterestedConsumer.putAll( ((ProducerUser)currentUserSingleton).getInterestedConsumers() );

            ref
                .child(mAuth.getCurrentUser().getUid())
                .child(PRODUCER_INTERESTED_CONSUMERS_CHILD_PATH)
                .startAt(sortedInterestedConsumer.lastKey())
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Notification.Builder notification = new Notification.Builder(context)
                                .setSmallIcon(R.drawable.ic_account_box_black_24dp)
                                .setContentTitle("Food Reservation")
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setAutoCancel(true);

                        getConsumerNameForReservationNotification(notificationManager, notification, dataSnapshot.getKey());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
        }*/
    }

    private static void getConsumerNameForReservationNotification(NotificationManager notificationManager, Notification.Builder notificationBuilder, String consumerKey)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference ref = database.getReference(CONSUMER_DATA_NODE_PATH);
        ref.child(consumerKey).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
               String consumerName = dataSnapshot.getValue(ConsumerUser.class).getName();
               notificationBuilder.setContentText(consumerName + " has reserved food.");
               notificationManager.notify(0, notificationBuilder.build());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError)
            {
                // @TODO: No network connectivity
            }
        });
    }
}
