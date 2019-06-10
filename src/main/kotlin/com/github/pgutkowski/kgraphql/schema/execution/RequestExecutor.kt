package com.github.pgutkowski.kgraphql.schema.execution

import com.github.pgutkowski.kgraphql.Context
import com.github.pgutkowski.kgraphql.request.VariablesJson
import kotlinx.coroutines.CoroutineScope


interface RequestExecutor {
    fun execute(plan : ExecutionPlan, variables: VariablesJson, context: Context) : String
    suspend fun suspendExecute(plan : ExecutionPlan, variables: VariablesJson, context: Context, coroutineScope: CoroutineScope) : String
}