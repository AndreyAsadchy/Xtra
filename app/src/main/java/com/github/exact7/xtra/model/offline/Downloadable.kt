package com.github.exact7.xtra.model.offline

interface Downloadable {
    val id: String
    val title: String
    val thumbnail: String
    val channelName: String
    val channelLogo: String
    val game: String
    val uploadDate: String
}

internal class Wrapper(downloadable: Downloadable) : Downloadable {

    override val id: String
    override val title: String
    override val thumbnail: String
    override val channelName: String
    override val channelLogo: String
    override val game: String
    override val uploadDate: String

    init {
        downloadable.let {
            id = it.id
            title = it.title
            thumbnail = it.thumbnail
            channelName = it.channelName
            channelLogo = it.channelLogo
            game = it.game
            uploadDate = it.uploadDate
        }
    }
}