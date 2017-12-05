package ec601.aty.food_app;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        } else if (isConsumer(userToFind))
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
        return ((ProducerUser) currentUserSingleton).getLocationKeys();
    }

    public static void registerProducerAsInterestForConsumerFromPoint(String geofireKey, MapPoint point, FirebaseAuth mAuth, long reservationAmount)
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

        Map<String, Object> producerKeyToPointDataMap;
        if (((ConsumerUser) currentUserSingleton).getInterestedInProducerList() == null)
        {
            producerKeyToPointDataMap = new HashMap<>();
        } else
        {
            producerKeyToPointDataMap = ((ConsumerUser) currentUserSingleton).getInterestedInProducerList();
        }

        Map.Entry<String, Object> existingEntryForProducer = producerKeyToPointDataMap.entrySet().stream()
                .filter(mapElement -> mapElement.getKey().equals(point.getPosterID()))
                .findFirst()
                .orElse(null);

        if (existingEntryForProducer != null)
        {
            if (existingEntryForProducer.getValue() instanceof HashMap)
            {
                long oldReservationValue = (long) ((HashMap) existingEntryForProducer.getValue()).get("reservationAmount");
                ((HashMap) existingEntryForProducer.getValue()).put("reservationAmount", oldReservationValue + reservationAmount);
            }
        } else
        {
            Map<String, Object> keyMap = new HashMap<>();
            keyMap.put("producerName", point.getProducerName());
            keyMap.put("mapPointKey", geofireKey);
            keyMap.put("reservationAmount", reservationAmount);
            keyMap.put("unit", point.getUnit());
            producerKeyToPointDataMap.put(point.getPosterID(), keyMap);
        }

        newRef.updateChildren(producerKeyToPointDataMap);
        ((ConsumerUser) currentUserSingleton).setInterestedInProducerList(producerKeyToPointDataMap);
    }

    public static void getPointDataForProducerManage(Dialog dialog, Context context)
    {
        if (isCurrentUserProducer())
        {
            if (((ProducerUser) UserUtils.currentUserSingleton).getLocationKeys() != null)
            {
                Map.Entry<String, Object> locationPair = ((ProducerUser) UserUtils.currentUserSingleton).getLocationKeys().entrySet().iterator().next();
                String pointKey = locationPair.getKey();

                FirebaseUtils.getPointDataForProducerManagement(pointKey, dialog, context);
            } else
            {
                Toast.makeText(context, "You don't have any points placed!", Toast.LENGTH_LONG).show();

            }
        }
    }

    public static void getProducerDataForConsumerManage()
    {
        if (isCurrentUserConsumer())
        {
            if (((ConsumerUser) UserUtils.currentUserSingleton).getInterestedInProducerList() != null)
            {
                Map<String, Object> producerMap = ((ConsumerUser) UserUtils.currentUserSingleton).getInterestedInProducerList();
                // @TODO ANISH: Producer Map maps producer keys to sub maps that have geofirekeys, producername, reservation amount, and unit maps
                // See consumerData with interestedInProducerList on firebase for an example
            } else
            {

            }
        }
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
                        } else
                        {
                            mChannel = new NotificationChannel(channelID, "Food Reservation", importance);
                            mChannel.enableVibration(true);
                            mChannel.enableLights(true);
                            mChannel.setLightColor(Color.BLUE);
                            notificationManager.createNotificationChannel(mChannel);
                            notification.setChannelId(channelID);
                        }
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

    /*
    * ONLY USE WHEN PRODUCER IS DELETING POINT
    * First, delete geofire data
    * Delete in consumer
    * Delete point data
    * Update producer data and get rid of interestedConsumers and locationKeys
    * */
    public static void deletePointDataFromManagement(FirebaseAuth mAuth)
    {
        if (((ProducerUser) currentUserSingleton).getLocationKeys() != null)
        {
            String geofireKey = ((ProducerUser) currentUserSingleton)
                    .getLocationKeys()
                    .entrySet()
                    .iterator()
                    .next()
                    .getKey();
            GeoFireUtils.deleteGeofirePoint(geofireKey);

            if (((ProducerUser) currentUserSingleton).getInterestedConsumers() != null)
            {
                ((ProducerUser) currentUserSingleton).getInterestedConsumers().forEach((consumerKey, geoFireKey) ->
                {
                    removeProducerFromConsumer(consumerKey, mAuth.getCurrentUser().getUid());
                });
            }

            deleteInterestedConsumerAndLocationKeys(mAuth.getCurrentUser().getUid());

            String pointDataKey = ((ProducerUser) UserUtils.currentUserSingleton)
                    .getLocationKeys()
                    .entrySet()
                    .iterator()
                    .next()
                    .getKey();
            FirebaseUtils.deletePointData(pointDataKey);
            UserUtils.searchForForUserTypeData(mAuth, currentUserSingleton);
        }
    }

    public static void findInterestedConsumersFromProducerKeyAndDelete(String producerKey)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(PRODUCER_DATA_NODE_PATH)
                .child(producerKey)
                .child(PRODUCER_INTERESTED_CONSUMERS_CHILD_PATH);

        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                dataSnapshot.getChildren().forEach(childData ->
                {
                    removeProducerFromConsumer(childData.getKey(), producerKey);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public static void removeProducerFromConsumer(String consumerKey, String currentProducerKey)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(CONSUMER_DATA_NODE_PATH)
                .child(consumerKey)
                .child(CONSUMER_INTERESTED_IN_PRODUCERS_CHILD_PATH)
                .child(currentProducerKey);

        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                dataSnapshot.getChildren().forEach(childData -> childData.getRef().removeValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public static void deleteInterestedConsumerAndLocationKeys(String currentProducerKey)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database
                .getReference(PRODUCER_DATA_NODE_PATH)
                .child(currentProducerKey);

        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                dataSnapshot.getChildren().forEach(childData ->
                {
                    if (childData.getKey().equals(PRODUCER_INTERESTED_CONSUMERS_CHILD_PATH) ||
                            childData.getKey().equals(PRODUCER_LOCATION_KEY_CHILD_PATH))
                    {
                        childData.getRef().removeValue();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
