package jp.k16.flyswattergame;

import android.content.Context;
import android.media.MediaPlayer;


// BGM
public class MyBgm
{
  private MediaPlayer m_bgm;

  public MyBgm (Context context)
  {
    this.m_bgm = MediaPlayer.create (context, R.raw.master1);
    this.m_bgm.setLooping (true);       // ループするようにする
    this.m_bgm.setVolume (1.0f, 1.0f);  // 左右のボリュームを最大に
  }

  // BGM再生
  public void start ()
  {
    if (!m_bgm.isPlaying ())
    {
      m_bgm.seekTo (0);
      m_bgm.start ();
    }
  }
  // BGM停止
  public void stop ()
  {
    if (m_bgm.isPlaying ())
    {
      m_bgm.stop ();

      /* 再生準備をする。
         停止時にはこれを呼び出して再生可能な状態に準備しないと
         再生できなくなってしまう
       */
      m_bgm.prepareAsync ();
    }
  }
}
