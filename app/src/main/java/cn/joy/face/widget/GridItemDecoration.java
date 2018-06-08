package cn.joy.face.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * User: Joy
 * Date: 2016/10/14
 * Time: 15:51
 */

public class GridItemDecoration extends RecyclerView.ItemDecoration {

	private int spanCount;
	private int spacing;
	private int horizontalSpacing, verticalSpacing;
	private boolean includeEdge;
	private int top, left, right, bottom;

	public GridItemDecoration(int spanCount, int spacing) {
		this(spanCount, spacing, false);
	}

	public GridItemDecoration(int spanCount, int horizontalSpacing, int verticalSpacing) {
		this(spanCount, horizontalSpacing, verticalSpacing, false);
	}

	public GridItemDecoration(int spanCount, int spacing, boolean includeEdge) {
		this.spanCount = spanCount;
		this.horizontalSpacing = spacing;
		this.verticalSpacing = spacing;
		this.includeEdge = includeEdge;
	}

	public GridItemDecoration(int spanCount, int horizontalSpacing, int verticalSpacing, boolean includeEdge) {
		this.spanCount = spanCount;
		this.horizontalSpacing = horizontalSpacing;
		this.verticalSpacing = verticalSpacing;
		this.includeEdge = includeEdge;
	}

	public GridItemDecoration(int spanCount, int horizontalSpacing, int verticalSpacing, int top, int left, int right, int bottom) {
		this.spanCount = spanCount;
		this.horizontalSpacing = horizontalSpacing;
		this.verticalSpacing = verticalSpacing;
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
	}

//	@Override
//	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//		int position = parent.getChildAdapterPosition(view); // item position
//		int column = position % spanCount; // item column
//
//		outRect.left = column == 0 ? left : horizontalSpacing;
//		outRect.right = column == spanCount - 1 ? right : 0;
//		outRect.top = position < spanCount ? top : verticalSpacing;
//		outRect.bottom = 0;
//		if (parent.getAdapter() != null) {
//			int count = 0;
//			if (parent.getAdapter() instanceof HeaderAdapter) {
//				count = ((HeaderAdapter) parent.getAdapter()).getRealItemCount();
//			} else {
//				count = parent.getAdapter().getItemCount();
//			}
//			int maxRow = count % spanCount > 0 ? count / spanCount + 1 : count / spanCount;
//			int currRow = (position + 1) % spanCount > 0 ? (position + 1) / spanCount + 1 : (position + 1) / spanCount;
//			if (currRow == maxRow) {
//				outRect.bottom = bottom;
//			}
//		}
//		Logs.d(position + ":" + outRect);
//	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		int position = parent.getChildAdapterPosition(view); // item position
		int column = position % spanCount; // item column

		if (includeEdge) {
			outRect.left = horizontalSpacing - column * horizontalSpacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
			outRect.right = (column + 1) * horizontalSpacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

			if (position < spanCount) { // top edge
				outRect.top = verticalSpacing;
			}
			outRect.bottom = verticalSpacing; // item bottom
		} else {
			outRect.left = column * horizontalSpacing / spanCount; // column * ((1f / spanCount) * spacing)
			outRect.right = horizontalSpacing - (column + 1) * horizontalSpacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
			if (position >= spanCount) {
				outRect.top = verticalSpacing; // item top
			}
		}
	}
}
