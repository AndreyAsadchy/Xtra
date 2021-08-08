package com.github.andreyasadchy.xtra.ui.videos

import com.github.andreyasadchy.xtra.model.kraken.video.Video
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel

abstract class BaseVideosViewModel(private val playerRepository: PlayerRepository) : PagedListViewModel<Video>() {

    val positions = playerRepository.loadVideoPositions()
}