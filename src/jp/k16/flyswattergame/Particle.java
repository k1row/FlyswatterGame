package jp.k16.flyswattergame;


import javax.microedition.khronos.opengles.GL10;


public class Particle
{
  public float m_x;
  public float m_y;
  public float m_size;

  public float m_move_x;  // 1フレームあたりのX軸方向の移動量
  public float m_move_y;  // 1フレームあたりのY軸方向の移動量

  public boolean m_is_active;  // このパーティクルが使用中かどうか

  public int m_frame_number;  // 生成からの時間(フレーム数)
  public int m_life_span;     // 寿命(フレーム数)

  public Particle ()
  {
    this.m_x = 0.0f;
    this.m_y = 0.0f;
    this.m_size = 1.0f;
    this.m_is_active = false;

    this.m_move_x = 0.0f;  // 初期値は0(動かさない)
    this.m_move_y = 0.0f;  // 初期値は0(動かさない)

    this.m_frame_number = 0;
    this.m_life_span = 60;    // 初期値では寿命を60フレームに
  }

  public void draw (GL10 gl, int texture)
  {
    // 現在のフレームが寿命の間のどの位置にあるのか計算する
    float life_parcentage = (float)m_frame_number / (float)m_life_span;

    // パーティクルの寿命が半分以上残っている時はフェードイン
    // 残っていない時はフェードアウトするようにする
    float alpha = 1.0f;
    if (life_parcentage <= 0.5f)
      alpha = life_parcentage * 2.0f;  // フェードイン
    else
      alpha = 1.0f - (life_parcentage - 0.5f) * 2.0f;  // フェードアウト

    GraphicUtil.draw_texture (gl, m_x, m_y, m_size, m_size, texture, 1.0f, 1.0f, 1.0f, alpha);
  }

  public void update ()
  {
    // 寿命に達したら非アクティブに
    m_frame_number++;
    if (m_frame_number >= m_life_span)
      m_is_active = false;

    // 毎フレームの処理で動かす
    m_x += m_move_x;
    m_y += m_move_y;
  }
}
