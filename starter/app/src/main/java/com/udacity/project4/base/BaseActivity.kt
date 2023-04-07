package com.udacity.project4.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.udacity.project4.common.AuthenticationViewModel
import org.koin.android.ext.android.inject

abstract class BaseActivity<DB: ViewDataBinding>(private val resId: Int) : AppCompatActivity() {

    protected val authenticationViewModel: AuthenticationViewModel by inject()

    lateinit var binding: DB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            resId
        )
        initView()
        initAction()
        initObserver()
    }

    abstract fun initObserver()

    abstract fun initAction()

    abstract fun initView()
}