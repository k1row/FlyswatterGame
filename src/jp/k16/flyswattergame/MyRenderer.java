package jp.k16.flyswattergame;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.os.Handler;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.Log;


// 描画関連を受け持つクラス
public class MyRenderer implements GLSurfaceView.Renderer
{
  public static final int TARGET_NUM = 10;     // 標的の数
  public static final int GAME_INTERVAL = 20;  // 制限時間（秒）

  private Context m_context;

  private int m_width;
  private int m_height;

  // テクスチャ
  private int m_bg_texture;        // 背景
  private int m_target_texture;    // 標的（ハエ）
  private int m_number_texture;    // 得点ボード
  private int m_gameover_texture;  // ゲームオーバー用
  private int m_particle_texture;  // パーティクル用

  // 標的（ハエ）の状態
  private MyTarget[] m_targets = new MyTarget[TARGET_NUM];

  private int m_score;              // 得点
  private long m_start_time;        // 開始時間
  private boolean m_gameover_flag;  // ゲームオーバーかどうか

  private Handler m_handler = new Handler ();

  private MySe m_se;   // 効果音

  private ParticleSystem m_particle_system;  // パーティクルシステム

  /* FPS関連 */
  private long m_fps_count_start_time = System.currentTimeMillis ();
  private int m_frame_in_second = 0;
  private int m_fps = 0;

  public long get_start_time () { return m_start_time; }
  public int get_score () { return m_score; }
  public void set_score (int score) { m_score = score; }

  public MyRenderer (Context context)
  {
    this.m_context = context;
    this.m_se = new MySe (context);
    this.m_particle_system = new ParticleSystem (100, 30);
    start_new_game ();
  }

  public void start_new_game ()
  {
    Random rand = Global.rand;

    // 標的（ハエ）の状態を初期化する
    for (int i = 0; i < TARGET_NUM; i++)
    {
      // 標的の初期座標は(-1.0～1.0, -1.0～1.0)の間のランダムな地点にする
      float x = rand.nextFloat () * 2.0f - 1.0f;
      float y = rand.nextFloat () * 2.0f - 1.0f;

      // 角度をランダムにする
      float angle = rand.nextInt (360);

      // 標的の大きさを0.25～0.5の間でランダムに決定する
      float size = rand.nextFloat () * 0.25f + 0.25f;

      // 標的の移動速度を0.01～0.02の間でランダムにする
      float speed = rand.nextFloat () * 0.01f + 0.01f;

      // 標的の旋回角度を-2.0f～2.0fの間でランダムに決定する
      float turn_angle = rand.nextFloat () * 4.0f - 2.0f;

      m_targets[i] = new MyTarget (x, y, angle, size, speed, turn_angle);
    }

    this.m_score = 0;
    this.m_start_time = System.currentTimeMillis ();  // 開始時間を設定
    this.m_gameover_flag = false;
  }

  public void render (GL10 gl)
  {
    // 経過時間を計算する
    int pass_time = (int)(System.currentTimeMillis () - m_start_time) / 1000;
    Log.i ("FlyswatterGame", "pass_time = " + pass_time);

    int remain_time = GAME_INTERVAL - pass_time;  // 残り時間
    if (remain_time <= 0)
    {
      remain_time = 0;         // マイナスにならないよう調整

      if (!m_gameover_flag)
      {
        m_gameover_flag = true;  // ゲームオーバー状態に

        // Global.main_activity.show_retry_button ()をUIスレッド上で実行
        // Handlerを使ってリトライボタンを表示する
        m_handler.post (new Runnable () {
          @Override
            public void run ()
            {
              Global.main_activity.show_retry_button ();
            }
        });
      }
    }

    // ランダムなタイミングで方向転換するようにする
    Random rand = Global.rand;

    MyTarget[] targets = m_targets;

    // すべての標的を1つずつ動かす
    for (int i = 0; i < TARGET_NUM; i++)
    {
      if (rand.nextInt (100) == 0)
      {
        // 旋回する角度を -2.0～2.0 の間でランダムに設定
        targets[i].m_turn_angle = rand.nextFloat () * 4.0f - 2.0f;
      }

      // 標的（ハエ）を旋回
      targets[i].m_turn_angle = targets[i].m_angle + targets[i].m_turn_angle;

      targets[i].move ();

      /* パーティクルを使って軌跡を描画する */
      // ちょっと揺らぎの効果を入れてみる
      float move_x = (rand.nextFloat () - 0.5f) * 0.01f;
      float move_y = (rand.nextFloat () - 0.5f) * 0.01f;
      m_particle_system.add (targets[i].m_x, targets[i].m_y, 0.1f, move_x, move_y);
    }

    // 背景を描画する
    GraphicUtil.draw_texture (gl,
                              0.0f, 0.0f, 2.0f, 3.0f, m_bg_texture,
                              1.0f, 1.0f, 1.0f, 1.0f);

    // パーティクルを描画する
    m_particle_system.update ();
    gl.glEnable (GL10.GL_BLEND);
    gl.glBlendFunc (GL10.GL_SRC_ALPHA, GL10.GL_ONE);
    m_particle_system.draw (gl, m_particle_texture);

    // 標的を描画する
    for (int i = 0; i < TARGET_NUM; i++)
    {
      targets[i].draw (gl, m_target_texture);
    }

    gl.glDisable (GL10.GL_BLEND);

    // 得点を描画する
    GraphicUtil.draw_number (gl, -0.5f, 1.25f, 0.125f, 0.125f, m_number_texture, m_score,
                             8, 1.0f, 1.0f, 1.0f, 1.0f);

    // 残り時間を描画する
    GraphicUtil.draw_number (gl, 0.5f, 1.2f, 0.4f, 0.4f, m_number_texture, remain_time,
                             2, 1.0f, 1.0f, 1.0f, 1.0f);


    /* デバッグモードならFPSを表示する */
    if (Global.is_debug)
    {
      long now_time = System.currentTimeMillis ();

      // 現在時間との差分を計算する
      long diff = now_time - m_fps_count_start_time;

      // 1秒以上経過していた場合は、フレーム数のカウント終了
      if (diff >= 1000)
      {
        m_fps = m_frame_in_second;
        m_frame_in_second = 0;
        m_fps_count_start_time = now_time;
      }
      m_frame_in_second++;  // フレーム数をカウント
      GraphicUtil.draw_number (gl, -0.5f, -1.25f, 0.2f, 0.2f, m_number_texture, m_fps,
                               2, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    // 終了ならゲームオーバー描画する
    if (m_gameover_flag)
    {
      GraphicUtil.draw_texture (gl,
                                0.0f, 0.0f, 2.0f, 0.5f, m_gameover_texture,
                                1.0f, 1.0f, 1.0f, 1.0f);
    }
  }

  @Override
    public void onDrawFrame (GL10 gl)
    {
      gl.glViewport (0, 0, m_width, m_height);

      gl.glMatrixMode (GL10.GL_PROJECTION);
      gl.glLoadIdentity ();
      gl.glOrthof (-1.0f, 1.0f, -1.5f, 1.5f, 0.5f, -0.5f);
      gl.glMatrixMode (GL10.GL_MODELVIEW);
      gl.glLoadIdentity ();

      gl.glClearColor (0.5f, 0.5f, 0.5f, 1.0f);
      gl.glClear (GL10.GL_COLOR_BUFFER_BIT);

      render (gl);
    }

  @Override
    public void onSurfaceChanged (GL10 gl, int width, int height)
    {
      this.m_width = width;
      this.m_height = height;

      Global.gl = gl; // GLコンテキストを保持する

      load_texture (gl);
    }

  private void load_texture (GL10 gl)
  {
    // テクスチャの生成を行う
    Resources res = m_context.getResources ();

    // 背景
    this.m_bg_texture = GraphicUtil.load_texture (gl, res, R.drawable.circuit);
    if (m_bg_texture == 0)
      Log.e ("FlyswatterGame", "circuit texture load ERROR!");

    // 標的（ハエ）
    this.m_target_texture = GraphicUtil.load_texture (gl, res, R.drawable.fly);
    if (m_target_texture == 0)
      Log.e ("FlyswatterGame", "fly texture load ERROR!");

    // 得点ボード
    this.m_number_texture = GraphicUtil.load_texture (gl, res, R.drawable.number_texture);
    if (m_number_texture == 0)
      Log.e ("FlyswatterGame", "number texture load ERROR!");

    // ゲームオーバー用
    this.m_gameover_texture = GraphicUtil.load_texture (gl, res, R.drawable.game_over);
    if (m_gameover_texture == 0)
      Log.e ("FlyswatterGame", "gameover texture load ERROR!");

    // パーティクル用
    this.m_particle_texture = GraphicUtil.load_texture (gl, res, R.drawable.particle_blue);
    if (m_particle_texture == 0)
      Log.e ("FlyswatterGame", "particle_blue texture load ERROR!");
  }

  @Override
    public void onSurfaceCreated (GL10 gl, EGLConfig config)
    {
    }

  // 画面がタッチされたら、MyGLSurfaceViewから呼ばれるイベント
  public void touched (float x, float y)
  {
    // ゲームオーバーなら処理終了
    if (m_gameover_flag)
      return;

    Log.i ("FlyswatterGame", String.format ("MyRenderer touched ! x = %f, y=%f", x, y));

    Random rand = Global.rand;
    MyTarget[] targets = m_targets;
    // すべての標的との当たり判定をする
    for (int i = 0; i < TARGET_NUM; i++)
    {
      // 距離が標的のサイズ（半径）より小さければ当たった事にする
      if (targets[i].is_point_inside (x, y))
      {
        // パーティクルを放出する
        for (int j = 0; j < 40; j++)
        {
          float move_x = (rand.nextFloat () - 0.5f) * 0.05f;
          float move_y = (rand.nextFloat () - 0.5f) * 0.05f;
          m_particle_system.add (targets[i].m_x, targets[i].m_y, 0.2f, move_x, move_y);
        }

        m_se.play_hit_sound ();  // 標的が叩かれたので、効果音を
        m_score += 100;          // 100点加算

        // 新しい評定をランダムな位置に表示（移動）する
        float dist = 2.0f;  // 画面中方から2.0f離れた円周上の点

        // 適当な位置に配置する
        float theta = Global.rand.nextFloat () * 360.0f / 180.0f * (float)Math.PI;
        targets[i].m_x = (float)Math.cos (theta) * dist;
        targets[i].m_y = (float)Math.sin (theta) * dist;
      }
    }
  }

  // ポーズしていた時間分を経過時間から除外する処理
  public void substract_paused_time (long paused_time)
  {
    m_start_time += paused_time;
  }
}
