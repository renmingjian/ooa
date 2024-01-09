package com.aai.core.network

class AINetworkException(val code: String, override val message: String) : Throwable(message)