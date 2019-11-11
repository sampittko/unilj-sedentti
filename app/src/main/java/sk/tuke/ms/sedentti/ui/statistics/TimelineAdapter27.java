package sk.tuke.ms.sedentti.ui.statistics;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.TimeHelper;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.day.Day;
import sk.tuke.ms.sedentti.model.day.DayModel;

public class TimelineAdapter27 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int DAY_VIEW = 0;
    private final int SESSION_VIEW = 1;
    private List<DayModel> dayModelsList = new ArrayList<>();
    private Context context;

    public TimelineAdapter27(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (dayModelsList.get(position) instanceof Day) {
            return DAY_VIEW;
        }
        return SESSION_VIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == DAY_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_statistics_day, parent, false);
            return new DayHolder(itemView);
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_statistics_session, parent, false);
        return new SessionHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.i("bind", "now binding " + position);

        if (dayModelsList.get(position) instanceof Day) {
//            doing day item
            DayHolder dayHolder = (DayHolder) holder;
            Day day = (Day) dayModelsList.get(position);

            dayHolder.date.setText(TimeHelper.formatDate(day.getDate()));
            dayHolder.completed.setText(String.valueOf(day.getNumberOfSessions()));

            dayHolder.activeTime.setText(TimeHelper.formatTimeString(day.getActiveTime()));
            dayHolder.sedentaryTime.setText(TimeHelper.formatTimeString(day.getSedentaryTime()));

        } else if (dayModelsList.get(position) instanceof Session) {
//            doing session item

            SessionHolder sessionHolder = (SessionHolder) holder;
            Session session = (Session) dayModelsList.get(position);

            String sessionName;
            if (session.isSedentary()) {
                sessionName = context.getResources().getString(R.string.home_timeline_name_sedentary);
                sessionHolder.dot.setBackground(context.getDrawable(R.drawable.shape_timeline_circle_sedentarry));
            } else {
                sessionName = context.getResources().getString(R.string.home_timeline_name_activity);
                sessionHolder.dot.setBackground(context.getDrawable(R.drawable.shape_timeline_circle_active));
            }
            sessionHolder.activityName.setText(sessionName);

//            handles the activityTime and adds date if needed
            String sessionTime;
            sessionTime = TimeHelper.formatDateTime(session.getStartTimestamp());
            sessionHolder.activityTime.setText(sessionTime);

//            handles duration
            if (session.getDuration() > 0L) {
                String sessionDuration;
                sessionDuration = TimeHelper.formatDuration(session.getDuration());
                sessionHolder.activityDuration.setText(sessionDuration);
            } else {
                sessionHolder.activityDuration.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.dayModelsList.size();
    }

    public void setDayModelsList(List<DayModel> dayModelsList) {
        this.dayModelsList = dayModelsList;
        notifyDataSetChanged();
    }

    class SessionHolder extends RecyclerView.ViewHolder {
        private TextView dot;
        private TextView activityName;
        private TextView activityTime;
        private TextView activityDuration;

        public SessionHolder(@NonNull View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.tw_f_home_timeline_dot);
            activityName = itemView.findViewById(R.id.tw_f_home_timeline_session_activity_name);
            activityTime = itemView.findViewById(R.id.tw_f_home_timeline_session_activity_time);
            activityDuration = itemView.findViewById(R.id.tw_f_home_timeline_session_activity_duration);

        }
    }

    class DayHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private TextView completed;
        private TextView success;
        private TextView activeTime;
        private TextView sedentaryTime;

        public DayHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tw_f_home_timeline_day_date);
            completed = itemView.findViewById(R.id.tw_f_home_timeline_day_value_completed);
            success = itemView.findViewById(R.id.tw_f_home_timeline_day_value_success);
            activeTime = itemView.findViewById(R.id.tw_f_home_timeline_day_value_active);
            sedentaryTime = itemView.findViewById(R.id.tw_f_home_timeline_day_value_sedentary);
        }
    }
}
