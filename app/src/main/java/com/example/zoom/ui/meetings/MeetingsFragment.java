package com.example.zoom.ui.meetings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.zoom.R;
import com.example.zoom.call.utils.Constants;
import com.example.zoom.call.utils.SharedPrefsHelper;
import com.example.zoom.conference.ui.ConferenceCallActivity;
import com.quickblox.users.model.QBUser;

public class MeetingsFragment extends Fragment implements View.OnClickListener {

    private TextView tvTitle, textViewMeetingID, textViewStart, textViewSendInviation;
    private View root;
    private SharedPrefsHelper sharedPrefsHelper;
    private QBUser currentUser;
    private String name;
    private String id;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_meetings, container, false);


        return root;
    }

    private void initUI() {
        tvTitle = getActivity().findViewById(R.id.tvTitle);
        textViewMeetingID = getActivity().findViewById(R.id.textViewMeetingID);
        textViewStart = getActivity().findViewById(R.id.textViewStart);
        textViewSendInviation = getActivity().findViewById(R.id.textViewSendInvitation);

        tvTitle.setText(getResources().getString(R.string.title_meetings));
        textViewMeetingID.setText(currentUser.getId().toString());

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        currentUser =sharedPrefsHelper.getQbUser();

        initUI();
        clickListener();
    }

    private void clickListener() {
        textViewStart.setOnClickListener(this);
        textViewSendInviation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textViewStart:
                navigateToMeetings();
                break;
            case R.id.textViewSendInvitation:
                shareURL();
                break;
        }
    }

    private void shareURL() {
        name = currentUser.getFullName();
        id = currentUser.getId()+"";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Join Zoon Meeting");
        intent.putExtra(Intent.EXTRA_TEXT, name + " is inviting you to a scheduled Zoom Meeting. \nJoin Zoom Meeting http://zoom.in/share?id="+id);

        startActivity(Intent.createChooser(intent, "Share a link!"));
    }

    private void navigateToMeetings() {
        String meetingId= currentUser.getId().toString();
        Intent intent = new Intent(getActivity(), ConferenceCallActivity.class);
        intent.putExtra(Constants.ACTION_KEY_CHANNEL_NAME,meetingId);
        intent.putExtra(Constants.ACTION_KEY_USER_NAME,currentUser.getFullName());
        intent.putExtra(Constants.ACTION_KEY_ENCRYPTION_KEY,"");
        intent.putExtra(Constants.ACTION_KEY_ENCRYPTION_MODE,getString(R.string.encryption_mode_value));
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

}