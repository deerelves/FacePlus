package cn.joy.plus.tools.image.selector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cn.joy.plus.R;

import static android.media.CamcorderProfile.get;

public class MultiImageSelectorFragment extends Fragment implements OnClickListener {
	private static final String TAG = "MultiImageSelectorF";

	private static final int REQUEST_CAMERA = 1;
	/**
	 * 不同loader定义
	 */
	private static final int LOADER_ALL = 0;
	private static final int LOADER_CATEGORY = 1;
	ImageSelectorCallBack callBack;
	TextView mTitle;
	TextView mBtnDone;
	/**
	 * 列间距
	 */
	private int columnsWidth;
	private int imageSelectedMode;
	private int imageSelectedCount;
	private String imageSelectedPath;
	private boolean isShowCamera;
	/**
	 * 是否为指定目录
	 */
	private boolean isAssignPath;
	private String cameraSavePathBase;
	private String cameraSavePath;
	private Toolbar mToolbar;
	private RecyclerView mRecycler;
	private ImagesAdapter mAdapter;

	private LinkedList<Directory> mImageDirectories = new LinkedList<>();
	private List<Image> mSelectedImages = new ArrayList<>();
	private List<Image> mImages = new ArrayList<>();

	//  顶部目录选择项
	private View mDirectory;

	private int currentDirectory = 0;

	private LoaderCallbacks<Cursor> mLoaderCallback = new LoaderCallbacks<Cursor>() {

		private final String[] IMAGE_PROJECTION = new String[]{
				Media.DATA, Media.DISPLAY_NAME, Media.DATE_ADDED, Media._ID
		};

		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
				case LOADER_ALL:
					return new CursorLoader(getActivity(), Media.EXTERNAL_CONTENT_URI, this.IMAGE_PROJECTION, null, null, this.IMAGE_PROJECTION[2] + " DESC");
				case LOADER_CATEGORY:
					return new CursorLoader(getActivity(), Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, IMAGE_PROJECTION[0] + " like '%" + args.getString(ImageSelectorConstants.EXTRA_IMAGE_SELECT_PATH) + "%'", null, IMAGE_PROJECTION[2] + " DESC");
				default:
					return null;
			}
		}


		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			mImages.clear();
			mImageDirectories.clear();

			if (isShowCamera) {
				Image camera = new Image();
				camera.setIsCamera(true);
				mImages.add(camera);
			}

			onGetData(data, mImages);
			mAdapter.changeData(mImages);

			Directory directory = new Directory();
			directory.setName(getString(R.string.image_selector_all));
			directory.setPath("none");
			directory.setImages(mImages);
			mImageDirectories.addFirst(directory);
		}

		void onGetData(Cursor data, List<Image> imgSaver) {
			if (data == null || data.getCount() == 0)
				return;
			data.moveToFirst();
			do {
				//  获取图片的ID
				int imgId = data.getInt(data.getColumnIndexOrThrow(this.IMAGE_PROJECTION[3]));
				String path = data.getString(data.getColumnIndexOrThrow(this.IMAGE_PROJECTION[0]));
				if (!path.toLowerCase().endsWith(".jpeg") && !path.toLowerCase().endsWith(".jpg") && !path.toLowerCase().endsWith(".png")) {
					continue;
				}
				//  如果是指定目录，但是当前image的目录不在指定目录下，则不做任何操作
				if (isAssignPath && !path.startsWith(imageSelectedPath)) {
					continue;
				}
				Image image = new Image();
				image.setPath(path);
				image.setName(data.getString(data.getColumnIndexOrThrow(this.IMAGE_PROJECTION[1])));
				image.setUri(Uri.parse(Media.EXTERNAL_CONTENT_URI.toString() + "/" + imgId));
				image.setId(imgId);
				image.setDate(data.getLong(data.getColumnIndexOrThrow(this.IMAGE_PROJECTION[2])));
				boolean exist = false;
				File f = new File(image.getPath());
				if (f.exists()) {

					File parent = f.getParentFile();
					for (Directory directory : mImageDirectories) {
						if (directory.getPath().equals(parent.getAbsolutePath())) {
							directory.getImages().add(image);
							exist = true;
						}
					}

					if (!exist) {
						Directory directory = new Directory();
						directory.setName(parent.getName());
						directory.setPath(parent.getAbsolutePath());
						directory.setImages(new ArrayList<Image>());
						directory.getImages().add(image);
						mImageDirectories.add(directory);
					}
					imgSaver.add(image);
				}
			} while (data.moveToNext());
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {

		}
	};

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof ImageSelectorCallBack) {
			this.callBack = (ImageSelectorCallBack) activity;
		}
	}

	@Nullable
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.image_selector_fragment, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.imageSelectedMode = this.getArguments().getInt(ImageSelectorConstants.EXTRA_IMAGE_SELECT_MODE, ImageSelectorConstants.IMAGE_SELECT_MODE_MULTI);
		this.imageSelectedCount = this.getArguments()
				.getInt(ImageSelectorConstants.EXTRA_IMAGE_SELECT_COUNT, ImageSelectorConstants.IMAGE_SELECT_COUNT_DEFAULT);
		this.imageSelectedPath = this.getArguments().getString(ImageSelectorConstants.EXTRA_IMAGE_SELECT_PATH);
		this.isAssignPath = !TextUtils.isEmpty(imageSelectedPath);
		this.isShowCamera = this.getArguments().getBoolean(ImageSelectorConstants.EXTRA_IMAGE_SELECT_SHOW_CAMERA, true);
		this.cameraSavePathBase = this.getArguments().getString(ImageSelectorConstants.EXTRA_IMAGE_SELECT_CAMERA_SAVE_PATH);
		if (TextUtils.isEmpty(this.cameraSavePathBase)) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				this.cameraSavePathBase = Environment.getExternalStorageDirectory().getAbsolutePath();
			} else {
				this.cameraSavePathBase = Environment.getDataDirectory().getAbsolutePath();
			}
		}

		this.mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
		mToolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (callBack == null)
					return;
				callBack.onCancel();
			}
		});
		this.mBtnDone = (TextView) view.findViewById(R.id.image_selector_btn_ok);
		this.mTitle = (TextView) view.findViewById(R.id.image_selector_title);
		this.mDirectory = view.findViewById(R.id.image_selector_directory);
		this.mRecycler = (RecyclerView) view.findViewById(R.id.image_selector_images);
		this.mRecycler.setLayoutManager(new GridLayoutManager(this.getActivity(), 3));
		this.mRecycler.setHasFixedSize(true);
		this.mRecycler.setAdapter(this.mAdapter = new ImagesAdapter(this.getActivity(), new ArrayList<Image>()));
		this.mTitle.setOnClickListener(this);
		this.mBtnDone.setOnClickListener(this);
		this.mBtnDone.setVisibility(this.isSingleMode() ? View.GONE : View.VISIBLE);
		this.mDirectory.setVisibility(this.isAssignPath ? View.GONE : View.VISIBLE);
	}

	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mRecycler.post(new Runnable() {
			@Override
			public void run() {
				columnsWidth = getActivity().getResources().getDimensionPixelOffset(R.dimen.size_5);
				mAdapter.setItemSide((int) (((float) mRecycler.getWidth() - (float) columnsWidth * 2) / 3));
				getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
				//				Bundle bundle = new Bundle();
				//				bundle.putString(ImageSelectorConstants.EXTRA_IMAGE_SELECT_PATH, imageSelectedPath);
				//				getActivity().getSupportLoaderManager()
				//						.initLoader(isAssignPath ? LOADER_ALL : LOADER_CATEGORY, isAssignPath ? null : bundle, mLoaderCallback);
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CAMERA && this.callBack != null) {
				//				/** 如果当前图片的度数不为0,则纠正图片的度数 */
				//				if (ImageTools.getExifOrientation(cameraSavePath) != 0) {
				//					ImageTools.rotateImage(cameraSavePath);
				//				}
				this.callBack.onImagesSelected(ImageSelectSource.Camera, new ArrayList<>(Collections.singletonList(cameraSavePath)));
			}
		} else if (this.callBack != null) {
			this.callBack.onCancel();
		}
	}

	/**
	 * 显示目录
	 */
	void showDirectories() {
		if (mImageDirectories.size() <= 1) {
			return;
		}
		final ListPopupWindow popupWindow = new ListPopupWindow(this.getActivity());
		popupWindow.setBackgroundDrawable(new ColorDrawable(-1));
		popupWindow.setAdapter(new DirectoryAdapter(this.getActivity(), this.mImageDirectories));
		popupWindow.setWidth(-1);
		int itemHeight = this.getActivity().getResources().getDimensionPixelOffset(R.dimen.size_60);
		popupWindow.setHeight(this.mImageDirectories.size() >= 5 ? itemHeight * 5 : itemHeight * this.mImageDirectories.size() + getActivity().getResources()
				.getDimensionPixelOffset(R.dimen.size_10));
		popupWindow.setAnchorView(this.mToolbar);
		popupWindow.setModal(true);
		popupWindow.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mTitle.setText(mImageDirectories.get(position).getName());
				mAdapter.changeData(mImageDirectories.get(position).getImages());
				currentDirectory = position;
				popupWindow.dismiss();
			}
		});
		popupWindow.show();
		popupWindow.getListView().setDividerHeight(getActivity().getResources().getDimensionPixelOffset(R.dimen.size_10));
		popupWindow.getListView().setDivider(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		popupWindow.setSelection(this.currentDirectory);
	}

	boolean isSingleMode() {
		return this.imageSelectedMode == 1;
	}

	public void onClick(View v) {
		if (v.getId() == R.id.image_selector_title) {
			this.showDirectories();
		} else if (v.getId() == R.id.image_selector_btn_ok) {
			if (this.callBack == null)
				return;
			ArrayList<String> paths = new ArrayList<>();
			for (Image image : mSelectedImages) {
				paths.add(image.getPath());
			}
			this.callBack.onImagesSelected(ImageSelectSource.Album, paths);
		}
	}


	public enum ImageSelectSource {
		Album(0x1),
		Camera(0x2);
		int type;

		ImageSelectSource(int type) {
			this.type = type;
		}

		public int getTypeId() {
			return type;
		}
	}

	public interface ImageSelectorCallBack {
		void onImagesSelected(ImageSelectSource selectSource, ArrayList<String> list);

		void onCancel();
	}

	class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageHolder> {
		static final int TYPE_IMAGE = 1;
		static final int TYPE_CAMERA = 2;
		private Context context;
		private List<Image> images;

		private int itemSide = 0;

		ImagesAdapter(Context context, List<Image> images) {
			this.context = context;
			this.images = images;
		}

		public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			switch (viewType) {
				case TYPE_IMAGE:
					return new ImageHolder(LayoutInflater.from(context).inflate(R.layout.image_selector_item_image, parent, false));
				case TYPE_CAMERA:
					return new CameraHolder(LayoutInflater.from(context).inflate(R.layout.image_selector_item_camera, parent, false));
			}
			return null;
		}

		public int getItemCount() {
			return images.size();
		}

		public void onBindViewHolder(final ImageHolder holder, final int position) {

			if (itemSide != 0) {
				if (holder.itemView.getLayoutParams() == null) {
					GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(itemSide, itemSide);
					holder.itemView.setLayoutParams(params);
				} else {
					holder.itemView.getLayoutParams().width = itemSide;
					holder.itemView.getLayoutParams().height = itemSide;
				}
			}
			// 调用系统相机
			if (holder.isCamera) {
				holder.itemView.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						cameraSavePath = cameraSavePathBase + "/" + System.currentTimeMillis() + ".jpg";
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cameraSavePath)));
						intent.putExtra("return-data", true);
						startActivityForResult(intent, REQUEST_CAMERA);
					}
				});
			} else {
				final Image image = images.get(position);
				Glide.with(context)
						.fromUri()
						.asBitmap()
						.load(image.getUri())
						.placeholder(new ColorDrawable(Color.parseColor("#555555")))
						.error(R.drawable.image_selector_load_error)
						.centerCrop()
						.into(holder.img);

				for (int i = 0; i < mSelectedImages.size(); i++) {
					if (mSelectedImages.get(i).equals(image)){
						image.setSelected(mSelectedImages.get(i).isSelected());
					}
				}
				final boolean isSelected = image.isSelected();

				holder.itemView.setSelected(isSelected);
				holder.backView.setVisibility(isSelected ? View.VISIBLE : View.GONE);
				if (isSingleMode()) {
					holder.backView.setVisibility(View.GONE);
					holder.checkBox.setVisibility(View.GONE);
				}

				holder.img.setOnClickListener(new OnClickListener() {
					@SuppressLint("StringFormatMatches")
					public void onClick(View v) {
						if (isSingleMode() && callBack != null) {
							callBack.onImagesSelected(ImageSelectSource.Album, new ArrayList<>(Collections.singletonList(image.getPath())));
							return;
						}
						if (!image.isSelected() && mSelectedImages.size() >= imageSelectedCount) {
							Toast.makeText(context, context.getString(R.string.image_selector_msg_amount_limit, imageSelectedCount), Toast.LENGTH_SHORT).show();
							return;
						}
						image.setSelected(!isSelected);
						mAdapter.notifyItemChanged(position);
						if (image.isSelected()) {
							mSelectedImages.add(image);
						} else {
							mSelectedImages.remove(image);
						}

						if (mSelectedImages.size() > 0) {
							mBtnDone.setEnabled(true);
							mBtnDone.setText(context.getString(R.string.image_selector_done_format, mSelectedImages.size(), imageSelectedCount));
						} else {
							mBtnDone.setEnabled(false);
							mBtnDone.setText(R.string.image_selector_done);
						}
					}
				});
			}
		}

		public int getItemViewType(int position) {
			return (images.get(position)).isCamera() ? TYPE_CAMERA : TYPE_IMAGE;
		}

		public void changeData(List<Image> images) {
			clear();
			this.images.addAll(images);
			notifyDataSetChanged();
//			notifyItemRangeChanged(0, images.size());
		}

		public void insert(Image image) {
			this.images.add(image);
			this.notifyItemInserted(this.images.size() - 1);
		}

		public void clear() {
			this.images.clear();
		}

		public void setItemSide(int itemSide) {
			this.itemSide = itemSide;
		}

		class CameraHolder extends ImageHolder {
			public CameraHolder(View itemView) {
				super(itemView);
				this.isCamera = true;
			}
		}

		class ImageHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
			boolean isCamera = false;
			ImageView img;
			View checkBox;
			View backView;

			public ImageHolder(View itemView) {
				super(itemView);
				this.img = (ImageView) itemView.findViewById(R.id.image_selector_image);
				this.checkBox = itemView.findViewById(R.id.image_selector_checkbox);
				this.backView = itemView.findViewById(R.id.image_selector_bg);
			}
		}
	}

	class DirectoryAdapter extends BaseAdapter {
		private List<Directory> directories;
		private Context context;

		DirectoryAdapter(Context context, List<Directory> directories) {
			this.context = context;
			this.directories = directories;
		}

		public int getCount() {
			return this.directories.size();
		}

		public Object getItem(int position) {
			return this.directories.get(position);
		}

		public long getItemId(int position) {
			return (long) position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mHolder;
			if (convertView == null) {
				convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.image_selector_directory_item, parent, false);
				mHolder = new ViewHolder(convertView);
				convertView.setTag(mHolder);
			} else {
				mHolder = (ViewHolder) convertView.getTag();
			}

			Directory directory = (Directory) getItem(position);
			mHolder.txtDirect.setText(position == 0 ? directory.getName() : directory.getName() + String.format("(%d)", directory.getImages().size()));
			if (directory.getImages().size() > 1 && isShowCamera) {
				Glide.with(context)
						.fromUri()
						.asBitmap()
						.load(directory.getImages().get(1).getUri())
						.error(R.drawable.image_selector_load_error)
						.into(mHolder.imgThumb);
			} else if (directory.getImages().size() > 0 && !isShowCamera) {
				Glide.with(context)
						.fromUri()
						.asBitmap()
						.load(directory.getImages().get(0).getUri())
						.error(R.drawable.image_selector_load_error)
						.into(mHolder.imgThumb);
			}
			return convertView;
		}

		public Context getContext() {
			return this.context;
		}

		class ViewHolder {
			ImageView imgThumb;
			TextView txtDirect;

			ViewHolder(View itemView) {
				this.imgThumb = (ImageView) itemView.findViewById(R.id.image_selector_dir_item_thumb);
				this.txtDirect = (TextView) itemView.findViewById(R.id.image_selector_dir_item_dir);
			}
		}
	}
}

