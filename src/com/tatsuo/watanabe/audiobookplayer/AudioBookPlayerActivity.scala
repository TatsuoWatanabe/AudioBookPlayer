package com.tatsuo.watanabe.audiobookplayer

import android.util.Log
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.view.View
import android.view.KeyEvent
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context

class AudioBookPlayerActivity extends android.app.Activity /*with OnCompletionListener with SeekBar.OnSeekBarChangeListener */ {
  override protected def onCreate(savedInstanceState: android.os.Bundle) {
    Log.d("Watch", "Watch -- onCreate!")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    //setContentView(R.layout.player)
  }
}