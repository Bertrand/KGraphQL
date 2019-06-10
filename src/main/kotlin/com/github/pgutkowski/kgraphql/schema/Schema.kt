package com.github.pgutkowski.kgraphql.schema

import com.github.pgutkowski.kgraphql.Context
import com.github.pgutkowski.kgraphql.schema.introspection.__Schema
import kotlinx.coroutines.CoroutineScope


interface Schema : __Schema {
    fun execute(request: String, variables: String?, context: Context = Context(emptyMap())) : String
    suspend fun suspendExecute(request: String, variables: String?, context: Context = Context(emptyMap()), coroutineScope: CoroutineScope) : String

    fun execute(request: String, context: Context = Context(emptyMap())) = execute(request, null, context)
    suspend fun suspendExecute(request: String, context: Context = Context(emptyMap()), coroutineScope: CoroutineScope) = suspendExecute(request, null, context, coroutineScope)

}