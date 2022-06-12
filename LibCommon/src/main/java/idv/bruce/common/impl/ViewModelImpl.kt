package idv.bruce.common.impl

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class ViewModelImpl : ViewModel() {
    companion object {
        val toastMsg: MutableLiveData<String> = MutableLiveData()

        val alertMsgRes: MutableLiveData<Int> = MutableLiveData()

        val progressMsgRes: MutableLiveData<Int> = MutableLiveData()
    }

    private var models: List<ModelImpl>? = null

    open fun onServiceConnected(binder: ServiceImpl.ServiceBinder) {
        models = binder.models
    }

    open fun onServiceDisconnect() {

    }

    protected fun getModel(tag: String): ModelImpl? = models?.find { it.tag == tag }
}