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

class AudioBookPlayerActivity extends android.app.Activity with OnCompletionListener with SeekBar.OnSeekBarChangeListener {
  // Handler to update UI timer, progress bar etc,.
  private val mHandler = new android.os.Handler

  // TypedResource
  private object TR {
    private def v[A <: View](id: Int) = findViewById(id).asInstanceOf[A]
    val btnPlay                   = v[ImageButton](R.id.btnPlay)
    val btnForward                = v[ImageButton](R.id.btnForward)
    val btnBackward               = v[ImageButton](R.id.btnBackward)
    val btnNext                   = v[ImageButton](R.id.btnNext)
    val btnPrevious               = v[ImageButton](R.id.btnPrevious)
    val btnPlaylist               = v[ImageButton](R.id.btnPlaylist)
    val audioProgressBar          = v[SeekBar](R.id.audioProgressBar)
    val audioTitleLabel           = v[TextView](R.id.audioTitle)
    val audioCurrentDurationLabel = v[TextView](R.id.audioCurrentDurationLabel)
    val audioTotalDurationLabel   = v[TextView](R.id.audioTotalDurationLabel)
  }

  override protected def onCreate(savedInstanceState: android.os.Bundle) {
    Log.d("Watch", "Watch -- onCreate!")
    super.onCreate(savedInstanceState)
    requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
    setContentView(R.layout.player)

    // Start listening for button presses
    val am = getSystemService(Context.AUDIO_SERVICE).asInstanceOf[android.media.AudioManager]
    val myEventReceiver = new android.content.ComponentName(getPackageName, classOf[RemoteControlReceiver].getName)
    am.registerMediaButtonEventReceiver(myEventReceiver)

    if(Player.playList.isEmpty){
      val s = AudioFilesManager.MEDIA_PATH + " に音声ファイルが見つかりませんでした。"
      (new android.app.AlertDialog.Builder(this)).setMessage(s).setPositiveButton(android.R.string.ok, null).show()
      return
    }

    Player.initialize
    referPlayer

    // Listeners
    TR.audioProgressBar.setOnSeekBarChangeListener(this) // Important
    Player.mp.setOnCompletionListener(this) // Important

    /**
     * Play button click event
     */
    TR.btnPlay.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { Player.togglePlaying; referPlayer }
    })

    /**
     * Forward button click event
     * Forwards audio specified seconds
     */
    TR.btnForward.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { Player.forword; referPlayerProgress }
    })

    /**
     * Backward button click event
     * Backward audio to specified seconds
     */
    TR.btnBackward.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { Player.backward; referPlayerProgress }
    })

    /**
     * Next button click event
     */
    TR.btnNext.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) = Player.mp.isPlaying match {
        case true  => Player.next; startPlaying
        case false => Player.next; referPlayer
      }
    })

    /**
     * Previous button click event
     */
    TR.btnPrevious.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) = Player.mp.isPlaying match {
        case true  => Player.previous; startPlaying
        case false => Player.previous; referPlayer
      }
    })

    /**
     * Button Click event for Play list click event
     * Launches list activity which displays list of audio
     */
    TR.btnPlaylist.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) {
        val i = new Intent(getApplicationContext(), classOf[PlayListActivity])
        startActivityForResult(i, 100)
      }
    })

  } //end onCreate
  
  /**
   * dispatchKeyEvent
   */
  override def dispatchKeyEvent(e: KeyEvent) = {
    Log.d("Watch", "Watch -- dispatchKeyEvent! keyCode = " + e.getKeyCode)
    (e.getAction, e.getKeyCode)  match {
      case (_, KeyEvent.KEYCODE_BACK) => false //don't destroy this activity
      case (KeyEvent.ACTION_UP, _ ) => referPlayer; super.dispatchKeyEvent(e) //for remote controller
      case (_, _) => super.dispatchKeyEvent(e)
    }
  }

  /**
   * Receiving audio index from playlist view and play the audio
   */
  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if(resultCode == 100){
      val afterFunc = if(Player.mp.isPlaying){ () => startPlaying }else{ () => referPlayer } // for keep playing state
      Player.setIndex(data.getExtras().getInt("audioIndex"))
      afterFunc()
    }
  }

  /**
   * When user starts moving the progress handler
   */
  override def onStartTrackingTouch(seekBar: SeekBar) { stopUpdateProgressBar }

  /**
   * onProgressChanged
   */
  override def onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) = fromTouch match {
    case true => TR.audioCurrentDurationLabel.setText(Player.getTimeStringByPercentage(progress))
    case false =>
  }

  /**
   * When user stops moving the progress hanlder
   */
  override def onStopTrackingTouch(seekBar: SeekBar) {
    stopUpdateProgressBar //stop update timer progress
    val currentPosition = Utilities.progressToMilliSeconds(seekBar.getProgress, Player.mp.getDuration)

    Player.mp.seekTo(currentPosition) //forward or backward to certain seconds
    updateProgressBar //update timer progress again
  }

  /**
   * On Audio Playing completed
   */
  override def onCompletion(arg0: MediaPlayer) {
    Player.initialize //to first audio
    referPlayer
  }

  /**
   * onStart
   */
  override def onStart {
    super.onStart
    Log.d("Watch", "Watch -- onStart!")
    if(!Player.playList.isEmpty){
      referPlayer
      updateProgressBar
    }
  }

  /**
   * onStop
   */
  override def onStop {
    super.onStop
    Log.d("Watch", "Watch -- onStop!")
    stopUpdateProgressBar
  }

  /**
   * onDestroy
   */
  override def onDestroy {
    super.onDestroy
    Log.d("Watch", "Watch -- onDestroy!")
    Player.mp.release
  }

  /**
   * Function to play a audio
   */
  def startPlaying() {
    Player.mp.start
    referPlayer
  }

  /**
   * Function to pause a audio
   */
  def pausePlaying() {
    Player.mp.pause
    referPlayer
  }

  /**
   * Update timer on seekbar
   */
  def updateProgressBar()     = mHandler.postDelayed(mUpdateTimeTask, 100)
  def stopUpdateProgressBar() = mHandler.removeCallbacks(mUpdateTimeTask)

  /**
   * Background Runnable thread
   */
  val mUpdateTimeTask = new Runnable {
    override def run() {
      referPlayerProgress
      mHandler.postDelayed(this, 100) //Running this thread after 100 milliseconds
    }
  }

  /**
   * Refer Player progress to Btns
   */
  def referPlayerProgress {
    TR.audioProgressBar.setProgress(Player.getProgressPercentage) //Updating progress bar
    TR.audioCurrentDurationLabel.setText(Player.getCurrentTimeString) //Displaying time completed playing
  }

  /**
   * Refer all Player state to Btns
   */
  def referPlayer {
    Log.d("Watch", "Watch -- referPlayer!")
    referPlayerProgress
    TR.audioTitleLabel.setText(Player.getTitle) //Displaying Audio title
    TR.audioTotalDurationLabel.setText(Player.getTotalTimeString) //Displaying Total Duration time
    TR.btnPlay.setImageResource(if(Player.mp.isPlaying) R.drawable.btn_pause else R.drawable.btn_play) //Changing Button Image
    TR.audioProgressBar.setProgress(Player.getProgressPercentage) //set Progress bar values
    TR.audioProgressBar.setMax(100) //set Progress bar values
  }
} //end AndroidBuildingMusicPlayerActivity

/**
 * RemoteControlReceiver
 * http://firespeed.org/diary.php?diary=kenz-1519-junl18
 */
class RemoteControlReceiver extends BroadcastReceiver {
  override def onReceive(context: Context, intent: Intent){
    Log.d("Watch", "Watch -- onReceive!")
    if(intent.getAction != Intent.ACTION_MEDIA_BUTTON){ return }

    val keyEv = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT).asInstanceOf[KeyEvent]
    if(keyEv.getAction != KeyEvent.ACTION_DOWN){ return }

    val keyCode = keyEv.getKeyCode
    val KEYCODE_MEDIA_PLAY  = 126
    val KEYCODE_MEDIA_PAUSE = 127

    keyCode match {
      case KEYCODE_MEDIA_PLAY | KEYCODE_MEDIA_PAUSE | KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE => Player.togglePlaying
      case _ =>
    }
  }// end onReceive
}