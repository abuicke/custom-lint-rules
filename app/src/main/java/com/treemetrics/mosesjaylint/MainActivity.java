package com.treemetrics.mosesjaylint;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Dummy dummy = new Dummy();
    dummy.getHarvestOp();
  }

}
