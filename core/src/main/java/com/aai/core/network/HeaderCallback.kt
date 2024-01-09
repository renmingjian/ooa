package com.aai.core.network

interface HeaderCallback {

    fun onGetHeaders(map: Map<String,List<String>>)

}