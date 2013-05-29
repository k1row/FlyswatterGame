package jp.k16.flyswattergame;

import java.util.Random;
import javax.microedition.khronos.opengles.GL10;


public class Global
{
  // GLコンテキストを保持する変数
  public static GL10 gl;

  public static Random rand = new Random (System.currentTimeMillis ());

  // MainActivity
  public static MainActivity main_activity;

  // デバックモードかどうか
  public static boolean is_debug;
}
