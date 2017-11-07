package ec601.aty.food_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String USER_DATA_NODE_PATH = "userData";
    private EditText orgname;
    private Button regbutton;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private Spinner typespinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ref = FirebaseDatabase.getInstance().getReference(USER_DATA_NODE_PATH);
        mAuth = FirebaseAuth.getInstance();

        typespinner = (Spinner) findViewById(R.id.typeselect);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.account_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typespinner.setAdapter(adapter);

        orgname = (EditText) findViewById(R.id.nameinput);
        regbutton = findViewById(R.id.register);
        regbutton.setOnClickListener(view -> {
            if (orgname.getText().toString().length()==0){
                orgname.setError("Please enter the name of your organization");
                orgname.requestFocus();
            }
            else if (typespinner.getSelectedItem().toString()==null){
                Toast.makeText(RegisterActivity.this, "Please select an account type", Toast.LENGTH_LONG).show();
            }
            else{
                registerUser();
            }
        });
    }

    protected void registerUser(){
        User user = new User(User.AccountType.valueOf(typespinner.getSelectedItem().toString().toUpperCase()), orgname.getText().toString());
        ref.child(mAuth.getCurrentUser().getUid()).setValue(user);
        startActivity(new Intent(RegisterActivity.this, MapsActivity.class));
    }
}
