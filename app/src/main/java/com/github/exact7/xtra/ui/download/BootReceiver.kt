package com.github.exact7.xtra.ui.download

//class BootReceiver : BroadcastReceiver() {
//
//    @Inject
//    lateinit var fetchProvider: FetchProvider
//
//    @Inject
//    lateinit var offlineRepository: OfflineRepository
//
//    override fun onReceive(context: Context, intent: Intent) {
//        Log.d("BootReceiver", "Boot received")
//        AndroidInjection.inject(this, context)
//        GlobalScope.launch {
//            try {
//                val unfinishedVideos = offlineRepository.getUnfinishedVideos()
//                unfinishedVideos.forEach {
//                    DownloadUtils.download()
//                }
//                with(fetchProvider.get(false)) {
//                    cancelAll()
//                    deleteAll()
//                    close()
//                }
//            } catch (e: Exception) {
//                Crashlytics.logException(e)
//            }
//        }
//    }
//}