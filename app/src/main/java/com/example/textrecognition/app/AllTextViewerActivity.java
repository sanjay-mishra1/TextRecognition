package com.example.textrecognition.app;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.textrecognition.R;
import com.example.textrecognition.java.ChooserActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllTextViewerActivity extends AppCompatActivity {

    private Dialog dialog;
    private TextView textView;
    private DatabaseReference databaseReference;
    String textMain="";
    private ChildEventListener listener;
    private ArrayList<String> textList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_text_viewer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        textList= new ArrayList<>(ChooserActivity.allTextExtracted);
        if (!textList.isEmpty())
         initializeRecyclerView(textList);
        String name = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (name !=null)
            name = name.substring(0, name.indexOf("@"));
        Log.e("Name",""+ name);
        databaseReference= FirebaseDatabase.getInstance().getReference("Customers").child(name);
        attachListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    void showFormattedText(){
        BottomSheetDialogFragment bottomSheetDialogFragment=new FormattedTextViewerFragment(textList);
        bottomSheetDialogFragment.show(getSupportFragmentManager(),"Format");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.format) {
            showFormattedText();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void attachListener() {
        listener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.e("Child add",""+snapshot.getValue());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.e("Child change",""+snapshot.getValue());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        databaseReference.addChildEventListener(listener);
    }

    @Override
    public void onBackPressed() {
        databaseReference.removeEventListener(listener);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    void alertDialog(String text){
        textMain=text;
        if (dialog==null) {
            final View dialogView = View.inflate(this, R.layout.info_dialog, null);
            dialog = new Dialog(this, R.style.Dialog1);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.width =dpToPx(350);
            lp.height =ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            Objects.requireNonNull(dialog.getWindow()).setAttributes(lp);
            dialog.setCanceledOnTouchOutside(true);

            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(dialogView);
            textView=dialog.findViewById(R.id.textSelected);
            textView.setText(text);
            dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
                if (i == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    return true;
                }
                return false;
            });
            dialogView.findViewById(R.id.copyBt).setOnClickListener(v -> {
                copyToClipboard(textMain);
                dialog.dismiss();
            });
            dialogView.findViewById(R.id.sendBt).setOnClickListener(v -> {
                sendToDb(textMain);
                dialog.dismiss();
            });
        }else{
            if (textView!=null)
               textView.setText(text);
        }
        dialog.show();
    }

    public void sendToDb(String textMain) {
        Log.e("text to send","text->"+textMain);
        Log.e("Saving","Started");
        databaseReference.child("data").setValue(textMain).addOnCompleteListener(task -> {
         if (task.isSuccessful())
             Toast.makeText(AllTextViewerActivity.this,"Send",Toast.LENGTH_SHORT).show();
         else
         {
             Toast.makeText(AllTextViewerActivity.this,"Error occurred Error:"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
             task.getException().printStackTrace();
         }
     }).addOnFailureListener(e -> e.printStackTrace());
    }

    public void copyToClipboard(String textMain) {
        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Text Detector", textMain);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"Copied to clipboard",Toast.LENGTH_SHORT).show();
    }

    private void initializeRecyclerView(List<String> textList) {
        RecyclerView recyclerView=findViewById(R.id.recyclerview);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new Adapter(textList,this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;

    }
    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        List<String >allText;
        Context context;

        public Adapter(List<String> allText, Context context) {
            this.allText = allText;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view_adapter_layout, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String text=allText.get(position);
            holder.setText(text);
            holder.setMoreOptions(context,text);
        }

        @Override
        public int getItemCount() {
            return allText.size();
        }


    }
    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView=itemView.findViewById(R.id.textview);
        }

        public void setText(String s) {

            textView.setText(s);
        }
        public void setMoreOptions(Context context,String text){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog(text);
                }
            });
        }

    }
}