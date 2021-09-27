package com.ssr_projects.findmyphone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import static android.content.Context.AUDIO_SERVICE;
import static com.ssr_projects.findmyphone.ConstantClass.RESULT_AUDIO_CODE;

public class ConfigSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SwitchPreferenceCompat audioTogglePreference = getPreferenceManager().findPreference("audio_enable_disable");
        final SwitchPreferenceCompat torchTogglePreference = getPreferenceManager().findPreference("torch");
        final PreferenceCategory audioPreference = getPreferenceManager().findPreference("audio_profile");
        final SeekBarPreference seekBarPreference = getPreferenceManager().findPreference("volume");
        final Preference ringtonePreference = getPreferenceManager().findPreference("ringtone");
        final EditTextPreference expiringKeywordPreference = getPreferenceManager().findPreference("keyword");

        expiringKeywordPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull final EditText editText) {
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if(charSequence.length() == 0){
                            Toast.makeText(getActivity(), "Key cannot be empty", Toast.LENGTH_SHORT).show();
                            expiringKeywordPreference.setText("ENABLE");
                        }
                        sharedPreferences.edit().putBoolean("is_valid", true).apply();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }
        });


        ringtonePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("audio/*");
                startActivityForResult(intent, ConstantClass.RESULT_AUDIO_CODE);
                return true;
            }
        });

        seekBarPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return true;
            }
        });

        AudioManager audioManager = (AudioManager) getActivity().getSystemService(AUDIO_SERVICE);

        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        seekBarPreference.setMax(max);
        seekBarPreference.setValue(sharedPreferences.getInt("volume", max));

        audioTogglePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean turned = (Boolean) newValue;
                if(!turned && torchTogglePreference.isChecked()){
                    torchTogglePreference.setEnabled(false);
                }
                else{
                    torchTogglePreference.setEnabled(true);
                }

                return true;
            }
        });

        torchTogglePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean turned = (Boolean) newValue;
                if(!turned && audioTogglePreference.isChecked()){
                    audioPreference.setDependency(null);
                    audioTogglePreference.setEnabled(false);
                }
                else{
                    audioPreference.setDependency("audio_enable_disable");
                    audioTogglePreference.setEnabled(true);
                }

                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_AUDIO_CODE) {
            try {
                Uri musicUrl = data.getData();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                Log.d(getClass().getName(), "onActivityResult: " + musicUrl);
                sharedPreferences.edit().putString("audio_url", getRealPathFromURI(musicUrl, getActivity())).apply();
            }
            catch (Exception e){
                Log.e(getClass().getName(), "onActivityResult: " + e);
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri, Context context) {
        String path = null;
        String[] proj = { MediaStore.MediaColumns.DATA };
        try {
            Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                path = cursor.getString(column_index);
            }
            cursor.close();
        }
        catch (SecurityException e){
            Log.e(getClass().getName(), "getRealPathFromURI: ", e );
        }
        return path;
    }


}