package com.lkr.lib_base.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * author : baoning
 * company：inkr
 * desc   : fragment基类, 用于需要设置缺省页的fragment
 */
abstract class InkrBaseEmptyableFragment : Fragment() {

    protected open val replaceId=View.generateViewId()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view=super.onCreateView(inflater, container, savedInstanceState)?.apply {
            id = replaceId
        }
        //生成根布局
        var root= activity?.let {
            FrameLayout(it).apply {
                layoutParams=ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        }
        root?.addView(view)
        return root
    }


    /**
     * 添加loading等布局工具类
     */
    inner class Layout(val layout:Int, val viewInit:(View.()->Unit)?=null){
        operator fun unaryPlus():View? {
            var v:View?=null
            with(view as? ViewGroup) {
                this?.addView(layoutInflater.inflate(layout, null).apply{
                    tag=layout
                    v=this
                }, -1)
            }
            return v?.apply {
                viewInit?.apply {
                    this.invoke(v!!)
                }
            }
        }
        operator fun unaryMinus(){
            with(view as? ViewGroup){
                this?.findViewWithTag<View>(layout)?.apply {
                    this@with.removeView(this)
                }
            }
        }
    }
    /**
     * loading页面工具
     */
    fun CoroutineScope.launchWithLoading(
        @LayoutRes layout:Int,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launch(context, start) {
            +Layout(layout)
            block()
            -Layout(layout)
        }
    }

    inline fun <reified T> arg(key:String, default:T): Lazy<T>{
        return lazy {
            if (arguments?.get(key) == null) {
                default
            } else {
                arguments?.get(key) as T
            }
        }
    }
}