package com.mdp.mdpandroidfinal2.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mdp.mdpandroidfinal2.R;

import java.nio.charset.Charset;

public class CommsPopUp extends AppCompatActivity {
    private static final String TAG = "CommsPopUp";


    //Instantiating layout elements
    EditText typeBoxEditText;
    FloatingActionButton send;
    private static TextView messageReceivedTextView;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comms);

        // Fetching layout elements
        typeBoxEditText = (EditText) findViewById(R.id.typeBoxEditText);
        messageReceivedTextView = (TextView) findViewById(R.id.messageReceivedTextView);
        messageReceivedTextView.setMovementMethod(new ScrollingMovementMethod());
        //messageRecievedTitle = (TextView) findViewById(R.id.messageReceivedTitleTextView);
        send = (FloatingActionButton) findViewById(R.id.messageButton);

        // get shared preferences
        sharedPreferences = getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked sendTextBtn");
                String sentText = "" + typeBoxEditText.getText().toString(); //Get the text typed in the textbox

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("message", sharedPreferences.getString("message", "") + '\n' + sentText);
                editor.commit();
                messageReceivedTextView.setText(sharedPreferences.getString("message", ""));
                typeBoxEditText.setText("");

                if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
                    byte[] bytes = sentText.getBytes(Charset.defaultCharset());
                    BluetoothConnectionService.write(bytes);
                }
                showLog("Exiting sendTextBtn");
            }
        });
    }

        private static void showLog (String message){
            Log.d(TAG, message);
        }

        public static TextView getMessageReceivedTextView() {
        return messageReceivedTextView;
    }
}
