package com.cl.wanandroid.base.list

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cl.wanandroid.R
import com.cl.wanandroid.base.list.base.BaseRecyclerViewModel
import com.cl.wanandroid.base.list.base.BaseViewData
import com.cl.wanandroid.base.list.loadmore.LoadMoreAdapter
import com.cl.wanandroid.base.list.loadmore.LoadMoreRecyclerView
import com.cl.wanandroid.base.list.pullrefresh.PullRefreshLayout
import com.cl.wanandroid.bean.LoadError
import com.cl.wanandroid.bean.excption.GlobalException
import com.cl.wanandroid.constant.LoadMoreState
import com.cl.wanandroid.databinding.ViewXRecyclerBinding
import com.cl.wanandroid.item.LoadMoreViewData
import com.cl.wanandroid.util.isNetworkConnect
import com.cl.wanandroid.util.toNetworkSetting

class XRecyclerView constructor(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    private val viewBinding = ViewXRecyclerBinding.inflate(LayoutInflater.from(context), this, true)
    private var activity: AppCompatActivity = context as AppCompatActivity
    private lateinit var config: Config
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentPageState = PageState.NORMAL
    private val retryOnClickListener by lazy {
        OnClickListener {
            loadData(isLoadMore = false, isReLoad = true, showLoading = true)
        }
    }
    private val showLoadingRunnable by lazy {
        Runnable {
            viewBinding.refreshLayout.isVisible = false
            viewBinding.loadingView.isVisible = true
            viewBinding.errorView.isVisible = false
        }
    }
    private var interceptTouchEvent = false
    private val resumeTouchRunnable by lazy {
        Runnable {
            interceptTouchEvent = false
        }
    }
    private val networkCallback = NetworkCallback()

    companion object {
        const val DELAY_SHOW_LOADING = 500L
        const val DELAY_RESUME_TOUCH_EVENT = 500L
    }

    fun init(config: Config) {
        config.check(context)
        this.config = config
        initView()
        initData()
    }

    private fun initView() {
        // ??????RecyclerView
        viewBinding.loadMoreRecyclerView.run {
            // ?????????
            isVerticalScrollBarEnabled = config.getShowScrollBar()
            // LayoutManager
            layoutManager = config.getLayoutManager()
            // ItemDecoration
            config.getItemDecoration()?.let {
                addItemDecoration(it)
            }
            // ItemAnimator
            config.getItemAnimator()?.let {
                itemAnimator = it
            }
            // Adapter
            adapter = config.getAdapter()
        }
    }

    private fun initData() {
        // ????????????(?????????)??????????????????
        config.getViewModel().firstViewDataLiveData.observe(activity) { viewData ->
            // ?????????????????????????????????????????????
            interceptTouchEventTemporarily()
            viewBinding.loadMoreRecyclerView.scrollToPosition(0)
            // ????????????????????????
            viewBinding.refreshLayout.run {
                if (isRefreshing) {
                    refreshComplete()
                }
            }
            viewBinding.loadMoreRecyclerView.resetLoadMoreListener()
            if (viewData === LoadError) {
                // ????????????????????????????????????????????????????????????
                viewBinding.loadMoreRecyclerView.setCanLoadMore(false)
                if (isNetworkConnect()) {
                    showPageState(PageState.LOAD_ERROR)
                } else {
                    showPageState(PageState.NO_NETWORK)
                }
            } else if (viewData.isEmpty()) {
                // ?????????????????????????????????
                viewBinding.loadMoreRecyclerView.setCanLoadMore(false)
                showPageState(PageState.EMPTY)
            } else {
                // ?????????????????????????????????Adapter
                config.getAdapter().setViewData(viewData)
                if (config.getPullUploadMoreEnable()) {
                    viewBinding.loadMoreRecyclerView.setCanLoadMore(true)
                    config.getAdapter().setLoadMoreState(LoadMoreState.LOADING)
                } else {
                    viewBinding.loadMoreRecyclerView.setCanLoadMore(false)
                    config.getAdapter().setLoadMoreState(LoadMoreState.GONE)
                }
                showPageState(PageState.NORMAL)
            }
        }

        // ??????????????????
        viewBinding.refreshLayout.isEnabled = config.getPullRefreshEnable()
        if (config.getPullRefreshEnable()) {
            viewBinding.refreshLayout.setOnPullRefreshListener(object :
                PullRefreshLayout.OnPullRefreshListener {
                override fun onRefreshBegin() {
                    loadData(isLoadMore = false, isReLoad = false)
                }
            })
        }

        if (config.getPullUploadMoreEnable()) {
            // ?????????????????????????????????????????????
            interceptTouchEventTemporarily()
            // ??????????????????????????????
            config.getViewModel().moreViewDataLiveData.observe(activity) { viewData ->
                if (viewData === LoadError) {
                    // ????????????????????????????????????????????????????????????
                    viewBinding.loadMoreRecyclerView.setCanLoadMore(false)
                    if (isNetworkConnect()) {
                        config.getAdapter().setLoadMoreState(LoadMoreState.ERROR)
                    } else {
                        config.getAdapter().setLoadMoreState(LoadMoreState.NO_NETWORK)
                    }
                } else if (viewData.isEmpty()) {
                    // ????????????????????????????????????
                    viewBinding.loadMoreRecyclerView.setCanLoadMore(false)
                    if (config.getViewModel().getCurrentPage() == 1) {
                        // ???????????????????????????????????????????????????????????????
                        config.getAdapter().setLoadMoreState(LoadMoreState.GONE)
                    } else {
                        config.getAdapter().setLoadMoreState(LoadMoreState.NO_MORE)
                    }
                } else {
                    // ????????????????????????Adapter????????????
                    viewBinding.loadMoreRecyclerView.setCanLoadMore(true)
                    config.getAdapter().addViewData(viewData)
                }
            }
            // ??????????????????
            viewBinding.loadMoreRecyclerView.setOnLoadMoreListener(object :
                LoadMoreRecyclerView.OnLoadMoreListener {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    loadData(isLoadMore = true, isReLoad = false)
                }
            })
        }

        // ??????????????????
        (activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).requestNetwork(
            NetworkRequest.Builder().build(), networkCallback
        )

        // ??????????????????
        loadData(isLoadMore = false, isReLoad = false, showLoading = true)
    }

    private fun interceptTouchEventTemporarily() {
        interceptTouchEvent = true
        mainHandler.postDelayed(resumeTouchRunnable, DELAY_RESUME_TOUCH_EVENT)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (interceptTouchEvent) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onDetachedFromWindow() {
        // ????????????????????????
        (activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).unregisterNetworkCallback(
            networkCallback
        )
        mainHandler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    fun showPageState(@PageState pageState: Int) {
        currentPageState = pageState
        mainHandler.removeCallbacks(showLoadingRunnable)
        when (currentPageState) {
            PageState.NORMAL -> {
                viewBinding.refreshLayout.isVisible = true
                viewBinding.loadingView.isVisible = false
                viewBinding.errorView.isVisible = false
            }
            PageState.LOADING -> {
                // ??????????????????500????????????
                mainHandler.postDelayed(showLoadingRunnable, DELAY_SHOW_LOADING)
            }
            PageState.LOAD_ERROR -> {
                viewBinding.refreshLayout.isVisible = false
                viewBinding.loadingView.isVisible = false
                viewBinding.errorView.isVisible = true
                viewBinding.errorView.showNetworkError(retryOnClickListener)
            }
            PageState.NO_NETWORK -> {
                viewBinding.refreshLayout.isVisible = false
                viewBinding.loadingView.isVisible = false
                viewBinding.errorView.isVisible = true
                viewBinding.errorView.showNoNetwork()
            }
            PageState.EMPTY -> {
                viewBinding.refreshLayout.isVisible = false
                viewBinding.loadingView.isVisible = false
                viewBinding.errorView.isVisible = true
                viewBinding.errorView.showEmpty(config.getEmptyIcon(), config.getEmptyMessage())
            }
        }
    }

    private fun loadData(isLoadMore: Boolean, isReLoad: Boolean, showLoading: Boolean = false) {
        if (showLoading) {
            showPageState(PageState.LOADING)
        }
        config.getViewModel().loadDataInternal(isLoadMore, isReLoad)
    }

    /**
     * ????????????????????????
     */
    fun startRefresh() {
        viewBinding.refreshLayout.autoRefresh()
    }

    /**
     * ??????????????????
     */
    fun refreshList() {
        loadData(isLoadMore = false, isReLoad = true)
    }

    fun removeData(position: Int) {
        val removedViewData = config.getAdapter().removeViewData(position)
        if (null != removedViewData) {
            config.getOnItemDeleteListener()?.onItemDelete(
                viewBinding.loadMoreRecyclerView,
                mutableListOf(removedViewData)
            )
        }
    }

    fun removeData(viewData: BaseViewData<*>) {
        val removedViewData = config.getAdapter().removeViewData(viewData)
        if (null != removedViewData) {
            config.getOnItemDeleteListener()?.onItemDelete(
                viewBinding.loadMoreRecyclerView,
                mutableListOf(removedViewData)
            )
        }
    }

    fun performItemClick(view: View, viewData: BaseViewData<*>, position: Int, id: Long) {
        // ????????????(???????????????item????????????)
        if (viewData is LoadMoreViewData) {
            // ????????????item??????
            when (config.getAdapter().getLoadMoreState()) {
                LoadMoreState.ERROR -> {
                    // ????????????????????????????????????????????????????????????
                    config.getAdapter().setLoadMoreState(LoadMoreState.LOADING)
                    loadData(isLoadMore = true, isReLoad = true)
                }
                LoadMoreState.NO_NETWORK -> {
                    // ???????????????????????????
                    toNetworkSetting(context)
                }
            }
        } else {
            // ??????item??????
            config.getOnItemClickListener()?.onItemClick(
                viewBinding.loadMoreRecyclerView,
                view,
                viewData,
                position,
                id
            )
        }
    }

    fun performItemLongClick(
        view: View,
        viewData: BaseViewData<*>,
        position: Int,
        id: Long
    ): Boolean {
        // ????????????
        var consumed = false
        if (viewData !is LoadMoreViewData) {
            consumed = config.getOnItemLongClickListener()?.onItemLongClick(
                viewBinding.loadMoreRecyclerView,
                view,
                viewData,
                position,
                id
            ) ?: false
        }
        return consumed
    }

    fun performItemChildViewClick(
        view: View,
        viewData: BaseViewData<*>,
        position: Int,
        id: Long,
        extra: Any?
    ) {
        config.getOnItemChildViewClickListener()?.onItemChildViewClick(
            viewBinding.loadMoreRecyclerView,
            view,
            viewData,
            position,
            id,
            extra
        )
    }

    interface OnItemClickListener {
        fun onItemClick(
            parent: RecyclerView,
            view: View,
            viewData: BaseViewData<*>,
            position: Int,
            id: Long
        )
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(
            parent: RecyclerView,
            view: View,
            viewData: BaseViewData<*>,
            position: Int,
            id: Long
        ): Boolean
    }

    interface OnItemChildViewClickListener {
        fun onItemChildViewClick(
            parent: RecyclerView,
            view: View,
            viewData: BaseViewData<*>,
            position: Int,
            id: Long,
            extra: Any?
        )
    }

    interface OnItemDeleteListener {
        fun onItemDelete(parent: RecyclerView, viewData: List<BaseViewData<*>>)
    }

    @IntDef(
        PageState.NORMAL,
        PageState.LOADING,
        PageState.LOAD_ERROR,
        PageState.NO_NETWORK,
        PageState.EMPTY
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class PageState {
        companion object {
            const val NORMAL = 0
            const val LOADING = 1
            const val LOAD_ERROR = 2
            const val NO_NETWORK = 3
            const val EMPTY = 4
        }
    }

    private inner class NetworkCallback : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            mainHandler.post {
                if (isAttachedToWindow) {
                    if (!config.getViewModel().needNetwork()) {
                        return@post
                    }
                    // ??????????????????????????????????????????????????????????????????
                    if (currentPageState == PageState.LOAD_ERROR || currentPageState == PageState.NO_NETWORK) {
                        loadData(isLoadMore = false, isReLoad = true, showLoading = true)
                    } else if (currentPageState == PageState.NORMAL && (config.getAdapter()
                            .getLoadMoreState() == LoadMoreState.ERROR || config.getAdapter()
                            .getLoadMoreState() == LoadMoreState.NO_NETWORK)
                    ) {
                        config.getAdapter().setLoadMoreState(LoadMoreState.LOADING)
                        loadData(isLoadMore = true, isReLoad = true, showLoading = false)
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            mainHandler.post {
                if (isAttachedToWindow) {
                    if (!config.getViewModel().needNetwork()) {
                        return@post
                    }
                    // ??????????????????????????????????????????????????????
                    if (currentPageState == PageState.LOAD_ERROR || currentPageState == PageState.LOADING) {
                        showPageState(PageState.NO_NETWORK)
                    } else if (currentPageState == PageState.NORMAL && config.getAdapter()
                            .getLoadMoreState() == LoadMoreState.ERROR
                    ) {
                        config.getAdapter().setLoadMoreState(LoadMoreState.NO_NETWORK)
                    }
                }
            }
        }
    }

    class Config {

        private lateinit var viewModel: BaseRecyclerViewModel
        private lateinit var adapter: LoadMoreAdapter
        private lateinit var layoutManager: RecyclerView.LayoutManager
        private var itemDecoration: RecyclerView.ItemDecoration? = null
        private var itemAnimator: RecyclerView.ItemAnimator? = null
        private var pullRefreshEnable = true
        private var pullUploadMoreEnable = true
        private var showScrollBar = true

        // ???????????????
        private var emptyMessage: String = ""

        // ???????????????
        @DrawableRes
        private var emptyIcon: Int = -1
        private var onItemClickListener: OnItemClickListener? = null
        private var onItemLongClickListener: OnItemLongClickListener? = null
        private var onItemSubViewClickListener: OnItemChildViewClickListener? = null
        private var onItemDeleteListener: OnItemDeleteListener? = null

        fun getViewModel() = viewModel

        fun setViewModel(viewModel: BaseRecyclerViewModel): Config {
            this.viewModel = viewModel
            return this
        }

        fun getAdapter() = adapter

        fun setAdapter(adapter: LoadMoreAdapter): Config {
            this.adapter = adapter
            return this
        }

        fun getLayoutManager() = layoutManager

        fun setLayoutManager(layoutManager: RecyclerView.LayoutManager): Config {
            this.layoutManager = layoutManager
            return this
        }

        fun getItemDecoration() = itemDecoration

        fun setItemDecoration(itemDecoration: RecyclerView.ItemDecoration): Config {
            this.itemDecoration = itemDecoration
            return this
        }

        fun getItemAnimator() = itemAnimator

        fun setItemAnimator(itemAnimator: RecyclerView.ItemAnimator): Config {
            this.itemAnimator = itemAnimator
            return this
        }

        fun getPullRefreshEnable() = pullRefreshEnable

        fun setPullRefreshEnable(pullRefreshEnable: Boolean): Config {
            this.pullRefreshEnable = pullRefreshEnable
            return this
        }

        fun getPullUploadMoreEnable() = pullUploadMoreEnable

        fun setPullUploadMoreEnable(pullUploadMoreEnable: Boolean): Config {
            this.pullUploadMoreEnable = pullUploadMoreEnable
            return this
        }

        fun getShowScrollBar() = showScrollBar

        fun setShowScrollBar(showScrollBar: Boolean): Config {
            this.showScrollBar = showScrollBar
            return this
        }

        fun getEmptyMessage() = emptyMessage

        fun setEmptyMessage(message: String): Config {
            this.emptyMessage = message
            return this
        }

        fun getEmptyIcon() = emptyIcon

        fun setEmptyIcon(@DrawableRes icon: Int): Config {
            this.emptyIcon = icon
            return this
        }

        fun getOnItemClickListener() = onItemClickListener

        fun setOnItemClickListener(onItemClickListener: OnItemClickListener): Config {
            this.onItemClickListener = onItemClickListener
            return this
        }

        fun getOnItemLongClickListener() = onItemLongClickListener

        fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener): Config {
            this.onItemLongClickListener = onItemLongClickListener
            return this
        }

        fun getOnItemChildViewClickListener() = onItemSubViewClickListener

        fun setOnItemChildViewClickListener(onItemSubViewClickListener: OnItemChildViewClickListener): Config {
            this.onItemSubViewClickListener = onItemSubViewClickListener
            return this
        }

        fun getOnItemDeleteListener() = onItemDeleteListener

        fun setOnItemDeleteListener(onItemDeleteListener: OnItemDeleteListener): Config {
            this.onItemDeleteListener = onItemDeleteListener
            return this
        }

        fun check(context: Context) {
            if (!::viewModel.isInitialized) {
                throw GlobalException.of("you should set a ViewModel")
            }
            if (!::adapter.isInitialized) {
                adapter = LoadMoreAdapter()
            }
            if (!::layoutManager.isInitialized) {
                layoutManager = LinearLayoutManager(context)
            }
            if (TextUtils.isEmpty(emptyMessage)) {
                emptyMessage = context.resources.getString(R.string.page_state_empty)
            }
            if (emptyIcon == -1) {
                emptyIcon = R.drawable.ic_empty
            }
        }
    }

}