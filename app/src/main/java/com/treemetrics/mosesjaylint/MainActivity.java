package com.treemetrics.mosesjaylint;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.v("tag", "log");
    Log.d("tag", "log");
    Log.i("tag", "log");
    Log.w("tag", "log");
    Log.e("tag", "log");
    Log.wtf("tag", "log");

    method();
  }

  private void method() {

  }

}
