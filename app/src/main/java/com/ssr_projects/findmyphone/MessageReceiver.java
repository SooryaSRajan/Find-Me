package com.ssr_projects.findmyphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ssr_projects.findmyphone.Adapter.NumberDataHolder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

import static com.ssr_projects.findmyphone.AddContactsActivity.PREF_ARRAY_LIST;

public class MessageReceiver extends BroadcastReceiver {

    String TAG = getClass().getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, MusicService.class);
        myIntent.setAction("ACTION_PLAY_AUDIO");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String keyWord = sharedPreferences.getString("keyword", null);

        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String address = smsMessage.getDisplayOriginatingAddress();
                Log.d(TAG, "onReceive: Address:: " + address);
                String messageBody = smsMessage.getMessageBody();
                Log.e(TAG, "onReceive: " + messageBody);

                if (sharedPreferences.getBoolean("is_custom_emabled", false)) {
                    //Check if mode enabled
                    Log.d(TAG, "onReceive: Number Mode Enabled");
                    final NumberDataHolder numberDataHolder = new NumberDataHolder("NA", address);
                    ArrayList<NumberDataHolder> arrayList;
                    if ((arrayList = getArrayList("set_array_list", context)) != null) {
                        //Gets array list
                        Log.d(TAG, "onReceive: Array List is availabe");
                        boolean itemExists = arrayList.stream().anyMatch(new Predicate<NumberDataHolder>() {
                            @Override
                            public boolean test(NumberDataHolder c) {
                                Log.d(TAG, "onReceive: Number is valid");
                                return c.getUserNumber().equals(numberDataHolder.getUserNumber());
                            }
                        });

                        if (itemExists) {
                            //valid number
                            Log.d(TAG, "onReceive: Number is valid, if conditon satisfied");
                            expirationEnabledBlock(sharedPreferences, messageBody, keyWord, context, myIntent);
                        }
                        else{
                            Log.d(TAG, "onReceive: Number is invalid, if conditon failed");
                            //Not a valid number
                        }
                    }
                    else{
                        Log.d(TAG, "onReceive: Array List is invalid");
                        //Array list does not exist
                    }


                } else {
                    //Number mode disabled, starts right away
                    Log.d(TAG, "onReceive:/Number mode disabled, starting function");
                    expirationEnabledBlock(sharedPreferences, messageBody, keyWord, context, myIntent);
                }
            }

        } else if (Objects.equals(intent.getAction(), "ACTION_STOP_AUDIO")) {
            context.stopService(intent);
            Toast.makeText(context, "Stopping audio", Toast.LENGTH_SHORT).show();
        }
    }

    public void startProtocol(Context context, Intent myIntent) {
        ConstantClass.isServiceRunning = true;
        Toast.makeText(context, "Command Received", Toast.LENGTH_SHORT).show();
        context.startService(myIntent);
    }

    public void expirationEnabledBlock(SharedPreferences sharedPreferences, String messageBody, String keyWord, Context context, Intent myIntent){
        if (sharedPreferences.getBoolean("expiration", false)) {
            //Expires if eabled
            if (sharedPreferences.getBoolean("is_valid", false)) {
                //Checks validity
                if (messageBody.equals(keyWord)) {
                    //Matches Keyword
                    sharedPreferences.edit().putBoolean("is_valid", false).apply();
                    if (!ConstantClass.isServiceRunning) {
                        startProtocol(context, myIntent);
                        //Starts service if not already running
                    }
                }
            } else {
                Toast.makeText(context, "Key Invalidated", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (messageBody.equals(keyWord)) {
                //Checks for keyword
                if (!ConstantClass.isServiceRunning) {
                    startProtocol(context, myIntent);
                    //Starts service if not already running
                }
            }

        }
    }

    public ArrayList<NumberDataHolder> getArrayList(String key, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<NumberDataHolder>>() {
            }.getType();
            return gson.fromJson(json, type);
        } else {
            return null;
        }
    }


}

