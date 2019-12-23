package sk.tuke.ms.sedentti.ui.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import de.hdodenhof.circleimageview.CircleImageView;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.dialog.StopSensingDialog;
import sk.tuke.ms.sedentti.helper.TimeHelper;
import sk.tuke.ms.sedentti.helper.shared_preferences.ActivityRecognitionSPHelper;
import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.recognition.activity.ActivityRecognitionService;

public class HomeFragment extends Fragment implements StopSensingDialog.StopSensingDialogListener {

    private static final String STOP_SENSING_DIALOG = "StopSensigDialog";
    private final String TAG = this.getClass().getSimpleName();

    private HomeViewModel homeViewModel;
    private final int TIMELINE_ITEM_HEIGHT = 60;
    private LinearLayout timelineLayout;
    private ActivityRecognitionSPHelper activityRecognitionSettings;
    private AppSPHelper appSettings;
    private int state;
    private boolean startUp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        this.activityRecognitionSettings = new ActivityRecognitionSPHelper(getContext());
        this.state = this.activityRecognitionSettings.getActivityRecognitionState();

        this.appSettings = new AppSPHelper(getContext());
        this.startUp = true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_activity_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                this.homeViewModel.updateModel();
                return true;
            case R.id.action_settings:
                return false;
        }
        return false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel.getHomeTimelineSessions().observe(this, sessions -> makeTimeline(sessions));

        TextView sensingOffCompletedSessionText = root.findViewById(R.id.tw_f_home_sensing_off_completed_sessions);
        homeViewModel.getFinishedCount().observe(this, count -> {
            this.startUp = count <= 0;

            StringBuilder sb = new StringBuilder();
            sb.append("You've completed ");
            sb.append(count);
            if (count == 1) {
                sb.append(" session");
            } else {
                sb.append(" sessions");
            }
            sb.append(" so far");

            sensingOffCompletedSessionText.setText(sb.toString());
        });

        TextView streakValue = root.findViewById(R.id.tw_f_home_value_streaks);
        homeViewModel.getStreak().observe(this, value -> {
            if (value != null) {
                streakValue.setText(value.toString());
            }
        });

        TextView successValue = root.findViewById(R.id.tw_f_home_value_success);
        homeViewModel.getSuccess().observe(this, value -> {
            if (value != null) {
                successValue.setText(value.toString() + " %");
            }
        });

        makeGraphs(root);

        setOnClickOnViews(root);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateSessionGraphStateUI(this.state);
        updateSensingStateUI(this.state);
        updateProfile(homeViewModel.getActiveProfile());
    }

    private void updateProfile(Profile activeProfile) {
        TextView username = getActivity().findViewById(R.id.tw_f_home_text_hello);
        StringBuilder sb = new StringBuilder();
        sb.append("Hello ");
        sb.append(activeProfile.getName().split(" ")[0]);
        username.setText(sb.toString());

        CircleImageView profilePhoto = getActivity().findViewById(R.id.iw_f_home_profile_image);
        String imageUrl = activeProfile.getPhotoUrl();
        if (imageUrl != null && imageUrl.length() > 0) {
            Glide.with(this).load(imageUrl).into(profilePhoto);
            profilePhoto.setBorderWidth(0);
        }
    }

    private void makeGraphs(View root) {
        DecoView activeSessionGraph = root.findViewById(R.id.graph_f_home_session);
        DecoView activeTime = root.findViewById(R.id.graph_f_home_active);
        DecoView sedentaryTime = root.findViewById(R.id.graph_f_home_sedentary);
        DecoView vehicleTime = root.findViewById(R.id.graph_f_home_vehicle);

        activeSessionGraph.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 100)
                .setInitialVisibility(true)
                .setLineWidth(40f)
                .build());

        activeTime.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 100)
                .setInitialVisibility(true)
                .setLineWidth(20f)
                .build());

        sedentaryTime.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 100)
                .setInitialVisibility(true)
                .setLineWidth(20f)
                .build());

        vehicleTime.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 100)
                .setInitialVisibility(true)
                .setLineWidth(20f)
                .build());

        SeriesItem sessionItem = new SeriesItem.Builder(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null))
                .setRange(0, 100, 0)
                .setLineWidth(40f)
                .build();

        SeriesItem activeItem = new SeriesItem.Builder(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null))
                .setRange(0, 100, 0)
                .setLineWidth(20f)
                .build();

        SeriesItem sedentaryItem = new SeriesItem.Builder(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setRange(0, 100, 0)
                .setLineWidth(20f)
                .build();

        SeriesItem vehicleItem = new SeriesItem.Builder(ResourcesCompat.getColor(getResources(), R.color.colorAccentOther, null))
                .setRange(0, 100, 0)
                .setLineWidth(20f)
                .build();

        int activeSessionIndex = activeSessionGraph.addSeries(sessionItem);
        int activeTimeIndex = activeTime.addSeries(activeItem);
        int sedentaryTimeIndex = sedentaryTime.addSeries(sedentaryItem);
        int vehicleTimeIndex = vehicleTime.addSeries(vehicleItem);

        TextView graphTimeValue = root.findViewById(R.id.tw_f_home_graph_time);
        TextView sessionActivity = root.findViewById(R.id.tw_f_home_graph_session_activity);
        homeViewModel.getPendingSession().observe(this, session -> {
            if (session != null) {
                graphTimeValue.setText(TimeHelper.formatTimeWithSeconds(session.getDuration()));
                // TODO: 11/11/19 set session limit
                // long limit = new SharedPreferencesHelper(getContext()).getSedentaryLimit() * 1000L;


                int normalizedValue;
                if (session.isSedentary()) {
                    normalizedValue = getNormalizedValue(session.getDuration(), appSettings.getSedentaryLimit());
                    sessionActivity.setText("Sedentary");
                    sessionItem.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                } else {
                    normalizedValue = getNormalizedValue(session.getDuration(), appSettings.getActiveLimit());
                    sessionActivity.setText("Active");
                    sessionItem.setColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                }
                activeSessionGraph.addEvent(new DecoEvent.Builder(normalizedValue).setIndex(activeSessionIndex).setDelay(4000).build());
            } else {
                updateSessionGraphStateUI(this.state);
            }
        });

        TextView sedentaryTimeValue = root.findViewById(R.id.tw_f_home_value_sedentary);
        homeViewModel.getDailySedentaryDuration().observe(this, value -> {
            sedentaryTimeValue.setText(TimeHelper.formatTimeString(value));
            // TODO: 11/10/19 set sedentary time goal
            int normalizedValue = getNormalizedValue(value, 8L * 3600L * 1000L);
            sedentaryTime.addEvent(new DecoEvent.Builder(normalizedValue).setIndex(activeTimeIndex).setDelay(4000).build());
        });

        TextView activeTimeValue = root.findViewById(R.id.tw_f_home_value_active);
        homeViewModel.getDailyActiveDuration().observe(this, value -> {
            activeTimeValue.setText(TimeHelper.formatTimeString(value));
            // TODO: 11/10/19 set activity time goal
            int normalizedValue = getNormalizedValue(value, 8 * 3600 * 1000);
            activeTime.addEvent(new DecoEvent.Builder(normalizedValue).setIndex(sedentaryTimeIndex).setDelay(4000).build());
        });

        TextView vehicleTimeValue = root.findViewById(R.id.tw_f_home_value_vehicle);
        homeViewModel.getDailyVehicleDuration().observe(this, value -> {
            vehicleTimeValue.setText(TimeHelper.formatTimeString(value));
            // TODO: 11/10/19 set vehicle time goal
            int normalizedValue = getNormalizedValue(value, 8 * 3600 * 1000);
            vehicleTime.addEvent(new DecoEvent.Builder(normalizedValue).setIndex(vehicleTimeIndex).setDelay(4000).build());
        });
    }

    private int getNormalizedValue(Long milliseconds, long milliLimit) {
        int result = (int) (((double) milliseconds / milliLimit) * 100L);

        if (result > 100) {
            return 100;
        }
        return result;
    }


    private void updateSessionGraphStateUI(int state) {
        if (getActivity() != null) {
            // first time start-up text
            TextView firstTimeText = getActivity().findViewById(R.id.tw_f_home_first_time_note);

            // layout when sensing turned-off
            LinearLayout nonActiveTextLayout = getActivity().findViewById(R.id.f_home_layout_sensing_off);

            // graph when sensing active
            LinearLayout sessionTextLayout = getActivity().findViewById(R.id.f_home_layout_session_text);
            DecoView sessionGraph = getActivity().findViewById(R.id.graph_f_home_session);

            if (state == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED || state == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_UNKNOWN) {
                if (startUp) {
                    // first time text visible and nothing else
                    firstTimeText.setVisibility(View.VISIBLE);

                    nonActiveTextLayout.setVisibility(View.INVISIBLE);
                    sessionTextLayout.setVisibility(View.INVISIBLE);
                    sessionGraph.setVisibility(View.INVISIBLE);
                } else {
                    // non active, sensing turned off
                    nonActiveTextLayout.setVisibility(View.VISIBLE);

                    firstTimeText.setVisibility(View.INVISIBLE);
                    sessionTextLayout.setVisibility(View.INVISIBLE);
                    sessionGraph.setVisibility(View.INVISIBLE);
                }
            } else if (state == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING) {
                // sensing active, turned on
                sessionGraph.setVisibility(View.VISIBLE);
                sessionTextLayout.setVisibility(View.VISIBLE);

                nonActiveTextLayout.setVisibility(View.INVISIBLE);
                firstTimeText.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void updateSensingStateUI(int state) {
        Button button = getActivity().findViewById(R.id.btn_home_button_sensing);
        TextView settingsIcon = getActivity().findViewById(R.id.f_home_sensing_settings);
        TextView sensingStatus = getActivity().findViewById(R.id.tw_home_text_sensing_state);
        if (state == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED) {
            button.getBackground().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
            settingsIcon.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.colorAccent)));

            sensingStatus.setText("Tracking is turned off");
            button.setText("Start");
        } else if (state == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING) {
            button.getBackground().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            settingsIcon.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.colorPrimary)));

            sensingStatus.setText("Tracking is active");
            button.setText("Stop");
        }
    }

    private void toggleButton() {
        if (this.state == PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED) {
//            turn it on
            this.state = PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_RUNNING;
            startForegroundService(PredefinedValues.COMMAND_START);
            updateSensingStateUI(this.state);
            updateSessionGraphStateUI(this.state);
        } else {
//            turn it off
            StopSensingDialog dialog = new StopSensingDialog();
            dialog.setCallback(this);
            dialog.show(getFragmentManager(), STOP_SENSING_DIALOG);
        }
    }

    ;

    private void startForegroundService(String command) {
        Intent intent = new Intent(getActivity().getApplicationContext(), ActivityRecognitionService.class);
        intent.setAction(command);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
        } else {
            getActivity().startService(intent);
        }

        Crashlytics.log(Log.DEBUG, TAG, "Activity recognition foreground service started");
    }


    private void setOnClickOnViews(View root) {
        this.timelineLayout = root.findViewById(R.id.f_home_layout_timeline);
        this.timelineLayout.setOnClickListener(v -> {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
            View view = bottomNavigationView.findViewById(R.id.navigation_statistics);
            view.performClick();
        });

        root.findViewById(R.id.iw_f_home_profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
                View view = bottomNavigationView.findViewById(R.id.navigation_profile);
                view.performClick();
            }
        });

        root.findViewById(R.id.btn_home_button_sensing).setOnClickListener((View view) -> toggleButton());
    }

    private void makeTimeline(ArrayList<Session> sessions) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        this.timelineLayout.removeAllViews();

        // handles activity naming
        for (Session session : sessions) {
            View view = inflater.inflate(R.layout.item_timeline_home, this.timelineLayout, false);
            TextView dot = view.findViewById(R.id.tw_f_home_timeline_dot);
            String sessionName;

            if (session.isSedentary()) {
                sessionName = getResources().getString(R.string.home_timeline_name_sedentary);
                dot.setBackground(getActivity().getDrawable(R.drawable.shape_timeline_circle_sedentary));
            } else if (session.isInVehicle()) {
                sessionName = getResources().getString(R.string.home_timeline_name_invehicle);
                dot.setBackground(getActivity().getDrawable(R.drawable.shape_timeline_circle_invehicle));
            } else {
                sessionName = getResources().getString(R.string.home_timeline_name_active);
                dot.setBackground(getActivity().getDrawable(R.drawable.shape_timeline_circle_active));
            }
            TextView activityName = view.findViewById(R.id.tw_f_home_timeline_session_activity_name);
            activityName.setText(sessionName);

//            handles the time and adds date if needed
            String sessionTime;
            Date sessionDate = new Date(session.getStartTimestamp());
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(sessionDate);
            int sessionDayNumber = currentCalendar.get(Calendar.DAY_OF_MONTH);
            currentCalendar.setTime(new Date());
            int currentDayNumber = currentCalendar.get(Calendar.DAY_OF_MONTH);

            if (currentDayNumber != sessionDayNumber) {
                sessionTime = TimeHelper.formatDateTime(sessionDate.getTime());
            } else {
                sessionTime = TimeHelper.formatTime(sessionDate.getTime());
            }

//            handles duration
            if (session.getDuration() > 0L) {
                sessionTime += " " + TimeHelper.formatDuration(session.getDuration());
            }

            TextView activityTime = view.findViewById(R.id.tw_f_home_timeline_session_start_time);
            activityTime.setText(sessionTime);

            this.timelineLayout.addView(view);
        }
        ViewGroup.LayoutParams layoutParams = this.timelineLayout.getLayoutParams();
        layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TIMELINE_ITEM_HEIGHT * sessions.size(), getResources().getDisplayMetrics());
        this.timelineLayout.setLayoutParams(layoutParams);
    }

    private void stopSensing() {
        this.state = PredefinedValues.ACTIVITY_RECOGNITION_SERVICE_STOPPED;
        startForegroundService(PredefinedValues.COMMAND_STOP);
        updateSensingStateUI(this.state);
        updateSessionGraphStateUI(this.state);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // TODO: 12/13/19 save pending activity to database
        stopSensing();
        this.homeViewModel.savePendingSession();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // TODO: 12/13/19 discard pending activity
        stopSensing();
        this.homeViewModel.discardPendingSession();
    }
}