package com.example.videorecorder;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MessageAdapter adapter;
    boolean isAdmin=false;
    FirebaseFirestore db;
    ProgressDialog progressDialog;
    TextView defaultText;
    ArrayList<MessageItem> arrayList=new ArrayList<>();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    SimpleDateFormat simpleDateFormat1= new SimpleDateFormat("HH:mm",Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Button btn = findViewById(R.id.send);
        EditText editText = findViewById(R.id.message);
        recyclerView = findViewById(R.id.recyclerView);
        defaultText=findViewById(R.id.default_text);
        progressDialog = new ProgressDialog(AdminActivity.this);
        progressDialog.setTitle("Loading....");

        db = FirebaseFirestore.getInstance();

        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayout layout = findViewById(R.id.admin_layout);
        SharedPreferences sharedPreferences =getSharedPreferences("user",MODE_PRIVATE);
        isAdmin=sharedPreferences.getBoolean("admin",false);

        if(isAdmin){
            layout.setVisibility(View.VISIBLE);
        }
        else{
            layout.setVisibility(View.GONE);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText().toString().isEmpty()){
                    Toast.makeText(AdminActivity.this,"Enter message!",Toast.LENGTH_SHORT).show();
                }
                else{
                    try {
                        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {

                    }
                    sendMessage(editText.getText().toString());

                    editText.getText().clear();
                }
            }
        });

        getMessages();

    }

    public void getMessages() {

        progressDialog.show();

        db.collection("messages")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            defaultText.setVisibility(View.VISIBLE);
                            progressDialog.cancel();
                            return;
                        } else {
                            List<MessageItem> types = documentSnapshots.toObjects(MessageItem.class);
                            arrayList.addAll(types);
                            adapter = new MessageAdapter(arrayList);
                            recyclerView.setAdapter(adapter);
                            progressDialog.cancel();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error getting data!!!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void sendMessage(String message){
        progressDialog.show();
        Date date = new Date();
        String messageDate=simpleDateFormat.format(date);
        String messageTime=simpleDateFormat1.format(date);

        Map<String, Object> msg = new HashMap<>();
        msg.put("message", message);
        msg.put("date",messageDate );
        msg.put("time", messageTime);

        db.collection("messages")
                .add(msg)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        addNewMessageToRV(message,messageDate,messageTime);
                        Log.d("add-msg", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("add-msg", "Error adding document", e);
                    }
                });
    }

    public void addNewMessageToRV(String m, String d, String t){
        Log.d("time",t);
        MessageItem messageItem = new MessageItem(m,d,t);
        arrayList.add(messageItem);
        if(arrayList.size()==1){
            adapter = new MessageAdapter(arrayList);
            recyclerView.setAdapter(adapter);
        }
        else {
            adapter.notifyItemInserted(arrayList.size() - 1);
        }
        defaultText.setVisibility(View.INVISIBLE);
        progressDialog.cancel();
    }


}
