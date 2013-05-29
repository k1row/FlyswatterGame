package jp.k16.flyswattergame;

import android.content.Context;

import android.media.SoundPool;
import android.media.AudioManager;
import android.util.Log;


// 効果音再生クラス
public class MySe
{
  private SoundPool m_sound_pool;
  private int m_hit_sound;  // 読み込んだ効果音オブジェクト

  public MySe (Context context)
  {
    // SoundPool (読み込むファイル数, 読み込む種類, 読み込む質)
    this.m_sound_pool = new SoundPool (1, AudioManager.STREAM_MUSIC, 0);

    try
    {
      // 効果音を読み込む  load (コンテキスト, 読み込むリソースID, 音の優先度)
      this.m_hit_sound = m_sound_pool.load (context, R.raw.explosion, 1);
    }
    catch (Exception e)
    {
      Log.e ("FlyswatterGame", e.getMessage ());
    }
  }

  public void play_hit_sound ()
  {
    // play (再生するサウンドID, 左のボリューム[0.0～1.0], 右のボリューム[0.0～1.0], 優先度[低い場合は0],
    //       ループ回数[0はしない、-1は無限ループ], 再生レート[通常1.0]
    m_sound_pool.play (m_hit_sound, 1.0f, 1.0f, 1, 0, 1.0f);
  }
}
