package com.cl.wanandroid.util

import com.orhanobut.logger.Logger

private const val VERBOSE = 1

private const val DEBUG = 2

private const val INFO = 3

private const val WARN = 4

private const val ERROR = 5

private var level = VERBOSE

fun v(message: String, vararg args: Any) {
    if (level >= VERBOSE) {
        Logger.v(message, args)
    }
}

fun d(message: String, vararg args: Any) {
    if (level >= DEBUG) {
        Logger.d(message, args)
    }
}

fun i(message: String, vararg args: Any) {
    if (level >= INFO) {
        Logger.i(message, args)
    }
}

fun w(message: String, vararg args: Any) {
    if (level >= WARN) {
        Logger.w(message, args)
    }
}

fun e(message: String, vararg args: Any) {
    if (level >= ERROR) {
        Logger.e(message, args)
    }
}

fun e(throwable: Throwable, message: String, vararg args: Any) {
    if (level >= ERROR) {
        Logger.e(throwable, message, args)
    }
}