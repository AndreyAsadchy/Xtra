package com.github.exact7.xtra.ui.videos

import com.github.exact7.xtra.model.kraken.video.Video
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.ui.common.PagedListViewModel

abstract class BaseVideosViewModel(private val playerRepository: PlayerRepository) : PagedListViewModel<Video>() {

    val positions = playerRepository.loadVideoPositions()
}