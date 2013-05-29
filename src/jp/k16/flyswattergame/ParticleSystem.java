package jp.k16.flyswattergame;


import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;


public class ParticleSystem
{
  // パーティクルの数
  public final int m_capacity;

  // パーティクル
  private Particle[] m_particles;

  public ParticleSystem (int capacity, int particle_life_span)
  {
    this.m_capacity = capacity;
    m_particles = new Particle[m_capacity];
    Particle[] particles = m_particles;
    for (int i = 0; i < m_capacity; i++)
    {
      particles[i] = new Particle ();
      particles[i].m_life_span = particle_life_span;
    }
  }

  public void add (float x, float y, float size, float move_x, float move_y)
  {
    Particle[] particles = m_particles;
    for (int i = 0; i < m_capacity; i++)
    {
      // 非アクティブのパーティクルを探す
      if (!particles[i].m_is_active)
      {
        particles[i].m_is_active = true;
        particles[i].m_x = x;
        particles[i].m_y = y;
        particles[i].m_size = size;
        particles[i].m_move_x = move_x;
        particles[i].m_move_y = move_y;
        particles[i].m_frame_number = 0;
        break;
      }
    }
  }

  public void draw (GL10 gl, int texture)
  {
    Particle[] particles = m_particles;

    /* 高速描画の為にまとめて描画する */
    // 頂点の配列 (1つのパーティクルあたり6項点 * 2要素)
    float[] vertices = GraphicUtil.get_vertices (6 * 2 * m_capacity);

    // 色の配列 (1つのパーティクルあたり6項点 * 4要素[R, G, B, A] * 最大パーティクル)
    float[] colors = GraphicUtil.get_colors (6 * 4 * m_capacity);

    // テクスチャマッピングの配列 (1つのパーティクルあたり6項点 * 2要素[x, y] * 最大パーティクル)
    float[] coords = GraphicUtil.get_coords (6 * 2 * m_capacity);

    // アクティブなパーティクルのカウント
    int vertext_index = 0;
    int color_index = 0;
    int tex_coord_index = 0;

    int active_particle_count = 0;

    for (int i = 0; i < m_capacity; i++)
    {
      // アクティブなパーティクルだけdrawする
      if (particles[i].m_is_active)
      {
        // 頂点座標を追加する
        float center_x = particles[i].m_x;
        float center_y = particles[i].m_y;

        float size = particles[i].m_size;
        float vleft = -0.5f * size + center_x;
        float vright = 0.5f * size + center_x;
        float vtop = 0.5f * size + center_y;
        float vbottom = -0.5f * size + center_y;

        /* 頂点座標をセット */

        // ポリゴン1
        vertices[vertext_index++] = vleft;
        vertices[vertext_index++] = vtop;
        vertices[vertext_index++] = vright;
        vertices[vertext_index++] = vtop;
        vertices[vertext_index++] = vleft;
        vertices[vertext_index++] = vbottom;

        // ポリゴン2
        vertices[vertext_index++] = vright;
        vertices[vertext_index++] = vtop;
        vertices[vertext_index++] = vleft;
        vertices[vertext_index++] = vbottom;
        vertices[vertext_index++] = vright;
        vertices[vertext_index++] = vbottom;

        /* 色をセット(フェードイン/フェードアウト) */
        // 色
        float life_percentage = (float)particles[i].m_frame_number / (float)particles[i].m_life_span;
        float alpha;
        if (life_percentage <= 0.5f)
          alpha = life_percentage * 0.2f;
        else
          alpha = 1.0f - (life_percentage - 0.5f) * 2.0f;

        for (int j = 0; j < 6; j++)
        {
          colors[color_index++] = 1.0f;
          colors[color_index++] = 1.0f;
          colors[color_index++] = 1.0f;
          colors[color_index++] = alpha;
        }

        /* テクスチャの設定 */

        // ポリゴン1
        coords[tex_coord_index++] = 0.0f;
        coords[tex_coord_index++] = 0.0f;
        coords[tex_coord_index++] = 1.0f;
        coords[tex_coord_index++] = 0.0f;
        coords[tex_coord_index++] = 0.0f;
        coords[tex_coord_index++] = 1.0f;

        // ポリゴン2
        coords[tex_coord_index++] = 1.0f;
        coords[tex_coord_index++] = 0.0f;
        coords[tex_coord_index++] = 0.0f;
        coords[tex_coord_index++] = 1.0f;
        coords[tex_coord_index++] = 1.0f;
        coords[tex_coord_index++] = 1.0f;

        active_particle_count++;
      }
    }

    // 一気に全部描画する
    FloatBuffer vertices_buffer = GraphicUtil.make_vertices_buffer (vertices);
    FloatBuffer color_buffer = GraphicUtil.make_colors_buffer (colors);
    FloatBuffer coord_buffer = GraphicUtil.make_tex_coords_buffer (coords);

    gl.glEnable (GL10.GL_TEXTURE_2D);
    gl.glBindTexture (GL10.GL_TEXTURE_2D, texture);
    gl.glVertexPointer (2, GL10.GL_FLOAT, 0, vertices_buffer);
    gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);
    gl.glColorPointer (4, GL10.GL_FLOAT, 0, color_buffer);
    gl.glEnableClientState (GL10.GL_COLOR_ARRAY);

    gl.glTexCoordPointer (2, GL10.GL_FLOAT, 0, coord_buffer);
    gl.glEnableClientState (GL10.GL_TEXTURE_COORD_ARRAY);

    gl.glDrawArrays (GL10.GL_TRIANGLES, 0, active_particle_count * 6);

    gl.glDisableClientState (GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glDisable (GL10.GL_TEXTURE_2D);  // テクスチャ無効化
  }

  public void update ()
  {
    Particle[] particles = m_particles;
    for (int i = 0; i < m_capacity; i++)
    {
      // アクティブなパーティクルを更新
      if (particles[i].m_is_active)
        particles[i].update ();
    }
  }
}
