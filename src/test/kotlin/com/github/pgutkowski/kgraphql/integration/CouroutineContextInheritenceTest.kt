package com.github.pgutkowski.kgraphql.integration

import com.github.pgutkowski.kgraphql.KGraphQL
import com.github.pgutkowski.kgraphql.deserialize
import com.github.pgutkowski.kgraphql.extract
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.*

class CouroutineContextInheritenceTest {


    val suspendResolverSchema = KGraphQL.schema {
        query("coroutineName") {
            suspendResolver { ->
                val result = coroutineContext.get(CoroutineName.Key)?.name
                "${result ?: "no-name"}"
            }
        }
    }

    @Test
    fun `ensuring caller's coroutine context is visible in suspend resolvers`(){

        runBlocking {
            val response = async(CoroutineName("A Fancy name")) {
                suspendResolverSchema.suspendExecute(request = "{coroutineName}", coroutineScope = this)
            }

            val resp = response.await()
            val map = deserialize(resp)

            MatcherAssert.assertThat(map.extract<String>("data/coroutineName"), CoreMatchers.equalTo("A Fancy name"))
        }
    }

}