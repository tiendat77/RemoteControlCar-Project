package com.dathuynh.dieukhienxe.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;


import com.dathuynh.dieukhienxe.Constants;
import com.dathuynh.dieukhienxe.R;
import com.dathuynh.dieukhienxe.model.CarModel;
import com.dathuynh.dieukhienxe.navdrawer.CustomExpandableListView;

import at.markushi.ui.CircleButton;
import io.github.controlwear.virtual.joystick.android.JoystickView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Message";

    //Variable for control
    private SocketClient msocketClient;
    private Handler handler;

    private TextView mTextViewAngleLeft;
    private TextView mTextViewStrengthLeft;
    private TextView mTextViewMessage;
    private TextView mTextViewReceive;
    private Button mButtonConnect;
    private CircleButton mButtonLeft;
    private CircleButton mButtonRight;
    private CircleButton mButtonBuzzer;
    private CircleButton mButtonLight;
    private CircleButton mButtonSafeMode;
    private JoystickView mJoystick;

    boolean light_isOn = false;
    boolean headlightLeft_isOn = false;
    boolean headlightRight_isOn = false;
    boolean safemode_isOn = false;
    public boolean darkmode = false;

    public String SERVER_IP = "192.168.4.1";

    //Variable for navigation drawer
    DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    ExpandableListView expandableListView;
    List<String> listdataHeader; // text cua header
    HashMap<String, List<CarModel>> listdataChild;
    CustomExpandableListView customExpandableListView;
    View listHeaderView;
    TextView user_name_header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // <editor-fold defaultstate="collapsed" desc="Setup">
        // hide status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        String username = pref.getString(Constants.PREF_USERNAME, null);

        //Get view for control
        mTextViewAngleLeft = (TextView) findViewById(R.id.textView_angle_left);
        mTextViewStrengthLeft = (TextView) findViewById(R.id.textView_strength_left);
        mTextViewMessage = (TextView) findViewById(R.id.message_send);
        mTextViewReceive = (TextView) findViewById(R.id.message_receive);
        mButtonBuzzer = (CircleButton) findViewById(R.id.button_buzzer);
        mButtonLeft = (CircleButton) findViewById(R.id.button_left);
        mButtonRight = (CircleButton) findViewById(R.id.button_right);
        mButtonLight = (CircleButton) findViewById(R.id.button_light);
        mButtonSafeMode = (CircleButton) findViewById(R.id.safe_mode);
        mButtonConnect = (Button) findViewById(R.id.button_connect);

        mJoystick = (JoystickView) findViewById(R.id.joystickView_left);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mTextViewReceive.setText((String) msg.obj);
            }
        };
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="JoyStick onMove Listener">
        mJoystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleLeft.setText(angle + "°");
                mTextViewStrengthLeft.setText(strength + "%");
                if (strength > 50) {
                    //go
                    if (angle > 45 && angle < 135) {
                        //w
                        if (msocketClient != null)
                            msocketClient.sendMessage("w");

                        mTextViewMessage.setText("go");
                    }
                    //back
                    if (angle > 225 && angle < 315) {
                        //s
                        if (msocketClient != null)
                            msocketClient.sendMessage("s");

                        mTextViewMessage.setText("back");
                    }
                    //turn left
                    if (angle > 135 && angle < 225) {
                        //a
                        if (msocketClient != null)
                            msocketClient.sendMessage("a");

                        mTextViewMessage.setText("left");
                    }
                    //turn right
                    if ((angle > 0 && angle < 45) || (angle < 359 && angle > 315)) {
                        //d
                        if (msocketClient != null)
                            msocketClient.sendMessage("d");

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
                connect();
            }
        });

        mButtonBuzzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //k
                    msocketClient.sendMessage("k");

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
                if (msocketClient != null)
                    msocketClient.sendMessage("i");

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
                if (msocketClient != null)
                    msocketClient.sendMessage("j");

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
                if (msocketClient != null)
                    msocketClient.sendMessage("l");

                mTextViewMessage.setText("signal_right");
            }
        });

        mButtonSafeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (safemode_isOn) {
                    mButtonSafeMode.setImageResource(R.drawable.safemodeoff);
                    //enable joystick
                    mJoystick.setFixedCenter(true);
                    mJoystick.setEnabled(true);
                    safemode_isOn = !safemode_isOn;
                } else {
                    mButtonSafeMode.setImageResource(R.drawable.safemodeon);
                    //disable joystick
                    mJoystick.setEnabled(false);
                    mJoystick.setFixedCenter(true);
                    safemode_isOn = !safemode_isOn;
                }
            }
        });
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Navigation Drawer">
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset != 0) {
                    mJoystick.setEnabled(false);
                    mJoystick.setFixedCenter(true);
                }
                if (slideOffset == 0) {
                    mJoystick.setFixedCenter(true);
                    if (safemode_isOn == false)
                        mJoystick.setEnabled(true);
                }
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        // handle listview + thêm header cho expandable listview
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        listHeaderView = getLayoutInflater().inflate(R.layout.nav_header, null, false);
        user_name_header = (TextView) listHeaderView.findViewById(R.id.tv_user_name);
        user_name_header.setText(username);
        expandableListView.addHeaderView(listHeaderView);
        addData();
        customExpandableListView = new CustomExpandableListView(MainActivity.this, listdataHeader, listdataChild, darkmode);
        expandableListView.setAdapter(customExpandableListView);

        // Handle Click ExpandableListView
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                String nameGroup = listdataHeader.get(groupPosition);
                switch (nameGroup) {
                    case "Dark Mode":
                        darkmode = !darkmode;
                        changeMode();
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
                    case "Car IP":
                        LayoutInflater li = LayoutInflater.from(MainActivity.this);
                        View customDialogView = li.inflate(R.layout.custom_dialog, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setView(customDialogView);

                        final EditText etName = (EditText) customDialogView.findViewById(R.id.et_name);

                        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        SERVER_IP = etName.getText().toString();
                                        connect();
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
                }
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                String nameGroup = listdataHeader.get(groupPosition);
                switch (nameGroup) {
                    case "Dark Mode":
                        darkmode = !darkmode;
                        changeMode();
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
                    case "Car IP":
                        LayoutInflater li = LayoutInflater.from(MainActivity.this);
                        View customDialogView = li.inflate(R.layout.custom_dialog, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setView(customDialogView);

                        final EditText etName = (EditText) customDialogView.findViewById(R.id.et_name);

                        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        SERVER_IP = etName.getText().toString();
                                        connect();
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
                }
            }
        });
        // </editor-fold>
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void connect() {
        msocketClient = new SocketClient(SERVER_IP);
        new Thread(msocketClient).start();
    }


    // <editor-fold defaultstate="collapsed" desc="Socket TCP">
    public class SocketClient implements Runnable {

        public Socket socket;
        private String SERVER_IP;
        BufferedReader is = null;

        public SocketClient(String SERVER_IP) {
            if (SERVER_IP.equals(""))
                this.SERVER_IP = Constants.SERVER_IP;
            else
                this.SERVER_IP = SERVER_IP;
        }

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, Constants.SERVER_PORT);
                mButtonConnect.setTextColor(getResources().getColor(R.color.state_online));
                while (!Thread.currentThread().isInterrupted()) {
                    this.is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = is.readLine();
                    //handler update ui
                    Message msg = handler.obtainMessage(1, (String) message);
                    handler.sendMessage(msg);
                }
            } catch (UnknownHostException e1) {
                Toast.makeText(getBaseContext(), "Unknown Host!", Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }


        public void sendMessage(final String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                        out.println(message);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


    }
    // </editor-fold>

    private void changeMode() {

        customExpandableListView.darkmode = darkmode;
        if (darkmode == true) {
            expandableListView.setBackgroundResource(R.color.bg_menu_dark);
            listHeaderView.setBackgroundResource(R.color.bg_header_dark);
            user_name_header.setTextColor(getResources().getColor(R.color.text_username_dark));

        } else {
            expandableListView.setBackgroundResource(R.color.bg_memu);
            listHeaderView.setBackgroundResource(R.color.bg_header);
            user_name_header.setTextColor(getResources().getColor(R.color.text_username));
        }

    }

    // tao du lieu
    private void addData() {

        listdataHeader = new ArrayList<>();
        listdataChild = new HashMap<String, List<CarModel>>();

        // tao du lieu cho header
        listdataHeader.add("Car IP");
        listdataHeader.add("Dark Mode");
        listdataHeader.add("Log Out");

        List<CarModel> itemnull = new ArrayList<CarModel>();
        // add data Hash Map
        listdataChild.put(listdataHeader.get(0), itemnull);
        listdataChild.put(listdataHeader.get(1), itemnull);
        listdataChild.put(listdataHeader.get(2), itemnull);
    }
    // </editor-fold>
}