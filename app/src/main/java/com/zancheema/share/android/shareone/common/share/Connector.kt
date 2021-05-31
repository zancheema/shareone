package com.zancheema.share.android.shareone.common.share

interface Connector {
    suspend fun connect()

    fun closeConnection()
}

interface ConnectorFactory<out T: Connector> {
    fun create(): T
}