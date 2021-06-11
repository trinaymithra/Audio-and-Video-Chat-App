package com.example.zoom.chat.imagepick;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.zoom.chat.imagepick.fragment.ImagePickHelperFragment;
import com.example.zoom.chat.imagepick.fragment.ImageSourcePickDialogFragment;

public class ImagePickHelper {

    public void pickAnImage(FragmentActivity activity, int requestCode) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(activity, requestCode);
        showImageSourcePickerDialog(activity.getSupportFragmentManager(), imagePickHelperFragment);
    }

    private void showImageSourcePickerDialog(FragmentManager fm, ImagePickHelperFragment fragment) {
        ImageSourcePickDialogFragment.show(fm,
                new ImageSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(fragment));
    }
}