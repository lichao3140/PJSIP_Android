package com.dpower.cintercom.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.util.DPLog;
import com.dpower.utils.MyFunction;

public class AutoListView extends ListView implements AbsListView.OnScrollListener {

    // 区分当前操作是刷新还是加载
    public static final int REFRESH = 0;
    public static final int LOAD = 1;

    // 区分PULL和RELEASE的距离的大小
    private static final int SPACE = 20;

    // 定义header的四种状态和当前状态
    private static final int NONE = 0;
    private static final int PULL = 1;
    private static final int RELEASE = 2;
    private static final int REFRESHING = 3;
    private int state;

    private LayoutInflater inflater;
    //	private View header;
    private View footer;
    private TextView tip;
    //	private TextView lastUpdate;
    private ImageView arrow;
    private ProgressBar refreshing;

    //	private TextView noData;
//    private TextView loadFull;
    //	private TextView more;
    private ProgressBar loading;

    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;

    private int startY;

    private int firstVisibleItem;
    private int scrollState;
//	private int headerContentInitialHeight;
//	private int headerContentHeight;

    // 只有在listview第一个item显示的时候（listview滑到了顶部）才进行下拉刷新， 否则此时的下拉只是滑动listview
    private boolean isRecorded;
    private boolean isLoading;// 判断是否正在加载
    private boolean loadEnable = true;// 开启或者关闭加载更多功能
    private boolean isLoadFull = false;
    private int pageSize = 10;

    public void setLoadFull(boolean isLoadFull){
        this.isLoadFull = isLoadFull;
    }

    private OnRefreshListener onRefreshListener;
    private OnLoadListener onLoadListener;

    public AutoListView(Context context) {
        super(context);
        initView(context);
    }

    public AutoListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AutoListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    // 下拉刷新监听
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    // 加载更多监听
    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.loadEnable = true;
        this.onLoadListener = onLoadListener;
    }

    public boolean isLoadEnable() {
        return loadEnable;
    }

    // 这里的开启或者关闭加载更多，并不支持动态调整
    public void setLoadEnable(boolean loadEnable) {
        this.loadEnable = loadEnable;
//		this.removeFooterView(footer);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    // 初始化组件
    private void initView(Context context) {

        // 设置箭头特效
        animation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(100);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(100);
        reverseAnimation.setFillAfter(true);

        inflater = LayoutInflater.from(context);
        footer = inflater.inflate(R.layout.listview_footer, null);
//        loadFull = (TextView) footer.findViewById(R.id.loadFull);
//		noData = (TextView) footer.findViewById(R.id.noData);
//		more = (TextView) footer.findViewById(R.id.more);
        loading = (ProgressBar) footer.findViewById(R.id.loading);

//		header = inflater.inflate(R.layout.pull_to_refresh, null);
//		arrow = (ImageView) header.findViewById(R.id.arrow);
//		tip = (TextView) header.findViewById(R.id.description);
//		lastUpdate = (TextView) header.findViewById(R.id.lastUpdate);
//		refreshing = (ProgressBar) header.findViewById(R.id.progress_bar);

//		// 为listview添加头部和尾部，并进行初始化
//		headerContentInitialHeight = header.getPaddingTop();
//		measureView(header);
//		headerContentHeight = header.getMeasuredHeight();
//		topPadding(-headerContentHeight);
//		this.addHeaderView(header);
        this.addFooterView(footer);
        this.setOnScrollListener(this);
    }

    public void onRefresh() {
        if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
    }

    public void onLoad() {
        if (onLoadListener != null) {
            onLoadListener.onLoad();
        }
    }

    public void onRefreshComplete(String updateTime) {
//		lastUpdate.setText(this.getContext().getString(R.string.lastUpdateTime,
//				lastUpdate));
        state = NONE;
        refreshHeaderViewByState();
    }

    // 用于下拉刷新结束后的回调
    public void onRefreshComplete() {
        String currentTime = MyFunction.GetCurrentTime();
        onRefreshComplete(currentTime);
    }

    // 用于加载更多结束后的回调
    public void onLoadComplete() {
        isLoading = false;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
        if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
            setView(2);
        }
        ifNeedLoad(view, scrollState);
    }

    // 根据listview滑动的状态判断是否需要加载更多
    private void ifNeedLoad(AbsListView view, int scrollState) {
//		if (!loadEnable) {
//			return;
//		}
        try {
            DPLog.println("AutoListView ifNeedLoad " + scrollState + ", " + isLoading + ", " + view.getLastVisiblePosition()
                    + ", " + view.getPositionForView(footer) + ", isLoadFull=" + isLoadFull);
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                    && !isLoading
                    && view.getLastVisiblePosition() == view
                    .getPositionForView(footer) && !isLoadFull) {
                isLoading = true;
                onLoad();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 监听触摸事件，解读手势
     */
    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (firstVisibleItem == 0) {
                    isRecorded = true;
                    startY = (int) ev.getY();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (state == PULL) {
                    state = NONE;
                    refreshHeaderViewByState();
                } else if (state == RELEASE) {
                    state = REFRESHING;
                    refreshHeaderViewByState();
                    onRefresh();
                }
                isRecorded = false;
                break;
            case MotionEvent.ACTION_MOVE:
                whenMove(ev);
                break;
        }
        return super.onTouchEvent(ev);
    }

    // 解读手势，刷新header状态
    private void whenMove(MotionEvent ev) {
        if (!isRecorded) {
            return;
        }
    }

    public void setView(int num){
        if(num == 0){
//            loadFull.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
        }else if(num == 1){ //加载完成
//            loadFull.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
        }else{ //正在加载
//            loadFull.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 这个方法是根据结果的大小来决定footer显示的。
     * <p>
     * 这里假定每次请求的条数为10。如果请求到了10条。则认为还有数据。如过结果不足10条，则认为数据已经全部加载，这时footer显示已经全部加载
     * </p>
     *
     * @param //resultSize
     */
//	public void setResultSize(int resultSize) {
//		if (resultSize == 0) {
//			isLoadFull = true;
////			more.setVisibility(View.GONE);
////			noData.setVisibility(View.VISIBLE);
//		} else if (resultSize > 0 && resultSize < pageSize) {
//			isLoadFull = true;
////			more.setVisibility(View.GONE);
////			noData.setVisibility(View.GONE);
//		} else if (resultSize == pageSize) {
//			isLoadFull = false;
////			more.setVisibility(View.VISIBLE);
////			noData.setVisibility(View.GONE);
//		}
//		DPLog.println("setResultSize isLoadFull=" + isLoadFull);
//
//	}

    // 根据当前状态，调整header
    private void refreshHeaderViewByState() {
        switch (state) {
            case NONE:
//			topPadding(-headerContentHeight);
                tip.setText(R.string.pull_to_refresh);
                refreshing.setVisibility(View.GONE);
                arrow.clearAnimation();
                arrow.setImageResource(R.drawable.arrow);
                break;
            case PULL:
                arrow.setVisibility(View.VISIBLE);
                tip.setVisibility(View.VISIBLE);
//			lastUpdate.setVisibility(View.VISIBLE);
                refreshing.setVisibility(View.GONE);
                tip.setText(R.string.pull_to_refresh);
                arrow.clearAnimation();
                arrow.setAnimation(reverseAnimation);
                break;
            case RELEASE:
                arrow.setVisibility(View.VISIBLE);
                tip.setVisibility(View.VISIBLE);
//			lastUpdate.setVisibility(View.VISIBLE);
                refreshing.setVisibility(View.GONE);
                tip.setText(R.string.pull_to_refresh);
                tip.setText(R.string.release_to_refresh);
                arrow.clearAnimation();
                arrow.setAnimation(animation);
                break;
            case REFRESHING:
//			topPadding(headerContentInitialHeight);
                refreshing.setVisibility(View.VISIBLE);
                arrow.clearAnimation();
                arrow.setVisibility(View.GONE);
                tip.setVisibility(View.GONE);
//			lastUpdate.setVisibility(View.GONE);
                break;
        }
    }

    // 用来计算header大小的。比较隐晦。
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /*
     * 定义下拉刷新接口
     */
    public interface OnRefreshListener {
        public void onRefresh();
    }

    /*
     * 定义加载更多接口
     */
    public interface OnLoadListener {
        public void onLoad();
    }
}
