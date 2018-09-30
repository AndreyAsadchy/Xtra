package com.exact.twitch.util.chat;

import android.util.SparseArray;

import com.exact.twitch.model.chat.Badge;
import com.exact.twitch.model.chat.ChatMessage;
import com.exact.twitch.model.chat.Emote;
import com.exact.twitch.model.chat.LiveChatMessage;
import com.exact.twitch.model.chat.SubscriberBadge;
import com.exact.twitch.tasks.LiveChatTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageListenerImpl implements LiveChatTask.OnMessageReceivedListener {

    private static final String TAG = MessageListenerImpl.class.getSimpleName();

    private final SparseArray<SubscriberBadge> subscriberBadges;
    private final OnChatMessageReceived callback;

    public MessageListenerImpl(SparseArray<SubscriberBadge> subscriberBadges, OnChatMessageReceived callback) {
        this.subscriberBadges = subscriberBadges;
        this.callback = callback;
    }

    @Override
    public void onMessage(String message) {
        String[] parts = message.split(" ", 2);
        String prefix = parts[0];
        Map<String, String> prefixes = splitAndMakeMap(prefix, ";", "=");
        LiveChatMessage.Builder builder = new LiveChatMessage.Builder();
        String badges = prefixes.get("@badges");
        if (badges != null) {
            Map<String, String> map = splitAndMakeMap(badges, ",", "/");
            List<Badge> list = new ArrayList<>(map.size());
            for (Map.Entry<String, String> badge : map.entrySet()) {
                list.add(new Badge(badge.getKey(), badge.getValue()));
                if (badge.getKey().equals("subscriber")) {
                    int subscriptionMonths = Integer.parseInt(badge.getValue());
                    builder.setSubscriberBadge(subscriberBadges.get(subscriptionMonths));
                }
            }
            builder.setBadges(list);
        }
        builder.setColor(prefixes.get("color"));
        builder.setDisplayName(prefixes.get("display-name"));
        String emotes = prefixes.get("emotes");
        if (emotes != null) {
            Map<String, String> map = splitAndMakeMap(emotes, "/", ":");
            List<Emote> list = new ArrayList<>(map.size());
            for (Map.Entry<String, String> emote : map.entrySet()) {
                String[] emoteIndexes = emote.getValue().split(",");
                for (String indexes : emoteIndexes) {
                    String[] index = indexes.split("-");
                    list.add(new Emote(emote.getKey(), Integer.parseInt(index[0]), Integer.parseInt(index[1])));
                }
            }
            builder.setEmotes(list);
        }
        builder.setId(prefixes.get("id"));
        builder.setRoomId(prefixes.get("room-id"));
//        builder.setTimestamp(Long.parseLong(prefixes.get("tmi-sent-ts"))); //TODO
        builder.setUserId(Integer.parseInt(prefixes.get("user-id")));
        builder.setUserType(prefixes.get("user-type"));
        String messageInfo = parts[1]; //:<user>!<user>@<user>.tmi.twitch.tv PRIVMSG #<channel> :<message>
        builder.setUserName(messageInfo.substring(1,messageInfo.indexOf("!")));
        builder.setMessage(messageInfo.substring(messageInfo.indexOf(":",44) + 1)); //from <message>
        ChatMessage chatMessage = builder.build();
        callback.onMessage(chatMessage);
    }

    @Override
    public void onNotice(String message) {

    }

    @Override
    public void onUserNotice(String message) {

    }

    @Override
    public void onRoomState(String message) {

    }

    @Override
    public void onJoin(String message) {

    }

    private Map<String, String> splitAndMakeMap(String string, String splitRegex, String mapRegex) {
        String[] array = string.split(splitRegex);
        Map<String, String> map = new HashMap<>();
        for (String pair : array) {
            String[] kv = pair.split(mapRegex);
            String value = kv.length == 2 ? kv[1] : null;
            map.put(kv[0], value);
        }
        return map;
    }
}
