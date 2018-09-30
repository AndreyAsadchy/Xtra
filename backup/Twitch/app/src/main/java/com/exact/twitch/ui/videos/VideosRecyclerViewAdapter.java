package com.exact.twitch.ui.videos;

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
import com.exact.twitch.model.video.Video;
import com.exact.twitch.ui.adapter.BindableRecyclerViewHolder;
import com.exact.twitch.ui.adapter.PagedListRecyclerViewAdapter;
import com.exact.twitch.util.TwitchApiHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideosRecyclerViewAdapter extends PagedListRecyclerViewAdapter<Video, VideosRecyclerViewAdapter.ViewHolder, BaseVideosFragment.OnVideoSelectedListener> {

    private static final String KEY_VIEWERS = "views";
    private static final String KEY_TITLE = "title";
    private static final String KEY_PREVIEW = "preview";

    private static final DiffUtil.ItemCallback<Video> DIFF_CALLBACK = new DiffUtil.ItemCallback<Video>() {

        @Override
        public boolean areItemsTheSame(Video oldItem, Video newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(Video oldItem, Video newItem) {
            return oldItem.getViews().equals(newItem.getViews()) &&
                    oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getPreview().equals(newItem.getPreview());
        }

        @Override
        public Object getChangePayload(Video oldItem, Video newItem) {
            Bundle diffBundle = new Bundle();
            if (!oldItem.getViews().equals(newItem.getViews())) {
                diffBundle.putInt(KEY_VIEWERS, newItem.getViews());
            }
            if (!oldItem.getTitle().equals(newItem.getTitle())) {
                diffBundle.putString(KEY_TITLE, newItem.getTitle());
            }
            if (!oldItem.getPreview().equals(newItem.getPreview())) {
                diffBundle.putString(KEY_PREVIEW, newItem.getPreview());
            }
            return diffBundle;
        }
    };

    public VideosRecyclerViewAdapter(BaseVideosFragment.OnVideoSelectedListener listener, Runnable retryCallback) {
        super(DIFF_CALLBACK, R.layout.fragment_videos_list_item, ViewHolder.class, listener, retryCallback);
    }

    @Override
    protected void onItemUpdate(ViewHolder holder, int position, @NonNull List<Object> payloads) {
        holder.update((Bundle) payloads.get(0));
    }

    class ViewHolder extends BindableRecyclerViewHolder<Video> {

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
            cardView = view.findViewById(R.id.fragment_videos_card_view);
            thumbnail = view.findViewById(R.id.fragment_videos_iv_thumbnail);
            streamerAvatar = view.findViewById(R.id.fragment_videos_iv_user_avatar);
            streamerName = view.findViewById(R.id.fragment_videos_tv_username);
            views = view.findViewById(R.id.fragment_videos_tv_views);
            title = view.findViewById(R.id.fragment_videos_tv_title);
            gameName = view.findViewById(R.id.fragment_videos_tv_game);
            date = view.findViewById(R.id.fragment_videos_tv_date);
            length = view.findViewById(R.id.fragment_videos_tv_length);
        }

        @Override
        protected void bindTo(Video item) {
            cardView.setOnClickListener(v -> itemClickListener.startVideo(item));
            loadPreview(item.getPreview());
            Picasso.get().load(item.getChannel().getLogo()).into(streamerAvatar);
            streamerName.setText(item.getChannel().getName());
            int viewsCount = item.getViews();
            int viewsRes;
            if (viewsCount != 1) {
                viewsRes = R.string.views;
            } else {
                viewsRes = R.string.view;
            }
            views.setText(context.getString(viewsRes, viewsCount));
            title.setText(item.getTitle());
            gameName.setText(item.getGame());
            date.setText(TwitchApiHelper.INSTANCE.parseIso8601Date(context, item.getCreatedAt()));
            length.setText(DateUtils.formatElapsedTime(item.getLength()));
        }

        void update(Bundle bundle) {
            for (String key : bundle.keySet()) {
                switch (key) {
                    case KEY_VIEWERS:
                        views.setText(String.valueOf(bundle.getInt(key)));
                        break;
                    case KEY_TITLE:
                        title.setText(bundle.getString(key));
                        break;
                    case KEY_PREVIEW:
                        loadPreview(bundle.getString(KEY_PREVIEW));
                        break;
                }
            }
        }

        private void loadPreview(String preview) {
            Picasso.get().load(preview).fit().into(thumbnail); //TODO change size
        }
    }
}