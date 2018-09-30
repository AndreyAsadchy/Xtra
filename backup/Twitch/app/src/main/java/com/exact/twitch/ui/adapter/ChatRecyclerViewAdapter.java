package com.exact.twitch.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.exact.twitch.R;
import com.exact.twitch.model.chat.Badge;
import com.exact.twitch.model.chat.ChatMessage;
import com.exact.twitch.model.chat.Emote;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> {

    private final int[] twitchColors = { -65536, -16776961, -16744448, -5103070, -32944, -6632142, -47872, -13726889, -2448096, -2987746, -10510688, -14774017, -38476, -7722014, -16711809 };

    private Random random = new Random();
    private List<ChatMessage> messages = new ArrayList<>(0);
    private final Context context;

    public ChatRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        ChatMessage chatMessage = messages.get(position);
        int index = 0;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String badgesUrl = "https://static-cdn.jtvnw.net/chat-badges/";
        List<Emote> images = new ArrayList<>();
        for (Badge badge : chatMessage.getBadges()) {
            String url = null;
            switch (badge.getId()) {
                case "admin":
                    url = badgesUrl + "admin.png";
                    break;
                case "bits":
                    int count = Integer.parseInt(badge.getVersion());
                    String color;
                    if (count < 100) {
                        color = "gray";
                    } else if (count < 1000) {
                        color = "purple";
                    } else if (count < 5000) {
                        color = "green";
                    } else if (count < 10000) {
                        color = "blue";
                    } else {
                        color = "red";
                    }
                    url = String.format("https://static-cdn.jtvnw.net/bits/dark/static/%s/1", color); //TODO change theme based on app theme
                    break;
                case "broadcaster":
                    url = badgesUrl + "broadcaster.png";
                    break;
                case "global_mod":
                    url = badgesUrl + "globalmod.png";
                    break;
                case "moderator":
                    url = badgesUrl + "mod.png";
                    break;
                case "subscriber":
                    url = chatMessage.getSubscriberBadge().getImageUrl2x();
                    break;
                case "staff":
                    url = badgesUrl + "staff.png";
                    break;
                case "turbo":
                    url = badgesUrl + "turbo.png";
                    break;
                case "sub-gifter":
                    url = "https://static-cdn.jtvnw.net/badges/v1/4592e9ea-b4ca-4948-93b8-37ac198c0433/1";
                    break;
                case "premium":
                    url = "https://static-cdn.jtvnw.net/badges/v1/a1dd5073-19c3-4911-8cb4-c464a7bc1510/1";
                    break;
                case "partner":
                    url = "https://static-cdn.jtvnw.net/badges/v1/d12a2e27-16f6-41d0-ab77-b780518f00a3/2";
                    break;
                case "clip-champ":
                    url = "https://static-cdn.jtvnw.net/badges/v1/f38976e0-ffc9-11e7-86d6-7f98b26a9d79/2";
                    break;
            }
            //TODO if badges don't wait for emotes
            if (url != null) {
                builder.append(" ");
                images.add(new Emote(url, index, ++index));
            }
//            loadImage(holder.tvMessage, url, builder, index, ++index) ;
        }
        //TODO add if mentions users make message red
        builder.append(chatMessage.getUserName()).append(": ").append(chatMessage.getMessage());
        Integer color;
        String userColor = chatMessage.getColor();
        if (userColor == null) {
            int randomColor = getRandomColor();
            chatMessage.setColor(String.valueOf(randomColor));
            color = randomColor;
        } else {
            color = userColor.startsWith("#") ? Color.parseColor(userColor) : Integer.valueOf(userColor);
        }
        int userNameLength = chatMessage.getUserName().length();
        builder.setSpan(new ForegroundColorSpan(color), index, index + userNameLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        List<Emote> emotes = chatMessage.getEmotes();
        if (emotes.size() == 0) {
            holder.tvMessage.setText(builder);
            loadImage(holder.tvMessage, builder, images);
            return;
        }
        index += userNameLength + 2;
        String emotesUrl = "https://static-cdn.jtvnw.net/emoticons/v1/";
        SpannableStringBuilder tempBuilder = new SpannableStringBuilder(builder);
        String message = chatMessage.getMessage();
        for (int i = 0; i < emotes.size(); i++) {
            Emote emote = emotes.get(i);
            int test = tempBuilder.toString().indexOf(message.substring(emote.getBegin(), emote.getEnd() + 1));
            tempBuilder.replace(test, test + (emote.getEnd() - emote.getBegin()) + 1, " ");
//            loadImage(holder.tvMessage, emotesUrl + emote.getId() + "/2.0", builder, index + emote.getBegin(), index + emote.getEnd() + 1);
            images.add(new Emote(emotesUrl + emote.getId() + "/2.0", index + emote.getBegin(), index + emote.getEnd() + 1));
        }
        holder.tvMessage.setText(tempBuilder);
        loadImage(holder.tvMessage, builder, images);
    }

    public void submitList(List<ChatMessage> list) {
        messages = list;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private int getRandomColor() {
        return twitchColors[random.nextInt(twitchColors.length)];
    }

    private void loadImage(TextView textView, SpannableStringBuilder builder, List<Emote> images) {
        final int[] leftToLoad = { images.size() };
        for (Emote image : images) {
            Picasso.get().load(image.getId()).resize(50, 50).into(new Target() { //TODO change size
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    builder.setSpan(new ImageSpan(drawable), image.getBegin(), image.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (--leftToLoad[0] == 0) {
                        textView.setText(builder);
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });

        }
    }

    private void loadImage(TextView textView, String uri, SpannableStringBuilder builder, int start, int end) {
        Picasso.get().load(uri).resize(30, 30).into(new Target() { //TODO change size
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                builder.setSpan(new ImageSpan(drawable), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(builder);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        ViewHolder(View view) {
            super(view);
            tvMessage = (TextView) view;
        }
    }
}
