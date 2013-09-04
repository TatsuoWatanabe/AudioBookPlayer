package com.tatsuo.watanabe.audiobookplayer

import android.widget.SimpleAdapter
import android.widget.AdapterView

class PlayListActivity extends android.app.ListActivity  {
  val playList = Player.playList

  override protected def onCreate(savedInstanceState: android.os.Bundle) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
    setContentView(R.layout.playlist)

    // Adding menuItems to ListView
    setListAdapter(new SimpleAdapter(
      this,
      playList,
      R.layout.playlist_item,
      Array("audioTitle"),
      Array(R.id.audioTitle)
    ))

    // listening to single listitem click
    getListView.setOnItemClickListener(new AdapterView.OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: android.view.View, position: Int, id: Long) {
        // Starting new intent
        val in = new android.content.Intent(getApplicationContext(), classOf[AudioBookPlayerActivity])
        
        // Sending songIndex to PlayerActivity
        setResult(100, in.putExtra("audioIndex", position))
        finish
      }// end onItemClick
    })// end setOnItemClickListener
  }// end onCreate
  
}