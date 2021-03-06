package com.subwilven.basemodel.project_base.base.adapters

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.subwilven.basemodel.project_base.views.MyRecyclerView
import java.util.*

public abstract class BaseAdapter<T, VH : BaseViewHolder<T>> : RecyclerView.Adapter<VH> , LifecycleObserver {

    var list: MutableList<T>? = null

    constructor() {}

    constructor(list: MutableList<T>) {
        this.list = list
    }
    var adapterDataObservation: AdapterDataObservation?=null

    fun registerAdapterDataObservertion(lifecycleOwner: LifecycleOwner,recyclerView: MyRecyclerView) {
        lifecycleOwner.lifecycle.addObserver(this)
        adapterDataObservation = AdapterDataObservation(recyclerView)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun connectListener() {
        registerAdapterDataObserver(adapterDataObservation!!)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun disconnectListener() {
        unregisterAdapterDataObserver(adapterDataObservation!!)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clearAdapterDataObservation(){
        adapterDataObservation?.clear()
    }

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        holder.onBind(list!![position],payloads)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        getItem(position)?.let {
            holder.onBind(it)
        }
    }

    override fun getItemCount(): Int {
        return if (list != null) list!!.size else 0
    }


    open fun setData(list: MutableList<T>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun getItem(index :Int) : T?{
        list?.let {
            return it[index]
        }
        return null
    }

    fun addItem(item: T) {
        initList()
        list!!.add(item)
        notifyItemInserted(itemCount - 1)
    }

    fun addItems(items: ArrayList<T>){
        initList()
        list!!.addAll(items)
        notifyItemInserted(list!!.count() - items.count())
    }

    open fun addItem(item: T, position: Int) {
        initList()
        list!!.add(position, item)
        notifyItemInserted(position)
    }

    fun updateItem(item: T) {
        val index = list!!.indexOf(item)
        if (index != -1)
            updateItem(item, index)
    }

    fun updateItem(item: T, position: Int) {
        initList()
        list!![position] = item
        notifyItemChanged(position)
    }

    open fun removeItem(item: T) {
        val index = list!!.indexOf(item)
        if (index != -1)
            removeItem(index)
    }

    open fun removeItem(position: Int) {
        initList()
        list!!.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clear() {
        initList()
        list!!.clear()
        notifyDataSetChanged()
    }

    private fun initList() {
        if (list == null)
            list = ArrayList()
    }
}
