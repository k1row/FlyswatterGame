package jp.k16.flyswattergame;

import android.app.Activity;
import android.os.Bundle;

import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.view.Gravity;
import android.view.KeyEvent;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;



import android.media.AudioManager;


public class MainActivity extends Activity
{
  private Button m_retry_button;
  private MyBgm m_bgm; // BGM

  private MyRenderer m_renderer;

  // バックグランド時に制限時間が減らないようにする
  private long m_pause_time = 0L;

  /** Called when the activity is first created. */
  @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate (savedInstanceState);

      // フルスクリーン表示
      getWindow ().addFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN);

      // タイトルバー非表示
      requestWindowFeature (Window.FEATURE_NO_TITLE);

      // 音量変更をボリュームボタンで出来るように
      setVolumeControlStream (AudioManager.STREAM_MUSIC);

      try
      {
        PackageManager pm = getPackageManager ();
        ApplicationInfo ai = pm.getApplicationInfo (getPackageName (), 0);
        Global.is_debug = (ApplicationInfo.FLAG_DEBUGGABLE == (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE));
      }
      catch (NameNotFoundException e)
      {
        e.printStackTrace ();
      }

      Global.main_activity = this;

      this.m_renderer = new MyRenderer (getApplicationContext ());
      MyGLSurfaceView glSurfaceView = new MyGLSurfaceView (this);
      glSurfaceView.setRenderer (m_renderer);

      setContentView (glSurfaceView);

      //ボタンのレイアウト
      FrameLayout.LayoutParams params = new FrameLayout.LayoutParams (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;

      // ゲームオーバーテクスチャに被らないようにする
      params.setMargins (0, (int)(getResources ().getDisplayMetrics ().density * 150), 0, 0);

      // ボタンの作成
      this.m_retry_button = new Button (this);
      this.m_retry_button.setText ("Retry");

      // 非表示に
      hide_retry_button ();
      addContentView (m_retry_button, params);

      // イベントの追加
      this.m_retry_button.setOnClickListener (
        new Button.OnClickListener () {
          @Override
            public void onClick (View v)
            {
              hide_retry_button ();
              m_renderer.start_new_game ();
            }
      });

      this.m_bgm = new MyBgm (this);

      // 保存されているゲーム情報が存在している場合はそれを復帰させる
      if (savedInstanceState != null)
      {
        long start_time = savedInstanceState.getLong ("start_time");
        long pause_time = savedInstanceState.getLong ("pause_time");
        int score = savedInstanceState.getInt ("score");
        long paused_time = pause_time - start_time;

        // 時間とスコアの復帰
        m_renderer.substract_paused_time (paused_time);
        m_renderer.set_score (score);
      }
    }

  @Override
    public void onResume ()
    {
      super.onResume ();

      if (m_pause_time != 0L)
      {
        // 現在時刻との差分をみてどれだけの時間バックグランドになっていたか計算
        long pause_time = System.currentTimeMillis () - m_pause_time;
        m_renderer.substract_paused_time (pause_time);
      }

      // バックグラウンドから復帰したら音楽も再生する
      m_bgm.start ();
    }

  @Override
    public void onPause ()
    {
      super.onPause ();

      // アプリがバックグラウンドになったら音楽も停止する
      m_bgm.stop ();

      // テクスチャを削除する
      TextureManager.delete_all (Global.gl);

      // バックグランドになった時刻を覚えておく
      m_pause_time = System.currentTimeMillis ();
    }

  /* アプリの状態を保存しておく。開始時間とスコアが保存できていれば
     Activityが破棄された状態でも、ゲーム進行上では元に戻せる
  */
  @Override
    public void onSaveInstanceState (Bundle outState)
    {
      super.onSaveInstanceState (outState);

      // 開始時間
      outState.putLong ("start_time", m_renderer.get_start_time ());

      // onPauseした時間
      outState.putLong ("pause_time", System.currentTimeMillis ());

      // スコア
      outState.putInt ("score", m_renderer.get_score ());
    }

  // ハードウェアボタンを制御する
  @Override
    public boolean dispatchKeyEvent (KeyEvent event)
    {
      if (event.getAction () == KeyEvent.ACTION_DOWN) // キーが押された
      {
        switch (event.getKeyCode ())
        {
        case KeyEvent.KEYCODE_BACK:  // Backボタン
          return false;
        default:
        }
      }
      return super.dispatchKeyEvent (event);
    }

	//リトライボタンを表示する
  public void show_retry_button ()
  {
    m_retry_button.setVisibility (View.VISIBLE);
  }
  //リトライボタンを非表示にする
  public void hide_retry_button ()
  {
    m_retry_button.setVisibility (View.INVISIBLE);
  }
}
