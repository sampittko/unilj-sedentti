package sk.tuke.ms.sedentti.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import de.hdodenhof.circleimageview.CircleImageView;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.activity.MotivateMeActivity;
import sk.tuke.ms.sedentti.activity.SettingsActivity;
import sk.tuke.ms.sedentti.model.Profile;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this.profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        this.profileViewModel.getProfile().observe(this, profile -> updateProfile(root, profile));
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        View statusLayout = getActivity().findViewById(R.id.temp_strikes);
//        statusLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getContext(), StatusActivity.class));
//            }
//        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_motivate_me:
                startActivity(new Intent(getContext(), MotivateMeActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
        }
        return false;
    }

    private void updateProfile(View root, Profile profile) {
        TextView username = root.findViewById(R.id.tw_f_profile_profile_name);
        username.setText(profile.getName());

        CircleImageView profilePhoto = root.findViewById(R.id.iw_f_profile_profile_image);
        String imageUrl = profile.getPhotoUrl();
        if (imageUrl != null && imageUrl.length() > 0) {
            Glide.with(this).load(imageUrl).into(profilePhoto);
            profilePhoto.setBorderWidth(0);
        }
    }
}
