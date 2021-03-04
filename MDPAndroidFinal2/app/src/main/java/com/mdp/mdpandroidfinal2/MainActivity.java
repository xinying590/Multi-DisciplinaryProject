package com.mdp.mdpandroidfinal2;
import com.mdp.mdpandroidfinal2.ui.main.ReconfigureFragment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.tabs.TabLayout;
import com.mdp.mdpandroidfinal2.R;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mdp.mdpandroidfinal2.ui.main.BluetoothConnectionService;
import com.mdp.mdpandroidfinal2.ui.main.BluetoothPopUp;
import com.mdp.mdpandroidfinal2.ui.main.CommsPopUp;
import com.mdp.mdpandroidfinal2.ui.main.GridMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Main Activity
    //Protocol String
    String MOV_FOR = "ARD|F";
    String TURN_LEFT = "ARD|L";
    String TURN_RIGHT = "ARD|R";
    String MOV_BACK = "ARD|REV";
    String START_EXP ="START_EXP";
    String START_FP= "START_FP";


    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Context context;


    private static GridMap gridMap;
    static TextView xAxisTextView, yAxisTextView, directionAxisTextView;
    static TextView robotStatusTextView;
    static Button f1, f2;
    Button reconfigure;
    Button startExpButton;
    Button startFPButton;
    ReconfigureFragment reconfigureFragment = new ReconfigureFragment();

    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;
    private static UUID myUUID;
    ProgressDialog myDialog;

    private static final String TAG = "Main Activity";

    //Control Fragment
    ImageButton moveForwardImageBtn, turnRightImageBtn, moveBackImageBtn, turnLeftImageBtn, exploreResetButton, fastestResetButton;
//    private static long exploreTimer, fastestTimer;
//    ToggleButton exploreButton, fastestButton;
//    TextView exploreTimeTextView, fastestTimeTextView, robotStatusTextView;
    Switch phoneTiltSwitch;
    static Button calibrateButton;
//    private static GridMap gridMap;

    private Sensor mSensor;
    private SensorManager mSensorManager;

    //MapTabFragment
    Button resetMapBtn, updateButton;
    ImageButton directionChangeImageBtn, exploredImageBtn, obstacleImageBtn, clearImageBtn;
    ToggleButton setStartPointToggleBtn, setWaypointToggleBtn;
    Switch manualAutoToggleBtn;
    private static boolean autoUpdate = false;
    public static boolean manualUpdateRequest = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //MainActivity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
//        ViewPager viewPager = findViewById(R.id.view_pager);
//        viewPager.setAdapter(sectionsPagerAdapter);
//        viewPager.setOffscreenPageLimit(9999);
//        TabLayout tabs = findViewById(R.id.tabs);
//        tabs.setupWithViewPager(viewPager);

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("incomingMessage"));

        // Set up sharedPreferences
        MainActivity.context = getApplicationContext();
        this.sharedPreferences();
        editor.putString("message", "");
        editor.putString("direction","None");
        editor.putString("connStatus", "Disconnected");
        editor.commit();

        Button bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popup = new Intent(MainActivity.this, BluetoothPopUp.class); // changing to the bluetooth pop up ui
                startActivity(popup);
            }
        });

        Button chatButton = (Button) findViewById(R.id.chatButton);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BluetoothPopUp", "Hi0");
                Intent popup= new Intent(MainActivity.this, CommsPopUp.class); // changing to the comms pop up ui
                startActivity(popup);
            }
        });

        startExpButton = (Button) findViewById(R.id.startExpButton);
        startExpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Pressed start exploration button");
                printMessage(START_EXP);

            }
        });

        startFPButton = (Button) findViewById(R.id.startFPButton);
        startFPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Pressed fastest path button");
                printMessage(START_FP);

            }
        });

        //Map
        gridMap = new GridMap(this);
        gridMap = findViewById(R.id.mapView);
        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        directionAxisTextView = findViewById(R.id.directionAxisTextView);

        // Robot Status
        robotStatusTextView = findViewById(R.id.robotStatusTextView);

        myDialog = new ProgressDialog(MainActivity.this);
        myDialog.setMessage("Waiting for other device to reconnect...");
        myDialog.setCancelable(false);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Persistence strings
        f1 = (Button) findViewById(R.id.f1ActionButton);
        f2 = (Button) findViewById(R.id.f2ActionButton);
        reconfigure = (Button) findViewById(R.id.configureButton);

        if (sharedPreferences.contains("F1")) {
            f1.setContentDescription(sharedPreferences.getString("F1", ""));
            showLog("setText for f1Btn: " + f1.getContentDescription().toString());
        }
        if (sharedPreferences.contains("F2")) {
            f2.setContentDescription(sharedPreferences.getString("F2", ""));
            showLog("setText for f2Btn: " + f2.getContentDescription().toString());
        }

        f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked f1Btn");
                if (!f1.getContentDescription().toString().equals("empty"))
                    MainActivity.printMessage(f1.getContentDescription().toString());
                showLog("f1Btn value: " + f1.getContentDescription().toString());
                showLog("Exiting f1Btn");
            }
        });

        f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked f2Btn");
                if (!f2.getContentDescription().toString().equals("empty"))
                    MainActivity.printMessage(f2.getContentDescription().toString());
                showLog("f2Btn value: " + f2.getContentDescription().toString());
                showLog("Exiting f2Btn");
            }
        });

        reconfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked reconfigureBtn");
                reconfigureFragment.show(getFragmentManager(), "Reconfigure Fragment");
                showLog("Exiting reconfigureBtn");
            }
        });


        //Control Fragment
        moveForwardImageBtn = findViewById(R.id.forwardImageBtn);
        turnRightImageBtn = findViewById(R.id.rightImageBtn);
        moveBackImageBtn = findViewById(R.id.backImageBtn);
        turnLeftImageBtn = findViewById(R.id.leftImageBtn);
        phoneTiltSwitch = findViewById(R.id.phoneTiltSwitch);
        calibrateButton = findViewById(R.id.calibrateButton);

        moveForwardImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked moveForwardImageBtn");
//                Log.d(TAG, "Clicked moveForwardImageBtn");
                if (gridMap.getAutoUpdate())
                    updateStatus("Please press 'MANUAL'");
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("forward");
                   refreshLabel();
                    if (gridMap.getValidPosition())
                        updateStatus("moving forward");
                    else
                        updateStatus("Unable to move forward");
                    showLog("Print message from onclick forward");
                    printMessage(MOV_FOR);
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
                showLog("Exiting moveForwardImageBtn");
            }
        });

        turnRightImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked turnRightImageBtn");
                if (gridMap.getAutoUpdate())
                    updateStatus("Please press 'MANUAL'");
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("right");
                    refreshLabel();
                    showLog("Print message from onclick right");
                    printMessage(TURN_RIGHT);
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
                showLog("Exiting turnRightImageBtn");
            }
        });

        moveBackImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked moveBackwardImageBtn");
                if (gridMap.getAutoUpdate())
                    updateStatus("Please press 'MANUAL'");
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("back");
                    refreshLabel();
                    if (gridMap.getValidPosition())
                        updateStatus("moving backward");
                    else
                        updateStatus("Unable to move backward");
                    showLog("Print message from onclick back");
                    printMessage(MOV_BACK);
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
                showLog("Exiting moveBackwardImageBtn");
            }
        });

        turnLeftImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked turnLeftImageBtn");
                if (gridMap.getAutoUpdate())
                    updateStatus("Please press 'MANUAL'");
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    gridMap.moveRobot("left");
                    refreshLabel();
                    updateStatus("turning left");
                    showLog("Print message from onclick left");
                    printMessage(TURN_LEFT);
                }
                else
                    updateStatus("Please press 'STARTING POINT'");
                showLog("Exiting turnLeftImageBtn");
            }
        });

//        mSensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        phoneTiltSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (gridMap.getAutoUpdate()) {
                    updateStatus("Please press 'MANUAL'");
                    phoneTiltSwitch.setChecked(false);
                }
                else if (gridMap.getCanDrawRobot() && !gridMap.getAutoUpdate()) {
                    if(phoneTiltSwitch.isChecked()){
                        //showToast("Tilt motion control: ON");
                        phoneTiltSwitch.setPressed(true);

                        mSensorManager.registerListener(MainActivity.this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);
                        sensorHandler.post(sensorDelay);
                    }else{
                        showToast("Tilt motion control: OFF");
                        showLog("unregistering Sensor Listener");
                        try {
                            mSensorManager.unregisterListener(MainActivity.this);
                        }catch(IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        sensorHandler.removeCallbacks(sensorDelay);
                    }
                } else {
                    updateStatus("Please press 'STARTING POINT'");
                    phoneTiltSwitch.setChecked(false);
                }
                if(phoneTiltSwitch.isChecked()){
                    compoundButton.setText("TILT ON");
                }else
                {
                    compoundButton.setText("TILT OFF");
                }
            }
        });

        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked Calibrate Button");
                MainActivity.printMessage("SS|");
                manualUpdateRequest = true;
                showLog("Exiting Calibrate Button");
            }
        });



        //MapTabFragment
        resetMapBtn = findViewById(R.id.resetMapBtn);
        setStartPointToggleBtn = findViewById(R.id.setStartPointToggleBtn);
        setWaypointToggleBtn = findViewById(R.id.setWaypointToggleBtn);
//        directionChangeImageBtn = findViewById(R.id.directionChangeImageBtn);
//        exploredImageBtn = findViewById(R.id.exploredImageBtn);
//        obstacleImageBtn = findViewById(R.id.obstacleImageBtn);
//        clearImageBtn = findViewById(R.id.clearImageBtn);
        manualAutoToggleBtn = findViewById(R.id.manualAutoToggleBtn);
        updateButton = findViewById(R.id.updateButton);
        

        resetMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked resetMapBtn");
                updateStatus("Reseting map...");
                gridMap.resetMap();
            }
        });


        setStartPointToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked setStartPointToggleBtn");
                if (setStartPointToggleBtn.getText().equals("STARTING POINT"))
                    updateStatus("Cancelled selecting starting point");
                else if (setStartPointToggleBtn.getText().equals("CANCEL") && !gridMap.getAutoUpdate()) {
                    updateStatus("Please select starting point");
                    gridMap.setStartCoordStatus(true); // The start coordinates have been set
                    gridMap.toggleCheckedBtn("setStartPointToggleBtn");
                } else
                    updateStatus("Please select manual mode");
                showLog("Exiting setStartPointToggleBtn");
            }
        });

        setWaypointToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked setWaypointToggleBtn");
                if (setWaypointToggleBtn.getText().equals("WAYPOINT"))
                    updateStatus("Cancelled selecting waypoint");
                else if (setWaypointToggleBtn.getText().equals("CANCEL")) {
                    updateStatus("Please select waypoint");
                    gridMap.setWaypointStatus(true);
                    gridMap.toggleCheckedBtn("setWaypointToggleBtn");
                }
                else
                    updateStatus("Please select manual mode");
                showLog("Exiting setWaypointToggleBtn");
            }
        });


        // TODO: Add direction fragment code and uncomment
//        directionChangeImageBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showLog("Clicked directionChangeImageBtn");
//                directionFragment.show(getActivity().getFragmentManager(), "Direction Fragment");
//                showLog("Exiting directionChangeImageBtn");
//            }
//        });

//        exploredImageBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showLog("Clicked exploredImageBtn");
//                if (!gridMap.getExploredStatus()) {
//                    updateStatus("Please check cell");
//                    gridMap.setExploredStatus(true);
//                    gridMap.toggleCheckedBtn("exploredImageBtn");
//                }
//                else if (gridMap.getExploredStatus())
//                    gridMap.setSetObstacleStatus(false);
//                showLog("Exiting exploredImageBtn");
//            }
//        });

//        obstacleImageBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showLog("Clicked obstacleImageBtn");
//                if (!gridMap.getSetObstacleStatus()) {
//                    updateStatus("Please plot obstacles");
//                    gridMap.setSetObstacleStatus(true);
//                    gridMap.toggleCheckedBtn("obstacleImageBtn");
//                }
//                else if (gridMap.getSetObstacleStatus())
//                    gridMap.setSetObstacleStatus(false);
//                showLog("Exiting obstacleImageBtn");
//            }
//        });

//        clearImageBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showLog("Clicked clearImageBtn");
//                if (!gridMap.getUnSetCellStatus()) {
//                    updateStatus("Please remove cells");
//                    gridMap.setUnSetCellStatus(true);
//                    gridMap.toggleCheckedBtn("clearImageBtn");
//                }
//                else if (gridMap.getUnSetCellStatus())
//                    gridMap.setUnSetCellStatus(false);
//                showLog("Exiting clearImageBtn");
//            }
//        });

        manualAutoToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked manualAutoToggleBtn");
                if (manualAutoToggleBtn.getText().equals("Manual")) {
                    try {
                        gridMap.setAutoUpdate(true);
                        autoUpdate = true;
                        gridMap.toggleCheckedBtn("None");
                        updateButton.setClickable(false);
                        updateButton.setTextColor(Color.GRAY);
//                        ControlFragment.getCalibrateButton().setClickable(false);
//                        ControlFragment.getCalibrateButton().setTextColor(Color.GRAY);
                        manualAutoToggleBtn.setText("Auto");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    updateStatus("AUTO mode");
                }
                else if (manualAutoToggleBtn.getText().equals("Auto")) {
                    try {
                        gridMap.setAutoUpdate(false);
                        autoUpdate = false;
                        gridMap.toggleCheckedBtn("None");
                        updateButton.setClickable(true);
                        updateButton.setTextColor(Color.BLACK);
//                        ControlFragment.getCalibrateButton().setClickable(true);
//                        ControlFragment.getCalibrateButton().setTextColor(Color.BLACK);
                        manualAutoToggleBtn.setText("Manual");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    updateStatus("MANUAL mode");
                }
                showLog("Exiting manualAutoToggleBtn");
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("Clicked updateButton");
                showLog("Print message from onclick update");
                MainActivity.printMessage("sendArena");
                manualUpdateRequest = true;
                showLog("Exiting updateButton");
                // Removed default loading of obstacles
//                try {
//                    String message = "{\"map\":[{\"explored\": \"ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff\",\"length\":300,\"obstacle\":\"00000000000000000706180400080010001e000400000000200044438f840000000000000080\"}]}";
//
//                    gridMap.setReceivedJsonObject(new JSONObject(message));
//                    gridMap.updateMapInformation();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        });
    }




    //Main Activity
//    public static GridMap getGridMap() {
//        return gridMap;
//    }

    public static void sharedPreferences() {
        sharedPreferences = MainActivity.getSharedPreferences(MainActivity.context);
        editor = sharedPreferences.edit();
    }

    // Send message via bluetooth
    //TODO: Check getMessageReceivedTextView()
    public static void printMessage(String message) {
        showLog("Entering printMessage1");
        editor = sharedPreferences.edit();

        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
        showLog(message);
//        editor.putString("message", CommsPopUp.getMessageReceivedTextView().getText() + "\n" + message);
//        editor.commit();
//        refreshMessageReceived();
        showLog("Exiting printMessage");
    }

    public static void printMessage(String name, int x, int y) throws JSONException {
        showLog("Entering printMessage2");
        sharedPreferences();

        JSONObject jsonObject = new JSONObject();
        String message;

        switch(name) {
//            case "starting":
            case "waypoint":
                jsonObject.put(name, name);
                jsonObject.put("x", x);
                jsonObject.put("y", y);
                message = name + " (" + x + "," + y + ")";
                break;
            default:
                message = "Unexpected default for printMessage: " + name;
                break;
        }
        Log.d(TAG, "E1"); //TODO: Check the editor.putString line
//        editor.putString("message", CommsPopUp.getMessageReceivedTextView().getText() + "\n" + message);
//        Log.d(TAG, "E2");
        editor.commit();
//        Log.d(TAG, "E3");
        if (BluetoothConnectionService.BluetoothConnectionStatus == true) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            BluetoothConnectionService.write(bytes);
        }
        Log.d(TAG, "E4");
        showLog("Exiting printMessage");
    }

    public static void refreshMessageReceived() {
        System.out.println("hey" + CommsPopUp.getMessageReceivedTextView());
        CommsPopUp.getMessageReceivedTextView().setText(sharedPreferences.getString("message", ""));
    }

    public void refreshDirection(String direction) {
        gridMap.setRobotDirection(direction);
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
        showLog("Print message from refreshDirection");
        printMessage("Direction is set to " + direction);
    }



    public static void refreshLabel() {
        xAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[0]-1));
        yAxisTextView.setText(String.valueOf(gridMap.getCurCoord()[1]-1));
        directionAxisTextView.setText(sharedPreferences.getString("direction",""));
    }

    public static void receiveMessage(String message) {
        showLog("Entering receiveMessage");
        sharedPreferences();
        editor.putString("message", sharedPreferences.getString("message", "") + "\n" + message);
        editor.commit();
        showLog("Exiting receiveMessage");
    }

    private static void showLog(String message) {
        Log.d(TAG, message);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
    }

    private BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice mDevice = intent.getParcelableExtra("Device");
            String status = intent.getStringExtra("Status");
            sharedPreferences();

            if(status.equals("connected")){
                try {
                    myDialog.dismiss();
                } catch(NullPointerException e){
                    e.printStackTrace();
                }

                Log.d(TAG, "mBroadcastReceiver5: Device now connected to "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Device now connected to "+mDevice.getName(), Toast.LENGTH_LONG).show();
                editor.putString("connStatus", "Connected to " + mDevice.getName());
//                TextView connStatusTextView = findViewById(R.id.connStatusTextView);
//                connStatusTextView.setText("Connected to " + mDevice.getName());
            }
            else if(status.equals("disconnected")){
                Log.d(TAG, "mBroadcastReceiver5: Disconnected from "+mDevice.getName());
                Toast.makeText(MainActivity.this, "Disconnected from "+mDevice.getName(), Toast.LENGTH_LONG).show();
//                mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
//                mBluetoothConnection.startAcceptThread();

                editor.putString("connStatus", "Disconnected");
//                TextView connStatusTextView = findViewById(R.id.connStatusTextView);
//                connStatusTextView.setText("Disconnected");

                myDialog.show();
            }
            editor.commit();
        }
    };

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("receivedMessage");
            showLog("receivedMessage: message --- " + message);
//            message = "AND|P1|P2";
//            String str = message;
//            ArrayList<String> messageList = new ArrayList<>(Arrays.asList(str.split(",")));
//            showLog(messageList.get(0));
//            showLog(messageList.get(1));

//              String[] tmp = message.split("[,]");
//              showLog(tmp[0]);
//            showLog(tmp[1]);



            // Parsing obj with ID
            if (message.length() == 14){
                if (message.substring(0,3).equals("obj")){
                    showLog(message.substring(0,3));
                   int x_coord = Integer.parseInt(message.substring(4, 6));
                   showLog(message.substring(4, 6));
                   int y_coord = Integer.parseInt(message.substring(7, 9));
                   showLog(message.substring(7, 9));
                   int id = Integer.parseInt(message.substring(11, 13));
                   showLog(message.substring(11, 13));
                   gridMap.drawImageNumberCell(x_coord, y_coord, id);

                }
            }

            // Parsing robot status
            if (message.length() > 11) {
//                showLog(message.substring(2, 8));
                if (message.substring(2, 8).equals("status")) {

                    String status = message.substring(11, message.length()-2);
                    showLog(status);
                    setRobotStatusTextView(status);
                }
            }

            try {
                if (message.length() > 7 && message.substring(2,6).equals("grid")) {
                    String resultString = "";
                    String amdString = message.substring(11,message.length()-2);
                    showLog("amdString: " + amdString);
                    BigInteger hexBigIntegerExplored = new BigInteger(amdString, 16);
                    String exploredString = hexBigIntegerExplored.toString(2);

                    while (exploredString.length() < 300)
                        exploredString = "0" + exploredString;

                    for (int i=0; i<exploredString.length(); i=i+15) {
                        int j=0;
                        String subString = "";
                        while (j<15) {
                            subString = subString + exploredString.charAt(j+i);
                            j++;
                        }
                        resultString = subString + resultString;
                    }
                    hexBigIntegerExplored = new BigInteger(resultString, 2);
                    resultString = hexBigIntegerExplored.toString(16);

                    JSONObject amdObject = new JSONObject();
                    amdObject.put("explored", "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
                    amdObject.put("length", amdString.length()*4);
                    amdObject.put("obstacle", resultString);
                    JSONArray amdArray = new JSONArray();
                    amdArray.put(amdObject);
                    JSONObject amdMessage = new JSONObject();
                    amdMessage.put("map", amdArray);
                    message = String.valueOf(amdMessage);
                    showLog("Executed for AMD message, message: " + message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if (message.length() > 8 && message.substring(2,7).equals("image")) {
                    JSONObject jsonObject = new JSONObject(message);
                    JSONArray jsonArray = jsonObject.getJSONArray("image");
                    gridMap.drawImageNumberCell(jsonArray.getInt(0),jsonArray.getInt(1),jsonArray.getInt(2));
                    showLog("Image Added for index: " + jsonArray.getInt(0) + "," +jsonArray.getInt(1));
                }
            } catch (JSONException e) {
                showLog("Adding Image Failed");
            }

            if (gridMap.getAutoUpdate() || manualUpdateRequest) {
                try {
                    gridMap.setReceivedJsonObject(new JSONObject(message));
                    gridMap.updateMapInformation();
                    manualUpdateRequest = false;
                    showLog("messageReceiver: try decode successful");
                } catch (JSONException e) {
                    showLog("messageReceiver: try decode unsuccessful");
                }
            }
            sharedPreferences();
            String receivedText = sharedPreferences.getString("message", "") + "\n" + message;
            editor.putString("message", receivedText);
            editor.commit();
            //TODO: Put received message in chatbox. This is needed to show message received
//            refreshMessageReceived();
        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    mBTDevice = (BluetoothDevice) data.getExtras().getParcelable("mBTDevice");
                    myUUID = (UUID) data.getSerializableExtra("myUUID");
                }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver5);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter2 = new IntentFilter("ConnectionStatus");
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver5, filter2);
        } catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putString(TAG, "onSaveInstanceState");
        showLog("Exiting onSaveInstanceState");
    }


    public void setRobotStatusTextView(String status) {
        robotStatusTextView.setText(status);
    }

    public static Button getF1() { return f1; }

    public static Button getF2() { return f2; }


    public static TextView getRobotStatusTextView() {  return robotStatusTextView; }



    //Control Fragment
    public  void updateStatus(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP,0, 0);
        toast.show();
    }

    Handler sensorHandler = new Handler();
    boolean sensorFlag= false;

    private final Runnable sensorDelay = new Runnable() {
        @Override
        public void run() {
            sensorFlag = true;
            sensorHandler.postDelayed(this,1000);
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        String TAGsensor = "SensorChanged";
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        Log.d(TAGsensor, "X: "+x);
        Log.d(TAGsensor, "Y: "+y);
        Log.d(TAGsensor, "Z: "+z);


//        showLog("SensorChanged X: "+x);
//        showLog("SensorChanged Y: "+y);
//        showLog("SensorChanged Z: "+z);

        if(sensorFlag) {
            if (y < -2) {
                showLog("Sensor Move Forward Detected");
                gridMap.moveRobot("forward");
                MainActivity.refreshLabel();
                MainActivity.printMessage("f");
            } else if (y > 2) {
                showLog("Sensor Move Backward Detected");
                gridMap.moveRobot("back");
                MainActivity.refreshLabel();
                MainActivity.printMessage("r");
            } else if (x > 2) {
                showLog("Sensor Move Left Detected");
                gridMap.moveRobot("left");
                MainActivity.refreshLabel();
                MainActivity.printMessage("tl");
            } else if (x < -2) {
                showLog("Sensor Move Right Detected");
                gridMap.moveRobot("right");
                MainActivity.refreshLabel();
                MainActivity.printMessage("tr");
            }
        }
        sensorFlag = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static Button getCalibrateButton() {
        return calibrateButton;
    }
    public Switch getTiltSwitch() {
        return phoneTiltSwitch;

    }
}
