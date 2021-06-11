package com.example.zoom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewVersion;
    private RelativeLayout rlTellOthers, rlRate, rlPrivacyPolicy;
    private ImageView imageViewBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initViews();
        clickListeners();
    }

    private void clickListeners() {
        rlTellOthers.setOnClickListener(this);
        rlRate.setOnClickListener(this);
        rlPrivacyPolicy.setOnClickListener(this);
        imageViewBack.setOnClickListener(this);
    }

    private void initViews() {
        textViewVersion = findViewById(R.id.textViewVersion);
        rlTellOthers = findViewById(R.id.rlTellOthers);
        rlRate = findViewById(R.id.rlRate);
        rlPrivacyPolicy = findViewById(R.id.rlPrivacyPolicy);
        imageViewBack = findViewById(R.id.imageViewBack);

        textViewVersion.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.rlTellOthers :
                shareApp();
                break;
            case R.id.rlRate :
                rateAppOnStore();
                break;
            case R.id.rlPrivacyPolicy :
                openBrowser();
                break;
            case R.id.imageViewBack :
                finish();
                break;
        }

    }

    private void openBrowser() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        startActivity(intent);
    }

    private void rateAppOnStore() {
        Uri uri = Uri.parse("market://details?id=us.zoom.videomeetings");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        startActivity(intent);
    }

    private void shareApp() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out my app at https://play.google.com/store/apps/details?id=us.zoom.videomeetings&hl=en_IN&gl=US");
        intent.setType("text/plain");
        startActivity(intent);
    }
}