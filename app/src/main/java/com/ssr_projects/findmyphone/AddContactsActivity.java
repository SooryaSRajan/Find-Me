package com.ssr_projects.findmyphone;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.loader.content.CursorLoader;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ssr_projects.findmyphone.Adapter.NumberDataHolder;
import com.ssr_projects.findmyphone.Adapter.RecyclerAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Predicate;

import static com.ssr_projects.findmyphone.ConstantClass.RESULT_CONTACT_CODE;

public class AddContactsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SharedPreferences sharedPreferences;
    FloatingActionButton floatingActionButton;

    final String PREF_STRING_ENABLED = "is_custom_emabled";
    public static final String PREF_ARRAY_LIST = "set_array_list";
    final String TAG = getClass().getName();
    RecyclerAdapter adapter;

    ArrayList<NumberDataHolder> arrayList;

    LinearLayout enabledTextView, emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_settings_fragment);


        recyclerView = findViewById(R.id.recycler_view);
        floatingActionButton = findViewById(R.id.add_contacts);

        emptyTextView = findViewById(R.id.empty_text);
        enabledTextView = findViewById(R.id.enabled_text);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if ((arrayList = getArrayList(PREF_ARRAY_LIST)) == null) {
            arrayList = new ArrayList<>();
        }

        adapter = new RecyclerAdapter(arrayList, sharedPreferences, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ItemDecoration());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isEnabled = sharedPreferences.getBoolean("custom_contact", false);
        recyclerView.setEnabled(isEnabled);
        floatingActionButton.setEnabled(isEnabled);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, RESULT_CONTACT_CODE);
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add Custom Contacts");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.switch_menu, menu);
        SwitchCompat switchView = (SwitchCompat) menu.findItem(R.id.switch_compact)
                .getActionView().findViewById(R.id.switch_view);

        Boolean isEnabled = sharedPreferences.getBoolean(PREF_STRING_ENABLED, false);

        switchView.setChecked(isEnabled);
        floatingActionButton.setEnabled(isEnabled);
        setRecycerVisibility(isEnabled);

        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPreferences.edit().putBoolean(PREF_STRING_ENABLED, b).apply();
                floatingActionButton.setEnabled(b);
                setRecycerVisibility(b);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_CONTACT_CODE && resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
            if (cursor.moveToFirst()) {

                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (hasPhone.equalsIgnoreCase("1")) {
                    Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null, null);
                    phones.moveToFirst();
                    Log.d(TAG, "onActivityResult: " + phones.getColumnCount());

                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex("data4"));
                    Log.d(TAG, "onActivityResult: Name: " + name + " Number: " + phoneNumber);

                    phones.close();

                    final NumberDataHolder numberDataHolder = new NumberDataHolder(name, phoneNumber);
                    boolean itemExists = arrayList.stream().anyMatch(new Predicate<NumberDataHolder>() {
                        @Override
                        public boolean test(NumberDataHolder c) {
                            return c.getUserNumber().equals(numberDataHolder.getUserNumber());
                        }
                    });

                    if(itemExists){
                        Log.d(TAG, "onActivityResult: Number already exists");
                        Toast.makeText(this, "Number Already Exists", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        arrayList.add(numberDataHolder);
                        adapter.notifyDataSetChanged();
                        setRecycerVisibility(true);

                        saveArrayList(arrayList, PREF_ARRAY_LIST);
                    }
                }

            }
            cursor.close();
        } else {
            Log.d(TAG, "onActivityResult: Failed, Code: " + resultCode + " Data: " + data);
        }
    }

    public void setRecycerVisibility(boolean isEnabled) {
        if (isEnabled) {
            if (arrayList.size() != 0) {
                recyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
                enabledTextView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                enabledTextView.setVisibility(View.GONE);
            }
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.GONE);
            enabledTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        outState.putParcelableArrayList("array_list", arrayList);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public void saveArrayList(ArrayList<NumberDataHolder> list, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    public ArrayList<NumberDataHolder> getArrayList(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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

