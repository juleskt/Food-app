package ec601.aty.food_app;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        ref.setValue(new ProducerUser(name));
    }
}
