package com.example.zoom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zoom.call.utils.Constants;
import com.example.zoom.conference.ui.ConferenceCallActivity;

public class JoinMeetingActivity extends BaseActivity implements View.OnClickListener {

    private EditText editTextID;
    private TextView textViewName;
    private AppCompatButton buttonJoinMeeting;
    private String name = "";
    private ImageView imageViewBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_meeting);

        initViews();
        clickListener();
        setData();
        textWatcher();
    }

    private void textWatcher() {
        editTextID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() > 0)
                    enableJoinButton();
                else
                    disableJoinButton();
            }
        });
    }

    private void setData() {
        if(sharedPrefsHelper.hasQbUser()){
            name = sharedPrefsHelper.getQbUser().getFullName();
        }
        else {
            name = getDeviceName();
        }
        textViewName.setText(name);
    }

    private void enableJoinButton() {
        buttonJoinMeeting.setEnabled(true);
        buttonJoinMeeting.setAlpha(1f);
    }

    private void disableJoinButton() {
        buttonJoinMeeting.setEnabled(false);
        buttonJoinMeeting.setAlpha(0.6f);
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void clickListener() {
        buttonJoinMeeting.setOnClickListener(this);
        imageViewBack.setOnClickListener(this);

    }

    private void initViews() {
        editTextID = findViewById(R.id.editTextID);
        textViewName = findViewById(R.id.textViewName);
        buttonJoinMeeting = findViewById(R.id.buttonJoinMeeting);
        imageViewBack = findViewById(R.id.imageViewBack);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonJoinMeeting :
                navigateToMeetings();
                break;
            case R.id.imageViewBack :
                finish();
                break;
        }
    }

    private void navigateToMeetings() {
        String meetingId= editTextID.getText().toString();
        Intent intent = new Intent(this, ConferenceCallActivity.class);
        intent.putExtra(Constants.ACTION_KEY_CHANNEL_NAME,meetingId);
        intent.putExtra(Constants.ACTION_KEY_USER_NAME,name);
        intent.putExtra(Constants.ACTION_KEY_ENCRYPTION_KEY,"");
        intent.putExtra(Constants.ACTION_KEY_ENCRYPTION_MODE,getString(R.string.encryption_mode_value));
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}