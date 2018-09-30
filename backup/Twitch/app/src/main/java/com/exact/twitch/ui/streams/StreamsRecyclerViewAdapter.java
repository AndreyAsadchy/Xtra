package com.exact.twitch.ui.streams;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.exact.twitch.R;
import com.exact.twitch.model.Preview;
import com.exact.twitch.model.channel.Channel;
import com.exact.twitch.model.stream.Stream;
import com.exact.twitch.ui.adapter.BindableRecyclerViewHolder;
import com.exact.twitch.ui.adapter.PagedListRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StreamsRecyclerViewAdapter extends PagedListRecyclerViewAdapter<Stream, StreamsRecyclerViewAdapter.ViewHolder, BaseStreamsFragment.OnStreamSelectedListener> {

    private static final String KEY_VIEWERS = "viewers";
    private static final String KEY_PREVIEW = "preview";
    private static final String KEY_GAME = "game";
    private static final String KEY_TITLE = "title";

    private static final DiffUtil.ItemCallback<Stream> DIFF_CALLBACK = new DiffUtil.ItemCallback<Stream>() {

        @Override
        public boolean areItemsTheSame(Stream oldItem, Stream newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(Stream oldItem, Stream newItem) {
            return oldItem.getViewers().equals(newItem.getViewers()) &&
                    oldItem.getPreview().equals(newItem.getPreview()) &&
                    oldItem.getGame().equals(newItem.getGame()) &&
                    oldItem.getChannel().getStatus().equals(newItem.getChannel().getStatus());
        }

        @Override
        public Object getChangePayload(Stream oldItem, Stream newItem) {
            Bundle diffBundle = new Bundle();
            if (!oldItem.getViewers().equals(newItem.getViewers())) {
                diffBundle.putInt(KEY_VIEWERS, newItem.getViewers());
            }
            if (!oldItem.getPreview().equals(newItem.getPreview())) {
                diffBundle.putParcelable(KEY_PREVIEW, newItem.getPreview());
            }
            if (!oldItem.getGame().equals(newItem.getGame())) {
                diffBundle.putString(KEY_GAME, newItem.getGame());
            }
            if (!oldItem.getChannel().getStatus().equals(newItem.getChannel().getStatus())) {
                diffBundle.putString(KEY_TITLE, newItem.getChannel().getStatus());
            }
            return diffBundle;
        }
    };

    public StreamsRecyclerViewAdapter(BaseStreamsFragment.OnStreamSelectedListener listener, Runnable retryCallback) {
        super(DIFF_CALLBACK, R.layout.fragment_streams_list_item, ViewHolder.class, listener, retryCallback);
    }

    @Override
    protected void onItemUpdate(ViewHolder holder, int position, @NonNull List<Object> payloads) {
        holder.update((Bundle) payloads.get(0));
    }

    class ViewHolder extends BindableRecyclerViewHolder<Stream> {

        private CardView cardView;
        private ImageView thumbnail;
        private ImageView type;
        private ImageView streamerAvatar;
        private TextView streamerName;
        private TextView viewers;
        private TextView title;
        private TextView gameName;

        private final Context context;

        public ViewHolder(View view) {
            super(view);
            context = view.getContext();
            cardView = view.findViewById(R.id.fragment_streams_card_view);
            thumbnail = view.findViewById(R.id.fragment_streams_iv_thumbnail);
            type = view.findViewById(R.id.fragment_streams_iv_stream_type);
            streamerAvatar = view.findViewById(R.id.fragment_streams_iv_user_avatar);
            streamerName = view.findViewById(R.id.fragment_streams_tv_username);
            viewers = view.findViewById(R.id.fragment_streams_tv_viewers);
            title = view.findViewById(R.id.fragment_streams_tv_title);
            gameName = view.findViewById(R.id.fragment_streams_tv_game);
        }

        @Override
        protected void bindTo(Stream item) {
            Channel channel = item.getChannel();
            Picasso.get().load(item.getPreview().getMedium()).fit().into(thumbnail); //TODO change width and height
            Picasso.get().load(channel.getLogo()).resize(75, 100).into(streamerAvatar);
            int typeDrawable;
            int typeColor;
            if (item.getPlaylist()) {
                typeDrawable = R.drawable.baseline_replay_black_24;
                typeColor = Color.GRAY;
            } else {
                typeDrawable = R.drawable.baseline_fiber_manual_record_black_24;
                typeColor = Color.RED;
            }
            Picasso.get().load(typeDrawable).into(type);
            type.setColorFilter(typeColor);
            streamerName.setText(channel.getName());
            int viewersCount = item.getViewers();
            int viewersRes;
            if (viewersCount != 1) {
                viewersRes = R.string.viewers;
            } else {
                viewersRes = R.string.viewer;
            }
            viewers.setText(context.getString(viewersRes, viewersCount));
            title.setText(channel.getStatus());
            gameName.setText(item.getGame());
            cardView.setOnClickListener(v -> itemClickListener.startStream(item));
        }

        void update(Bundle bundle) {
            for (String key : bundle.keySet()) {
                switch (key) {
                    case KEY_VIEWERS:
                        viewers.setText(String.valueOf(bundle.getInt(key)));
                        break;
                    case KEY_PREVIEW:
                        Picasso.get().load(((Preview) bundle.getParcelable(key)).getMedium()).fit().into(thumbnail);
                        break;
                    case KEY_GAME:
                        gameName.setText(bundle.getString(key));
                        break;
                    case KEY_TITLE:
                        title.setText(bundle.getString(key));
                        break;
                }
            }
        }
    }
}
