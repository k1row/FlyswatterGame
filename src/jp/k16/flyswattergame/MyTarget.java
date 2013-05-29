package jp.k16.flyswattergame;


import javax.microedition.khronos.opengles.GL10;
import android.util.Log;


public class MyTarget
{
  // 標的（ハエ）の状態
  public float m_x, m_y;         // 位置（座標）
  public float m_angle;          // 角度
  public float m_size;           // サイズ
  public float m_speed;          // 移動速度
  public float m_turn_angle;     // 旋回角度

  public MyTarget (float x, float y,
                   float angle, float size,
                   float speed, float turn_angle)
  {
    this.m_x = x;
    this.m_y = y;
    this.m_angle = angle;
    this.m_size = size;
    this.m_speed = speed;
    this.m_turn_angle = turn_angle;
  }

  public void move ()
  {
    // 標的（ハエ）を動かす
    // X軸方向の移動量 = cos (現在向いている方法) × 進行速度
    // Y軸方向の移動量 = sin (現在向いている方法) × 進行速度

    float theta = m_angle / 180.0f * (float)Math.PI;
    m_x = m_x + (float)Math.cos (theta) * m_speed;
    m_y = m_y + (float)Math.sin (theta) * m_speed;

    // ワープ処理（画面を出てからしばらく外側まで動いたら行う)
    if (m_x >=  2.0f) m_x -= 4.0f;
    if (m_x <= -2.0f) m_x += 4.0f;
    if (m_y >=  2.5f) m_y -= 5.0f;
    if (m_y <= -2.5f) m_y += 5.0f;
  }

  // ポイントが当たり判定の範囲内かを返す
  public boolean is_point_inside (float x, float y)
  {
    // 標的とタッチされたポイントとの距離を計算する
    float dx = x - m_x;
    float dy = y - m_y;
    float distance = (float)Math.sqrt (dx * dx + dy * dy);

    // 距離が標的のサイズ（半径）より小さければ当たった事にする
    if (distance <= m_size * 0.5f)
    {
      Log.i ("FlyswatterGame", String.format ("HIT !!"));
      return true;
    }
    return false;
  }

  public void draw (GL10 gl, int texture)
  {
    // 標的（ハエ）を描画する
    gl.glPushMatrix ();
    {
      gl.glTranslatef (m_x, m_y, 0.0f);          // 平行移動する
      gl.glRotatef (m_angle, 0.0f, 0.0f, 1.0f);  // 回転する
      gl.glScalef (m_size, m_size, 1.0f);        // 拡大させるためここの大きさを変更
      GraphicUtil.draw_texture (gl,
                                0.0f, 0.0f, 1.0f, 1.0f, texture,
                                1.0f, 1.0f, 1.0f, 1.0f);
    }
    gl.glPopMatrix ();
  }
}
