package com.github.exact7.xtra.model.video

import com.google.gson.annotations.SerializedName

class VideosResponse(@SerializedName(value = "vods", alternate = ["videos"]) val videos: List<Video>)