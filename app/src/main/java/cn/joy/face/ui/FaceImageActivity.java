package cn.joy.face.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.joy.face.ParentActivity;
import cn.joy.face.R;
import cn.joy.face.utils.FaceManager;
import cn.joy.face.utils.image.ImageLoader;
import cn.joy.face.widget.GridItemDecoration;
import cn.joy.face.widget.adapter.AdapterPlus;
import cn.joy.face.widget.adapter.ViewHolderPlus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Author: Joy
 * Date:   2018/6/8
 */

public class FaceImageActivity extends ParentActivity {

	@BindView(R.id.recycler)
	RecyclerView mRecycler;

	ImageAdapter mAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 全屏|保持屏幕常亮
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_face_image);
		ButterKnife.bind(this);
		mRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
		mRecycler.addItemDecoration(new GridItemDecoration(3, getContext().getResources().getDimensionPixelOffset(R.dimen.size_10), true));
		mRecycler.setAdapter(mAdapter = new ImageAdapter(getContext()));

		Observable.just(new File(FaceManager.getInstance().getFaceSavePath())).map(f -> {
			List<String> list = new ArrayList<>();
			if (f.exists()) {
				File[] files = f.listFiles();
				if (files != null && files.length > 0) {
					for (File ff : files) {
						list.add(ff.getAbsolutePath());
					}
				}
			}
			return list;
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(strings -> {
			mAdapter.clear();
			mAdapter.insertRange(strings);
		});
	}

	@OnClick(R.id.btnCancel)
	public void onClick() {
		finish();
	}

	class ImageAdapter extends AdapterPlus<String> {

		ImageAdapter(Context context) {
			super(context);
		}

		@Override
		public ViewHolderPlus<String> onCreateViewHolder(ViewGroup parent, int viewType, LayoutInflater inflater) {
			return new ImageHolder(createView(R.layout.item_face_image, parent));
		}

		class ImageHolder extends ViewHolderPlus<String> {

			@BindView(R.id.image)
			ImageView image;

			ImageHolder(View itemView) {
				super(itemView);
				ButterKnife.bind(this, itemView);
			}

			@Override
			public void onBinding(int position, String s) {
				Glide.with(getContext()).load(new File(s)).asBitmap().transform(new CenterCrop(getContext())).into(image);
			}

			@OnClick(R.id.image)
			public void onClick() {
				ImageLoader.browser(FaceImageActivity.this, getItemObject());
			}
		}
	}
}
