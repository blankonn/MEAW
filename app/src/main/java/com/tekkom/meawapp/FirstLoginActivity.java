package com.tekkom.meawapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirstLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FirstLoginActivity";
    private EditText inputUserName, inputName, inputID;
    private TextView inputDepartement;
    private Button save;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.firstlogin_btn_save:
                updateData();
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_login);

        inputUserName = findViewById(R.id.firstlogin_edt_username);
        inputName = findViewById(R.id.firstlogin_edt_name);
        inputID = findViewById(R.id.firstlogin_edt_id);

        inputDepartement = findViewById(R.id.firstlogin_txv_departement);

        save = findViewById(R.id.firstlogin_btn_save);
        save.setOnClickListener(this);
    }

    private void checkDepartement() {
        final String departementID = inputID.getText().toString().trim().substring(0, 4);
        switch (departementID) {
            case "0721":
                inputDepartement.setText("Teknik Komputer");
                break;
            case "0711":
                inputDepartement.setText("Teknik Elektro");
                break;
            case "0731":
                inputDepartement.setText("Teknik Biomedik");
                break;
            default:
                inputDepartement.setText("Teknik Komputer");
                break;
        }
    }

    private void updateData() {
        String userName = inputUserName.getText().toString().trim();
        String name = inputName.getText().toString().trim();
        String id = inputID.getText().toString().trim();
        if (inputID.getText().toString().length() >= 4) {
            checkDepartement();
        }
        String departement = inputDepartement.getText().toString().trim();

        if (userName.equals("") || name.equals("") || id.equals("") || departement.equals("")) {
            if (userName.equals("")) {
                inputUserName.setError("Username cannot blank");
                inputUserName.requestFocus();
            }
            if (name.equals("")) {
                inputName.setError("Name cannot blank");
                inputName.requestFocus();
            }
            if (id.equals("")) {
                inputID.setError("ID cannot blank");
                inputID.requestFocus();
            }
            if (departement.equals("")) {
                inputDepartement.setError("Departement field error");
                inputDepartement.requestFocus();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Saving data", Toast.LENGTH_LONG).show();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Map<String, Object> user = new HashMap<>();
            user.put("username", userName);
            user.put("name", name);
            user.put("id", id);
            user.put("departement", departement);
            user.put("firsttime", "0");
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Saving data done", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Error saving document", Toast.LENGTH_LONG).show();
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
            Toast.makeText(getApplicationContext(), "Checking data", Toast.LENGTH_LONG).show();
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()) {
                                    Toast.makeText(getApplicationContext(), "Login...", Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());

                                    if (documentSnapshot.getString("status").equals("Lecture")) {
                                        startActivity(new Intent(FirstLoginActivity.this, LectureProfileActivity.class));
                                    } else if (documentSnapshot.getString("status").equals("Student")) {
                                        startActivity(new Intent(FirstLoginActivity.this, StudentActivity.class));
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Data checking error", Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Get data error", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        }
    }
}
