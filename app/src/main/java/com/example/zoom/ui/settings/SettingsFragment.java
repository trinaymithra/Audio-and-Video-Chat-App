package com.example.zoom.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.zoom.AboutActivity;
import com.example.zoom.R;
import com.example.zoom.call.services.LoginService;
import com.example.zoom.call.utils.Constants;
import com.example.zoom.call.utils.SharedPrefsHelper;
import com.example.zoom.call.utils.UsersUtils;
import com.example.zoom.welcome.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.quickblox.messages.services.SubscribeService;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private TextView textViewName, textViewEmail;
    private RelativeLayout rlContacts, rlMeetings, rlChat, rlAbout, rlLogOut;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_settings, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViews();
        clickListener();
    }

    private void clickListener() {
        rlContacts.setOnClickListener(this);
        rlMeetings.setOnClickListener(this);
        rlChat.setOnClickListener(this);
        rlAbout.setOnClickListener(this);
        rlLogOut.setOnClickListener(this);
    }

    private void initViews() {
        TextView textViewTitle = getActivity().findViewById(R.id.tvTitle);
        textViewTitle.setText(getActivity().getResources().getString(R.string.title_settings));

        textViewName = root.findViewById(R.id.textViewName);
        textViewEmail = root.findViewById(R.id.textViewEmail);
        rlContacts = root.findViewById(R.id.rlContacts);
        rlMeetings = root.findViewById(R.id.rlMeetings);
        rlChat = root.findViewById(R.id.rlChat);
        rlAbout = root.findViewById(R.id.rlAbout);
        rlLogOut = root.findViewById(R.id.rlLogOut);

        textViewName.setText(SharedPrefsHelper.getInstance().getQbUser().getFullName());
        textViewEmail.setText(SharedPrefsHelper.getInstance().get(Constants.USER_EMAIL));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlAbout :
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            case R.id.rlContacts :
                Navigation.findNavController(v).navigate(R.id.navigation_contacts);

                break;
            case R.id.rlChat :
                Navigation.findNavController(v).navigate(R.id.navigation_meetchat);
                break;
            case R.id.rlMeetings :
                Navigation.findNavController(v).navigate(R.id.navigation_meetings);
                break;
            case R.id.rlLogOut :
                logout();
                break;
        }

    }

    private void logout() {
        SubscribeService.unSubscribeFromPushes(getActivity());
        LoginService.logout(getActivity());
        UsersUtils.removeUserData(getActivity());
        SharedPrefsHelper.getInstance().clearAllData();
        FirebaseAuth.getInstance().signOut();
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(getActivity(), WelcomeActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}