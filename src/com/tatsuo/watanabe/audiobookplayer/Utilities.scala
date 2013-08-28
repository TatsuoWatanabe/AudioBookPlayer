package com.tatsuo.watanabe.audiobookplayer

object Utilities {
  
  /**
   * Function to convert milliseconds time to
   * Timer Format
   * Hours:Minutes:Seconds
   * */
  def milliSecondsToTimeString(milliseconds: Long) = {
    val pattern = """(\d+):(\d+):(\d+)""".r
    val df = new java.text.SimpleDateFormat("H:mm:ss")
    df.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
    val hms = df.format(milliseconds)
    
    hms match {
      case pattern(h, mm, ss) if h == "0" && mm.head == '0' => mm.last.toString + ":" + ss
      case pattern(h, mm, ss) if h == "0"                   => mm + ":" + ss
      case _ => hms
    }
  }
  
  /**
   * Function to get Progress percentage
   * @param currentDuration
   * @param totalDuration
   * */
  def getProgressPercentage(currentDuration: Long, totalDuration: Long) = {
    val currentSeconds = currentDuration.toDouble / 1000
    val totalSeconds   = totalDuration.toDouble / 1000
    // calculating percentage
    (currentSeconds / totalSeconds * 100).toInt
  }

  /**
   * Function to change progress to timer
   * @param progress - 
   * @param totalDuration
   * returns current duration in milliseconds
   * */
  def progressToMilliSeconds(progress: Int, totalDuration: Int) = {
    val currentDuration = (progress.toDouble / 100 * totalDuration.toDouble / 1000).toInt
    
    // return current duration in milliseconds
    currentDuration * 1000
  }
  
  /**
   * Function to change progress to time string
   * @param progress
   * @param totalDuration
   */
  def progressToTimeString(progress: Int, totalDuration: Int) = {
    val m = progressToMilliSeconds(progress, totalDuration)
    milliSecondsToTimeString(m)
  }
}
