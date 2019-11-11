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

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.SessionHolder> {

    private List<Session> sessions = new ArrayList<>();
    private Context context;

    public TimelineAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public SessionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_statistics_session, parent, false);
        return new SessionHolder(itemView);
    }

    public void onBindViewHolder(@NonNull SessionHolder holder, int position) {
        Log.i("bind", "now binding " + position);
        Session session = sessions.get(position);

        String sessionName;
        if (session.isSedentary()) {
            sessionName = context.getResources().getString(R.string.home_timeline_name_sedentary);
            holder.dot.setBackground(context.getDrawable(R.drawable.shape_timeline_circle_sedentarry));
        } else {
            sessionName = context.getResources().getString(R.string.home_timeline_name_activity);
            holder.dot.setBackground(context.getDrawable(R.drawable.shape_timeline_circle_active));
        }
        holder.activityName.setText(sessionName);

//            handles the activityTime and adds date if needed
        String sessionTime;
        sessionTime = TimeHelper.formatDateTime(session.getStartTimestamp());
        holder.activityTime.setText(sessionTime);

//            handles duration
        if (session.getDuration() > 0L) {
            String sessionDuration;
            sessionDuration = TimeHelper.formatDuration(session.getDuration());
            holder.activityDuration.setText(sessionDuration);
        } else {
            holder.activityDuration.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
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
}
