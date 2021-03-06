package com.subwilven.basemodel.project_base.base.other

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.subwilven.basemodel.project_base.BaseApplication
import com.subwilven.basemodel.project_base.POJO.ErrorModel
import com.subwilven.basemodel.project_base.POJO.Message
import com.subwilven.basemodel.project_base.POJO.ScreenStatus
import com.subwilven.basemodel.project_base.base.adapters.AdatperItemLoading
import com.subwilven.basemodel.project_base.utils.network.Failure
import com.subwilven.basemodel.project_base.utils.network.Result
import com.subwilven.basemodel.project_base.utils.network.RetrofitCorounites


public abstract class BaseViewModel : ViewModel() {

    /*
     Do not forget to check if liveData value is null before requesting the data
     Use appScope when you need the response of network request even that user has left the screen
     Use viewModelScope when the network response don't represent any importance if the user left the screen
    */

    val appScope = ProcessLifecycleOwner.get().lifecycleScope
    val mLoadingViews =
        SingleLiveEvent<Pair<String, MutableMap<Int, Boolean>>>()//list of view that may show loading instead show full screen loading
    val mSnackBarMessage = SingleLiveEvent<Message>()
    val mEnableSensitiveInputs = MutableLiveData<Boolean>()
    val mToastMessage = SingleLiveEvent<Message>()
    val mDialogMessage = SingleLiveEvent<Message>()
    val mShowLoadingFullScreen = MutableLiveData<Pair<String, Boolean>>()
    val mShowErrorFullScreen = MutableLiveData<Pair<String, ErrorModel?>>()
    private val registeredFragments = HashMap<String, ScreenStatus>()
    private var lastRegisteredFragment: String? = null
    var isNewInstant: Boolean = true

    val lastRegisterdFragmentStatus: ScreenStatus?
        get() = registeredFragments[lastRegisteredFragment]

    init {
        mLoadingViews.value = Pair("", mutableMapOf())
    }

    //becarfull when useing this fun if more than one fragment share the same viewmodel
    open fun loadInitialData() {}

    fun registerFragment(fragmentClassName: String) {

        if (!registeredFragments.containsKey(fragmentClassName))
            registeredFragments[fragmentClassName] = ScreenStatus.STARTING
        lastRegisteredFragment = fragmentClassName
    }

    //if all network jobs finished successfully mark the screen as completed
    fun markAsCompleted(results: List<Result<Any?>>) {
        for (result in results) {
            if (result is Failure)
                return
        }
        lastRegisteredFragment?.let { registeredFragments[it] = ScreenStatus.COMPLETED }

    }


    suspend fun <T> networkCall(viewId: Int? = null,errorWillBeHandled :Boolean =false, block: suspend () -> T): Result<T> {
        lastRegisteredFragment?.let {
            return RetrofitCorounites(this, it, viewId,errorWillBeHandled).networkCall(block)
        }
        return Failure()
    }

    suspend fun <T> networkCallWithAdapter(
        adapterItem: AdatperItemLoading? = null,
        errorWillBeHandled :Boolean =false,
        block: suspend () -> T
    ): Result<T> {
        lastRegisteredFragment?.let {
            return RetrofitCorounites(this, it, adapterItem,errorWillBeHandled).networkCall(block)
        }
        return Failure()
    }


//    suspend fun <T> multipleNetworkCall(viewId: Int? = null, blocks: List<Pair<suspend () -> T,(T)->Unit>>): Result<Any> {
//        return RetrofitCorounites(this, viewId).multipleNetworkCall(blocks)
//    }

    fun markAsCompleted(name: String) {
        registeredFragments[name] = ScreenStatus.COMPLETED
    }


    fun showSnackBarMessage(s: Message) {
        mSnackBarMessage.value = s
    }

    fun showToastMessage(s: Message) {
        if (mToastMessage.hasActiveObservers())
            mToastMessage.value = s
        else
            showAlertBar(s)
    }

    fun showDialogMessage(s: Message) {
        mDialogMessage.value = s
    }

    fun enableSensitiveInputs(b: Boolean) {
        mEnableSensitiveInputs.value = b
    }

    fun showLoadingFullScreen(fragmentTag: String?, b: Boolean) {
        mShowLoadingFullScreen.value = Pair(fragmentTag!!, b)
    }

    fun showNoConnectionFullScreen(fragmentTag: String?, errorModel: ErrorModel?) {
        mShowErrorFullScreen.value = Pair(fragmentTag!!, errorModel)
    }

    fun showAlertBar(message: Message) {
        BaseApplication.instance?.showAlertBar(message)
    }

    private val isStartingFragment: Boolean
        get() = lastRegisterdFragmentStatus == ScreenStatus.STARTING

    fun showLoading(fragmentTag: String, viewId: Int?, adapterItem: AdatperItemLoading?) {
        enableSensitiveInputs(false)
        when {
            isStartingFragment -> {
                showNoConnectionFullScreen(fragmentTag, null)
                showLoadingFullScreen(fragmentTag, true)
            }
            viewId != null -> {
                mLoadingViews.value?.second?.put(viewId, true)
                mLoadingViews.value =
                    Pair(fragmentTag, mLoadingViews.value?.second?.toMutableMap()!!)
            }
            adapterItem != null -> {
                adapterItem.isLoading = true
                notifyItemChanges(adapterItem)
            }
        }
    }

    //used when there is a recycler view and each item can make network request  so by this fun we can change the status of loading views in the item
    open fun notifyItemChanges(adapterItem: AdatperItemLoading) {

    }

    fun hideLoading(fragmentTag: String, viewId: Int?, adapterItem: AdatperItemLoading?) {
        enableSensitiveInputs(true)
        when {
            isStartingFragment -> showLoadingFullScreen(fragmentTag, false)
            viewId != null -> {
                mLoadingViews.value?.second?.put(viewId, false)
                mLoadingViews.value =
                    Pair(fragmentTag, mLoadingViews.value?.second?.toMutableMap()!!)
            }
            adapterItem != null -> {
                adapterItem.isLoading = false
                notifyItemChanges(adapterItem)
            }
        }
    }


    fun unRegister(fragmentTag: String) {
        //registeredFragments.remove(fragmentClassName)
        if (fragmentTag == lastRegisteredFragment)
            lastRegisteredFragment = null // MAY CAUSE PROBLEM
    }

}
