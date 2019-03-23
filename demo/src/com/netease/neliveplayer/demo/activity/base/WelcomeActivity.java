package com.netease.neliveplayer.demo.activity.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.netease.neliveplayer.demo.R;

//设置启动欢迎页
public class WelcomeActivity extends Activity {
	@SuppressWarnings("unused")
	private ImageView mNEWelcomeImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		mNEWelcomeImage = (ImageView) findViewById(R.id.welcome_image);
		
		new Handler().postDelayed(r, 2000); //设置2秒钟后切换到下个Activity
	}
	
	Runnable r = new Runnable() {
		public void run() {
			Intent intent = new Intent();
			intent.setClass(WelcomeActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	};
}