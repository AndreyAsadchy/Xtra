package com.exact.twitch.ui.clips;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.cardview.widget.CardView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.exact.twitch.R;
import com.exact.twitch.model.clip.Clip;
import com.exact.twitch.ui.adapter.BindableRecyclerViewHolder;
import com.exact.twitch.ui.adapter.PagedListRecyclerViewAdapter;
import com.exact.twitch.util.TwitchApiHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ClipsRecyclerViewAdapter extends PagedListRecyclerViewAdapter<Clip, ClipsRecyclerViewAdapter.ViewHolder, BaseClipsFragment.OnClipSelectedListener> {

    private static final String KEY_VIEWS = "views";
    private static final String KEY_TITLE = "title";

    private static final DiffUtil.ItemCallback<Clip> DIFF_CALLBACK = new DiffUtil.ItemCallback<Clip>() {

        @Override
        public boolean areItemsTheSame(Clip oldItem, Clip newItem) {
            return oldItem.getSlug().equals(newItem.getSlug());
        }

        @Override
        public boolean areContentsTheSame(Clip oldItem, Clip newItem) {
            return oldItem.getViews().equals(newItem.getViews()) &&
                    oldItem.getTitle().equals(newItem.getTitle());
        }

        @Override
        public Object getChangePayload(Clip oldItem, Clip newItem) {
            Bundle diffBundle = new Bundle();
            if (!oldItem.getViews().equals(newItem.getViews())) {
                diffBundle.putInt(KEY_VIEWS, newItem.getViews());
            }
            if (!oldItem.getTitle().equals(newItem.getTitle())) {
                diffBundle.putString(KEY_TITLE, newItem.getTitle());
            }
            return diffBundle;
        }
    };

    public ClipsRecyclerViewAdapter(BaseClipsFragment.OnClipSelectedListener listener, Runnable retryCallback) {
        super(DIFF_CALLBACK, R.layout.fragment_clips_list_item, ViewHolder.class, listener, retryCallback);
    }

    @Override
    protected void onItemUpdate(ViewHolder holder, int position, @NonNull List<Object> payloads) {
        holder.update((Bundle) payloads.get(0));
    }

    class ViewHolder extends BindableRecyclerViewHolder<Clip> {

        private CardView cardView;
        private ImageView thumbnail;
        private ImageView streamerAvatar;
        private TextView streamerName;
        private TextView views;
        private TextView title;
        private TextView gameName;
        private TextView date;
        private TextView length;

        private Context context;

        ViewHolder(View view) {
            super(view);
            context = view.getContext();
            cardView = view.findViewById(R.id.fragment_clips_card_view);
            thumbnail = view.findViewById(R.id.fragment_clips_iv_thumbnail);
            streamerAvatar = view.findViewById(R.id.fragment_clips_iv_user_avatar);
            streamerName = view.findViewById(R.id.fragment_clips_tv_username);
            views = view.findViewById(R.id.fragment_clips_tv_views);
            title = view.findViewById(R.id.fragment_clips_tv_title);
            gameName = view.findViewById(R.id.fragment_clips_tv_game);
            date = view.findViewById(R.id.fragment_clips_tv_date);
            length = view.findViewById(R.id.fragment_clips_tv_length);
        }

        @Override
        protected void bindTo(Clip item) {
            cardView.setOnClickListener(v -> itemClickListener.startClip(item));
            Picasso.get().load(item.getThumbnails().getMedium()).fit().into(thumbnail);
            Picasso.get().load(item.getBroadcaster().getLogo()).into(streamerAvatar);
            streamerName.setText(item.getBroadcaster().getName());
            int viewersCount = item.getViews(); //TODO refactor in other adapters too
            int viewersRes;
            if (viewersCount != 1) {
                viewersRes = R.string.viewers;
            } else {
                viewersRes = R.string.viewer;
            }
            views.setText(context.getString(viewersRes, viewersCount));
            title.setText(item.getTitle());
            gameName.setText(item.getGame());
            date.setText(TwitchApiHelper.INSTANCE.parseIso8601Date(context, item.getCreatedAt()));
            length.setText(DateUtils.formatElapsedTime(item.getDuration().longValue()));
        }

        void update(Bundle bundle) {
            for (String key : bundle.keySet()) {
                switch (key) {
                    case KEY_VIEWS:
                        views.setText(String.valueOf(bundle.getInt(key)));
                        break;
                    case KEY_TITLE:
                        title.setText(bundle.getString(key));
                        break;
                }
            }
        }
    }
}