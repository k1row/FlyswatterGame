package jp.k16.flyswattergame;


import java.util.Hashtable;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;


public class TextureManager
{
  // テクスチャを保持する
  private static Map<Integer, Integer> m_texures = new Hashtable<Integer, Integer>();

  // ロードしたテクスチャを追加する
  public static final void add_texture (int res_id, int tex_id)
  {
    m_texures.put (res_id, tex_id);
  }

  // テクスチャを削除する
  public static final void delete_texture (GL10 gl, int res_id)
  {
    if (m_texures.containsKey (res_id))
    {
      int[] tex_id = new int[1];
      tex_id[0] = m_texures.get (res_id);
      gl.glDeleteTextures (1, tex_id, 0);
      m_texures.remove (res_id);
    }
  }

  // すべてのテクスチャを削除する
  public static final void delete_all (GL10 gl)
  {
    List<Integer> keys = new ArrayList<Integer>(m_texures.keySet ());
    for (Integer key : keys)
      delete_texture (gl, key);
  }
}
