package com.rosinante24.edittextlimitconfig;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text_fetch)
    TextView textFetch;
    @BindView(R.id.edit_text_cek_length)
    EditText editTextCekLength;
    @BindView(R.id.button_fetch)
    Button buttonFetch;
    @BindView(R.id.layout_app)
    LinearLayout layoutApp;

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//      nyambungin aplikasi ke firebase remote config
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//      setting sharedpreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//      lempar data default sesuai tipe data masing2 menggunakan HashMap
        HashMap<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("edit_text_length", 10L);
        defaultConfig.put("default_text_view", "Hi! I'm default text here");
        defaultConfig.put("text_caps_setting", false);
        defaultConfig.put("bg_color", getResources().getColor(R.color.colorPrimary));
//      membuat pengaturan pada Firebase Remote Config
        FirebaseRemoteConfigSettings remoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
//      mengatur batas karakter yg bisa diinput di edit text
        editTextCekLength.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(sharedPreferences.getInt("edit_text_length", 10))
        });
//      mengatur pengaturan default dari aplikasi, sebelum diubah dengan remote config
        firebaseRemoteConfig.setDefaults(defaultConfig);
        firebaseRemoteConfig.setConfigSettings(remoteConfigSettings);
//      memanggil method yang berisi pengaturan lanjutan dari remote config
        getAllFetch();
    }

    @OnClick(R.id.button_fetch)
    public void onClick() {
        getAllFetch();
    }

    //          method getAllFetch(), yang berisi pengaturan lanjutan dari remote config
    private void getAllFetch() {
//      mengatur durasi dari pengaturan remote config
        long expirationcache = 3600;
//      memberikan keadaan, jika remote config dalam mode developer
        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
//      mengatur durasi menjadi nol
            expirationcache = 0;
        }
//      mengambil data perubahan dari remote config dengan method fetch(), yg diisi dengan durasi pengaturan
        firebaseRemoteConfig.fetch(expirationcache)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
//      memberi kondisi, jika pengambilan data berhasil
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Fetch success", Toast.LENGTH_SHORT).show();
//      mengaktifkan data yang diambil dari remote config
                            firebaseRemoteConfig.activateFetched();
//      memanggil method yang berisi pengaturan untuk menampilkan data
                            displayFetch();
//      memberi kondisi, jika data gagal diambil
                        } else {
                            Toast.makeText(MainActivity.this, "Fetch Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    // method displayFetch() yang berisi pengaturan untuk menamilkan data yang diambil dari remote config
    private void displayFetch() {
//      menangkap data dari remote config, menggunakan method get(), yang disesuaikan dengan tipe data yang diambil,
//      dan diisi dengan parameter yang ada di remote config
        String text_fetch = firebaseRemoteConfig.getString("default_text_view");
        boolean text_caps = firebaseRemoteConfig.getBoolean("text_caps_setting");
        Long edit_text_length = firebaseRemoteConfig.getLong("edit_text_length");
        int bg_color = Color.parseColor(firebaseRemoteConfig.getString("bg_color"));
//      memberikan kondisi jika yang ditangkap adalah data boolean yang mengatur kapital, dari text
        if (text_caps) {
            textFetch.setAllCaps(true);
        } else {
            textFetch.setAllCaps(false);
        }
//      menaruh data yang sudah diambil dari remote config kepada edit text, sebagai pengaturan panjang karakter
        editTextCekLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(edit_text_length.intValue())});
//      menaruh data yang sudah diambil dari remote config ke text view
        textFetch.setText(text_fetch);
//      menaruh data yang sudah diambil dari remote config ke layout yang akan dijadikan pengaturan background layout
        layoutApp.setBackgroundColor(bg_color);
//      menaruh data yang sudah diambil dari remote config ke edit text yang akan dijadikan hint
        editTextCekLength.setHint("You can only fill this field with " + edit_text_length + " characters ");
    }

    //      memanggil method yang berisi penarikan data dari remote config saat aplikasi dijalankan
    @Override
    protected void onStart() {
        super.onStart();
        getAllFetch();
    }

    //      memanggil method yang berisi penarikan data dari remote config saat aplikasi kembali dijalankan
    @Override
    protected void onResume() {
        super.onResume();
        getAllFetch();
    }
}
