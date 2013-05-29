package jp.k16.flyswattergame;

import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.lang.Math;
import javax.microedition.khronos.opengles.GL10;

import java.util.Hashtable;

import android.opengl.GLUtils;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;


public class GraphicUtil
{
  // GC対策 配列オブジェクトを保持する
  private static Hashtable<Integer, float[]> vertices_pool = new Hashtable<Integer, float[]>();
  private static Hashtable<Integer, float[]> colors_pool = new Hashtable<Integer, float[]>();
  private static Hashtable<Integer, float[]> coords_pool = new Hashtable<Integer, float[]>();

  public static float[] get_vertices (int n)
  {
    // すでに再利用可能な配列がハッシュテーブルに格納されているので、それを取り出して使う
    if (vertices_pool.containsKey (n))
      return vertices_pool.get (n);

    // 再利用可能な配列はまだないので、ここで生成してハッシュテーブルに追加する
    float[] vertices = new float[n];
    vertices_pool.put (n, vertices);

    // 今回生成したものを使う
    return vertices;
  }
  public static float[] get_colors (int n)
  {
    if (colors_pool.containsKey (n))
      return colors_pool.get (n);

    float[] colors = new float[n];
    colors_pool.put (n, colors);

    return colors;
  }
  public static float[] get_coords (int n)
  {
    if (coords_pool.containsKey (n))
      return coords_pool.get (n);

    float[] coords = new float[n];
    coords_pool.put (n, coords);

    return coords;
  }


  // バッファオブジェクトを保持する
  private static Hashtable<Integer, FloatBuffer> polygon_vertices_pool = new Hashtable<Integer, FloatBuffer>();
  private static Hashtable<Integer, FloatBuffer> polygon_colors_pool = new Hashtable<Integer, FloatBuffer>();
  private static Hashtable<Integer, FloatBuffer> tex_coords_pool = new Hashtable<Integer, FloatBuffer>();

  // 頂点座標用
  public static final FloatBuffer make_vertices_buffer (float[] arr)
  {
    FloatBuffer fb = null;
    if (polygon_vertices_pool.containsKey (arr.length))
    {
      fb = polygon_vertices_pool.get (arr.length);
      fb.clear ();
      fb.put (arr);
      fb.position (0);
      return fb;
    }

    fb = make_float_buffer (arr);
    polygon_vertices_pool.put (arr.length, fb);
    return fb;
  }
  // 色用
  public static final FloatBuffer make_colors_buffer (float[] arr)
  {
    FloatBuffer fb = null;
    if (polygon_colors_pool.containsKey (arr.length))
    {
      fb = polygon_colors_pool.get (arr.length);
      fb.clear ();
      fb.put (arr);
      fb.position (0);
      return fb;
    }

    fb = make_float_buffer (arr);
    polygon_colors_pool.put (arr.length, fb);
    return fb;
  }
  // テクスチャ用
  public static final FloatBuffer make_tex_coords_buffer (float[] arr)
  {
    FloatBuffer fb = null;
    if (tex_coords_pool.containsKey (arr.length))
    {
      fb = tex_coords_pool.get (arr.length);
      fb.clear ();
      fb.put (arr);
      fb.position (0);
      return fb;
    }

    fb = make_float_buffer (arr);
    tex_coords_pool.put (arr.length, fb);
    return fb;
  }

  public static final FloatBuffer make_float_buffer (float[] arr)
  {
    // sizeof (float) == 4byte
    ByteBuffer bb = ByteBuffer.allocateDirect (arr.length * 4);
    bb.order (ByteOrder.nativeOrder ());
    FloatBuffer fb = bb.asFloatBuffer ();
    fb.put (arr);
    fb.position (0);
    return fb;
  }

  // 四角形の描画
  public static final void draw_square (GL10 gl, float x, float y, float r, float g, float b, float a)
  {
    draw_rectangle (gl, x, y, 1.0f, 1.0f, r, g, b, a);
  }

  public static final void draw_square (GL10 gl, float r, float g, float b, float a)
  {
    draw_square (gl, 0.0f, 0.0f, r, g, b, a);
  }

  public static final void draw_square (GL10 gl)
  {
    draw_square (gl, 1.0f, 0.0f, 0.0f, 1.0f);
  }

  // 縦横のサイズを指定して四角形を描画
  public static final void draw_rectangle (GL10 gl,
                                           float x, float y,
                                           float width, float height,
                                           float r, float g, float b, float a)
  {
    /*
    float[] vertices = {
      -0.5f * width + x, -0.5f * height + y,
      0.5f * width + x, -0.5f * height + y,
      -0.5f * width + x,  0.5f * height + y,
      0.5f * width + x,  0.5f * height + y,
    };
     */
    // GC対策のため以下のように変更
    float[] vertices = get_vertices (8);
    vertices[0] = -0.5f * width + x;
    vertices[1] = -0.5f * height + y;
    vertices[2] = 0.5f * width + x;
    vertices[3] = -0.5f * height + y;
    vertices[4] = -0.5f * width + x;
    vertices[5] = 0.5f * height + y;
    vertices[6] = 0.5f * width + x;
    vertices[7] = 0.5f * height + y;

    /*
    float[] colors = {
      r, g, b, a,
      r, g, b, a,
      r, g, b, a,
      r, g, b, a,
    };
     */
    float[] colors = get_colors (16);
    for (int i = 0; i < 16; i++)
    {
      colors[i++] = r;
      colors[i++] = g;
      colors[i++] = b;
      colors[i] = a;
    }

    FloatBuffer polygon_vertices = make_vertices_buffer (vertices);
    FloatBuffer polygon_colors = make_colors_buffer (colors);

    gl.glVertexPointer (2, GL10.GL_FLOAT, 0, polygon_vertices);
    gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);
    gl.glColorPointer (4, GL10.GL_FLOAT, 0, polygon_colors);
    gl.glEnableClientState (GL10.GL_COLOR_ARRAY);

    gl.glDrawArrays (GL10.GL_TRIANGLE_STRIP, 0, 4);
  }

  /*
     devides => 円を何分割して描画するか（正何角形として描画するか）
     redius  => 円の半径
     RGBA    => 円の色
   */
  public static final void draw_circle (GL10 gl,
                                        float x, float y,
                                        int devies, float radius,
                                        float r, float g, float b, float a)
  {
    /*
    　 i番目のポリゴンは中央の座標(0.0f, 0.0f)、円周上のi番目の点の座標、円周上の(i + 1)番目の点の
       座標から構成されます。正n角形では、n個の頂点があります。i番目の頂点のx, y座標は下記のような
       式で求められます

       角度 = (360 / n ) * i, x = cos (角度) * 半径, y = sin (角度) * 半径
       これをラジアン値に変換すると、
       ラジアン値 = 角度(360度単位) / 180 * π

       よって今回のプログラムではこうなります
       ラジアン値 = (360 / n * i) / 180 * π = 2 / n * i * π

       float theta = 2.0f / (float)devies * (float)i * (float)Math.PI;
       float x = (float)Math.cos (theta) * radius;
       float y = (float)Math.sin (theta) * radius;
     */


    /* 中央に点を配置する正n角形を構成する三角形の数はn個となります。
       各ポリゴンは3つの頂点から構成されるため、頂点の数は「n * 3」個です
       頂点はx, yという2つの要素をデータとして持つので、頂点配列のサイズは「n * 3 * 2」個となります
     */
    float[] vertices = get_vertices (devies * 3 * 2);

    int vertex_id = 0;  // 頂点配列の要素の番号
    for (int i = 0; i < devies; i++)
    {
      // 円周上のi番目の頂点の角度(ラジアン)を計算
      float theta1 = 2.0f / (float)devies * (float)i * (float)Math.PI;

      // 円周上の(i + 1)番目の頂点の角度(ラジアン)を計算
      float theta2 = 2.0f / (float)devies * (float)(i + 1) * (float)Math.PI;

      // i番目の三角形の0番目の頂点情報をセットする
      vertices[vertex_id++] = x;
      vertices[vertex_id++] = y;

      // i番目の三角形の1番目の頂点情報をセットする(円周上のi番目の頂点)
      vertices[vertex_id++] = (float)Math.cos ((double)theta1) * radius + x;  // x座標
      vertices[vertex_id++] = (float)Math.sin ((double)theta1) * radius + y;  // y座標

      // i番目の三角形の2番目の頂点情報をセットする(円周上のi+1番目の頂点)
      vertices[vertex_id++] = (float)Math.cos ((double)theta2) * radius + x;  // x座標
      vertices[vertex_id++] = (float)Math.sin ((double)theta2) * radius + y;  // y座標

    }

    FloatBuffer polygon_vertices = make_colors_buffer (vertices);

    // ポリゴンの色を指定する
    gl.glColor4f (r, g, b, a);
    gl.glDisableClientState (GL10.GL_COLOR_ARRAY);

    gl.glVertexPointer (2, GL10.GL_FLOAT, 0, polygon_vertices);
    gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);

    gl.glDrawArrays (GL10.GL_TRIANGLES, 0, devies * 3);
  }


  private static final BitmapFactory.Options options = new BitmapFactory.Options ();
  static
  {
    // リソースの自動リサイズをしない
    options.inScaled = false;

    // 32bit画像として読み込む
    options.inPreferredConfig = Config.ARGB_8888;
  }
  public static final int load_texture (GL10 gl, Resources resources, int res_id)
  {
    int[] textures = new int[1];

    // Bitmapの生成
    Bitmap bmp = BitmapFactory.decodeResource (resources, res_id, options);
    if (bmp == null)
      return 0;

    // OpenGL用のテクスチャを生成する
    gl.glGenTextures (1, textures, 0);
    gl.glBindTexture (GL10.GL_TEXTURE_2D, textures[0]);
    GLUtils.texImage2D (GL10.GL_TEXTURE_2D, 0, bmp, 0);
    gl.glTexParameterf (GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
    gl.glTexParameterf (GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
    gl.glBindTexture (GL10.GL_TEXTURE_2D, 0);

    // OpenGLへの転送が完了したので、VMメモリ上に作成したBitmapを破棄する
    bmp.recycle ();

    // TextureManagerに登録する
    TextureManager.add_texture (res_id, textures[0]);

    return textures[0];
  }

  public static final void draw_texture (GL10 gl,
                                         float x, float y, float width, float height, int texture,
                                         float u, float v, float tex_w, float tex_h,
                                         float r, float g, float b, float a)
  {
    /*
    float[] vertices = {
      -0.5f * width + x, -0.5f * height + y,
      0.5f * width + x, -0.5f * height + y,
      -0.5f * width + x,  0.5f * height + y,
      0.5f * width + x,  0.5f * height + y,
    };
     */

    // GC対策のため以下のように変更
    float[] vertices = get_vertices (8);
    vertices[0] = -0.5f * width + x;
    vertices[1] = -0.5f * height + y;
    vertices[2] = 0.5f * width + x;
    vertices[3] = -0.5f * height + y;
    vertices[4] = -0.5f * width + x;
    vertices[5] = 0.5f * height + y;
    vertices[6] = 0.5f * width + x;
    vertices[7] = 0.5f * height + y;

    /*
    float[] colors = {
      r, g, b, a,
      r, g, b, a,
      r, g, b, a,
      r, g, b, a,
    };
     */
    float[] colors = get_colors (16);
    for (int i = 0; i < 16; i++)
    {
      colors[i++] = r;
      colors[i++] = g;
      colors[i++] = b;
      colors[i] = a;
    }

    /*
    float[] coords = {
      u        , v + tex_h,
      u + tex_w, v + tex_h,
      u        , v,
      u + tex_w, v,
    };
     */
    float[] coords = get_coords (8);
    coords[0] = u;
    coords[1] = v + tex_h;
    coords[2] = u + tex_w;
    coords[3] = v + tex_h;
    coords[4] = u;
    coords[5] = v;
    coords[6] = u + tex_w;
    coords[7] = v;


    FloatBuffer polygon_vertices = make_vertices_buffer (vertices);
    FloatBuffer polygon_colors = make_colors_buffer (colors);
    FloatBuffer tex_coords = make_tex_coords_buffer (coords);

    gl.glEnable (GL10.GL_TEXTURE_2D);  // テクスチャ有効化

    // テクスチャオブジェクトの指定
    gl.glBindTexture (GL10.GL_TEXTURE_2D, texture);

    gl.glVertexPointer (2, GL10.GL_FLOAT, 0, polygon_vertices);
    gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);
    gl.glColorPointer (4, GL10.GL_FLOAT, 0, polygon_colors);
    gl.glEnableClientState (GL10.GL_COLOR_ARRAY);
    gl.glTexCoordPointer (2, GL10.GL_FLOAT, 0, tex_coords);
    gl.glEnableClientState (GL10.GL_TEXTURE_COORD_ARRAY);

    gl.glDrawArrays (GL10.GL_TRIANGLE_STRIP, 0, 4);

    gl.glDisableClientState (GL10.GL_TEXTURE_COORD_ARRAY);
    gl.glDisable (GL10.GL_TEXTURE_2D);  // テクスチャ無効化
  }

  public static final void draw_texture (GL10 gl,
                                         float x, float y, float width, float height, int texture,
                                         float r, float g, float b, float a)
  {
    draw_texture (gl, x, y, width, height, texture,
                  0.0f, 0.0f, 1.0f, 1.0f,
                  r, g, b, a);
  }

  public static final void draw_number (GL10 gl,
                                        float x, float y, float w, float h,
                                        int texture, int number,
                                        float r, float g, float b, float a)
  {
    float u = (float)(number % 4) * 0.25f;
    float v = (float)(number / 4) * 0.25f;

    draw_texture (gl, x, y, w, h, texture, u, v, 0.25f, 0.25f, r, g, b, a);
  }

  public static final void draw_number (GL10 gl,
                                        float x, float y, float width, float height,
                                        int texture, int number, int figures,
                                        float r, float g, float b, float a)
  {
    float total_width = width * (float)figures; // n文字分の横幅
    float right_x = x + (total_width * 0.5f);   // 右端のx座標
    float fig1x = right_x - width * 0.5f;       // 1番右の桁(1桁目)の中心のx座標

    for (int i = 0; i < figures; i++)
    {
      float fignx = fig1x - (float)i * width;     // n+1桁目の中心のx座標

      int number2draw = number % 10;
      number = number / 10;

      draw_number (gl, fignx, y, width, height, texture, number2draw, 1.0f, 1.0f, 1.0f, 1.0f);
    }
  }
}
