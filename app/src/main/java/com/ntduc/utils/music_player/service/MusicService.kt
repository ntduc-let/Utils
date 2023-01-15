package com.ntduc.utils.music_player.service

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MusicService : MediaSessionService() {
  private var mediaSession: MediaSession? = null
  private var player: Player? = null
  
  // Create your Player and MediaSession in the onCreate lifecycle event
  override fun onCreate() {
    super.onCreate()
    player = ExoPlayer.Builder(this).build().apply {
      setHandleAudioBecomingNoisy(true)
    }
    mediaSession = MediaSession.Builder(this, player!!).build()
  }
  
  // Return a MediaSession to link with the MediaController that is making
  // this request.
  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
    mediaSession
}