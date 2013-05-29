package jp.k16.flyswattergame;

import android.opengl.GLSurfaceView;

import android.view.MotionEvent;
import android.view.SurfaceHolder;

import android.content.Context;
import android.util.Log;


public class MyGLSurfaceView extends GLSurfaceView
{
  // 画面サイズ
  private float m_width;
  private float m_height;

  // MyRendererを保持する
  private MyRenderer m_renderer;

  public MyGLSurfaceView (Context context)
  {
    super (context);

    // タッチイベントを取得できるようにする
    setFocusable (true);
  }

  @Override
    public boolean onTouchEvent (MotionEvent event)
    {
      // タッチされた後、スクリーンの座標をOpenGLの座標に変換する
      // OpenGL上のX = (画面上のX / width)  * 2.0f  - 1.0f;
      // OpenGL上のY = (画面上のY / height) * -3.0f + 1.5f;

      float x = (event.getX () / (float)m_width) * 2.0f - 1.0f;
      float y = (event.getY () / (float)m_height) * -3.0f + 1.5f;

      Log.i ("FlyswatterGame", String.format ("touched ! x = %f, y=%f", x, y));

      // レンダラーに通知して当たり判定をしてもらう
      m_renderer.touched (x, y);

      return false;
    }

  @Override
    public void surfaceChanged (SurfaceHolder holder, int format, int w, int h)
    {
      super.surfaceChanged (holder, format, w, h);
      this.m_width = w;
      this.m_height = h;
    }

  @Override
    public void setRenderer (Renderer renderer)
    {
      super.setRenderer (renderer);
      this.m_renderer = (MyRenderer)renderer;
    }
}
