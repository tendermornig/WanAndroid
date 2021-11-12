package com.cl.wanandroid.bean.excption

class GlobalException private constructor(message: String) : RuntimeException(message) {
    companion object {
        fun of(message: String) = GlobalException(message)
    }
}