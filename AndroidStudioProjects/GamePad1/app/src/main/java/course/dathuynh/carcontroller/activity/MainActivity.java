package course.dathuynh.carcontroller.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.markushi.ui.CircleButton;
import course.dathuynh.carcontroller.model.CarModel;
import course.dathuynh.carcontroller.Constants;
import course.dathuynh.carcontroller.nav.CustomExpandableListView;
import course.dathuynh.carcontroller.util.JSONParser;
import course.dathuynh.carcontroller.R;
import course.dathuynh.carcontroller.util.SocketClient;
import io.github.controlwear.virtual.joystick.android.JoystickView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Message";
    private String SERVER_IP = "";

    /* View */
    private TextView mTextViewAngleLeft;
    private TextView mTextViewStrengthLeft;
    private TextView mTextViewMessage;
    private TextView mTextViewReceive;
    private Button mButtonConnect;
    private CircleButton mButtonLeft;     //(signal left)
    private CircleButton mButtonRight;    //(signal right)
    private CircleButton mButtonBuzzer;
    private CircleButton mButtonLight;
    private CircleButton mButtonSafeMode;
    private JoystickView mJoystick;

    /*Variable for button status*/
    boolean light_isOn = false;
    boolean headlightLeft_isOn = false;
    boolean headlightRight_isOn = false;
    boolean SafeMode_isOn = false;
    public boolean DarkMode = false;

    /*Navigation Drawer*/
    private ExpandableListView expandableListView;
    private List<String> ListDataHeader;
    private HashMap<String, List<CarModel>> ListDataChild;
    private CustomExpandableListView customExpandableListView;
    private View listHeaderView;
    private List<CarModel> carModelList;
    private TextView user_name_header;

    //  navigation_drawer_open/close
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    /*Socket Client*/
    private SocketClient mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // <editor-fold defaultstate="collapsed" desc="Setup">
        /*Hide status bar*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_main);

        /*Shared Preferences*/
        SharedPreferences pref = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        String username = pref.getString(Constants.PREF_USERNAME, null);

        /*Get View from layout*/
        mTextViewAngleLeft = (TextView) findViewById(R.id.textView_angle_left);
        mTextViewStrengthLeft = (TextView) findViewById(R.id.textView_strength_left);
        mTextViewMessage = (TextView) findViewById(R.id.message_send);
        mTextViewReceive = (TextView) findViewById(R.id.message_receive);
        mButtonConnect = (Button) findViewById(R.id.btn_connect);
        mButtonBuzzer = (CircleButton) findViewById(R.id.button_buzzer);
        mButtonLeft = (CircleButton) findViewById(R.id.button_left);
        mButtonRight = (CircleButton) findViewById(R.id.button_right);
        mButtonLight = (CircleButton) findViewById(R.id.button_light);
        mButtonSafeMode = (CircleButton) findViewById(R.id.safe_mode);

        /*Declare Socket*/
        //new connectTask().execute("");

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="JoyStick Listener">
        mJoystick = (JoystickView) findViewById(R.id.joystickView_left);
        mJoystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleLeft.setText(angle + "°");
                mTextViewStrengthLeft.setText(strength + "%");
                if (strength > 50) {
                    //go
                    if (angle > 45 && angle < 135) {
                        //w
                        if (mSocket != null)
                            mSocket.sendMessage("w");

                        mTextViewMessage.setText("go");
                    }
                    //back
                    if (angle > 225 && angle < 315) {
                        //s
                        if (mSocket != null)
                            mSocket.sendMessage("s");

                        mTextViewMessage.setText("back");
                    }
                    //turn left
                    if (angle > 135 && angle < 225) {
                        //a
                        if (mSocket != null)
                            mSocket.sendMessage("a");

                        mTextViewMessage.setText("left");
                    }
                    //turn right
                    if ((angle > 0 && angle < 45) || (angle < 359 && angle > 315)) {
                        //d
                        if (mSocket != null)
                            mSocket.sendMessage("d");

                        mTextViewMessage.setText("right");
                    }
                }

            }
        }, 350);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Button onClick Listener">
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new connectTask().execute("");
                mButtonConnect.setTextColor(getResources().getColor(R.color.login_bnt));
            }
        });

        mButtonBuzzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //k
                if (mSocket != null)
                    mSocket.sendMessage("k");

                mTextViewMessage.setText("buzzer");
            }
        });
        mButtonLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (light_isOn) {
                    mButtonLight.setImageResource(R.drawable.lightoff);
                } else {
                    mButtonLight.setImageResource(R.drawable.lighton);
                }
                light_isOn = !light_isOn;

                //i
                if (mSocket != null)
                    mSocket.sendMessage("i");

                mTextViewMessage.setText("light");
            }
        });
        mButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (headlightLeft_isOn) {
                    mButtonLeft.setImageResource(R.drawable.headlightleftoff);
                    mButtonRight.setImageResource(R.drawable.headlightrightoff);

                } else {
                    mButtonLeft.setImageResource(R.drawable.headlightlefton);
                    mButtonRight.setImageResource(R.drawable.headlightrightoff);
                    headlightRight_isOn = false;
                }
                headlightLeft_isOn = !headlightLeft_isOn;

                //j
                if (mSocket != null)
                    mSocket.sendMessage("j");

                mTextViewMessage.setText("signal_left");
            }
        });
        mButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (headlightRight_isOn) {
                    mButtonRight.setImageResource(R.drawable.headlightrightoff);
                    mButtonLeft.setImageResource(R.drawable.headlightleftoff);
                } else {
                    mButtonRight.setImageResource(R.drawable.headlightrighton);
                    mButtonLeft.setImageResource(R.drawable.headlightleftoff);
                    headlightLeft_isOn = false;

                }
                headlightRight_isOn = !headlightRight_isOn;

                //l
                if (mSocket != null)
                    mSocket.sendMessage("l");

                mTextViewMessage.setText("signal_right");
            }
        });

        mButtonSafeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SafeMode_isOn) {
                    mButtonSafeMode.setImageResource(R.drawable.safemodeoff);
                    //enable joystick
                    mJoystick.setFixedCenter(true);
                    mJoystick.setEnabled(true);
                    SafeMode_isOn = !SafeMode_isOn;
                } else {
                    mButtonSafeMode.setImageResource(R.drawable.safemodeon);
                    //disable joystick
                    mJoystick.setEnabled(false);
                    mJoystick.setFixedCenter(true);
                    SafeMode_isOn = !SafeMode_isOn;
                }
            }
        });
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Navigation Drawer">
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //slideOffset changes from 0 to 1. 1 means it is completely open, 0 - closed.
                //Once offset changes from 0 to !0 - it means it started opening process.
                if (slideOffset != 0) {
                    mJoystick.setEnabled(false);
                    mJoystick.setFixedCenter(true);
                }
                if (slideOffset == 0) {
                    mJoystick.setFixedCenter(true);
                    if (SafeMode_isOn == false)
                        mJoystick.setEnabled(true);
                }

            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        listHeaderView = getLayoutInflater().inflate(R.layout.nav_header, null, false);
        user_name_header = (TextView) listHeaderView.findViewById(R.id.tv_user_name);
        user_name_header.setText(username);
        expandableListView.addHeaderView(listHeaderView);
        addData();
        customExpandableListView = new CustomExpandableListView(MainActivity.this,
                ListDataHeader, ListDataChild, DarkMode);
        expandableListView.setAdapter(customExpandableListView);


        // Handle Click ExpandableListView
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                String nameGroup = ListDataHeader.get(groupPosition);
                switch (nameGroup) {
                    case "Car Status":
                        AttemptGetCar attemptGetCar = new AttemptGetCar();
                        attemptGetCar.execute("getcar");
                        if (carModelList.size() == 0)
                            Toast.makeText(MainActivity.this, "There is no car", Toast.LENGTH_SHORT).show();

                        break;
                    case "Change Password":
                        Intent intent = new Intent(getApplicationContext(), ChangepasswordActivity.class);
                        startActivity(intent);
                        break;
                    case "Dark Mode":
                        DarkMode = !DarkMode;
                        changeMode();
                        break;
                    case "Server IP":
                        LayoutInflater li = LayoutInflater.from(MainActivity.this);
                        View customDialogView = li.inflate(R.layout.custom_dialog, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setView(customDialogView);

                        final EditText etName = (EditText) customDialogView.findViewById(R.id.et_name);

                        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        SERVER_IP = etName.getText().toString();
                                        Toast.makeText(MainActivity.this, etName.getText().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        break;
                    case "Log Out":
                        AlertDialog.Builder AlDi = new AlertDialog.Builder(MainActivity.this);
                        AlDi.setTitle("LogOut");
                        AlDi.setMessage("Are you sure you want to exit?");
                        AlDi.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlDi.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences pref = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.clear();
                                editor.commit();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                        AlDi.create().show();
                        break;

                }
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                String nameGroup = ListDataHeader.get(groupPosition);
                switch (nameGroup) {
                    case "Car Status":
                        break;
                    case "Change Password":
                        Intent intent = new Intent(getApplicationContext(), ChangepasswordActivity.class);
                        startActivity(intent);
                        break;
                    case "Dark Mode":
                        DarkMode = !DarkMode;
                        changeMode();
                        break;
                    case "Server IP":
                        LayoutInflater li = LayoutInflater.from(MainActivity.this);
                        View customDialogView = li.inflate(R.layout.custom_dialog, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setView(customDialogView);

                        final EditText etName = (EditText) customDialogView.findViewById(R.id.et_name);

                        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        SERVER_IP = etName.getText().toString();
                                        Toast.makeText(MainActivity.this, etName.getText().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        break;
                    case "Log Out":
                        AlertDialog.Builder AlDi = new AlertDialog.Builder(MainActivity.this);
                        AlDi.setTitle("LogOut");
                        AlDi.setMessage("Are you sure you want to exit?");
                        AlDi.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlDi.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences pref = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.clear();
                                editor.commit();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                        AlDi.create().show();
                        break;
                }
            }
        });
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Car list on Click listener">
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        "You select car " + (childPosition + 1),
                        Toast.LENGTH_SHORT).show();

                if (mSocket != null)
                    mSocket.sendMessage("Choose car " + (childPosition + 1));

                return false;
            }
        });
        // </editor-fold>
    }

    // <editor-fold defaultstate="collapsed" desc="don'nt know">

    // </editor-fold>

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSocket != null)
            mSocket.sendMessage("/quit");

        mSocket.stopClient();
    }

    // <editor-fold defaultstate="collapsed" desc="Connect Socket Server AsyncTask">
    public class connectTask extends AsyncTask<String, String, SocketClient> {

        @Override
        protected SocketClient doInBackground(String... message) {

            //we create a Client object and
            mSocket = new SocketClient(new SocketClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            }, SERVER_IP);
            mSocket.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mTextViewReceive.setText(values[0]);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get car list support">
    private class AttemptGetCar extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String _param = args[0];

            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("getcar", _param));

            JSONObject json = jsonParser.makeHttpRequest(Constants.URL_GetCar, "POST", params);

            return json;
        }

        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                try {
                    if (result.getInt("success") == 1)
                        onGetCarSuccess(result);
                    else onGetCarFail();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, String.valueOf(result));
            } else {
                Toast.makeText(getApplicationContext(), "Unable to retrieve any data from server", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onGetCarFail() {
        carModelList.clear();
    }

    private void onGetCarSuccess(JSONObject result) {
        try {
            // Getting JSON Array node
            JSONArray car = result.getJSONArray("car");
            carModelList.clear();
            for (int i = 0; i < car.length(); i++) {
                JSONObject carObj = car.getJSONObject(i);
                String name = carObj.getString("name");
                int state = carObj.getInt("state");
                // adding each child node to HashMap key => value
                CarModel carModel = new CarModel(name, state);
                carModelList.add(carModel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Function for get car event">

    private void changeMode() {
        customExpandableListView.darkmode = DarkMode;
        if (DarkMode == true) {
            expandableListView.setBackgroundResource(R.color.bg_menu_dark);
            listHeaderView.setBackgroundResource(R.color.bg_header_dark);
            user_name_header.setTextColor(getResources().getColor(R.color.text_username_dark));

        } else {
            expandableListView.setBackgroundResource(R.color.bg_memu);
            listHeaderView.setBackgroundResource(R.color.bg_header);
            user_name_header.setTextColor(getResources().getColor(R.color.text_username));
        }

    }

    private void addData() {
        AttemptGetCar attemptGetCar = new AttemptGetCar();
        attemptGetCar.execute("getcar");

        ListDataHeader = new ArrayList<>();
        ListDataChild = new HashMap<String, List<CarModel>>();

        // Header title
        ListDataHeader.add("Car Status");
        ListDataHeader.add("Change Password");
        ListDataHeader.add("Dark Mode");
        ListDataHeader.add("Server IP");
        ListDataHeader.add("Log Out");

        //CarModel[name; isOnline, isChoose]
        carModelList = new ArrayList<CarModel>();

        List<CarModel> itemNull = new ArrayList<CarModel>();
        // add data Hash map
        ListDataChild.put(ListDataHeader.get(0), carModelList);
        ListDataChild.put(ListDataHeader.get(1), itemNull);
        ListDataChild.put(ListDataHeader.get(2), itemNull);
        ListDataChild.put(ListDataHeader.get(3), itemNull);
        ListDataChild.put(ListDataHeader.get(4), itemNull);
    }
    // </editor-fold>
    // </editor-fold>
}