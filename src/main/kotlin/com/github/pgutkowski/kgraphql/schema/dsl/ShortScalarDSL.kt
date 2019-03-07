package com.github.pgutkowski.kgraphql.schema.dsl

import com.github.pgutkowski.kgraphql.schema.SchemaException
import com.github.pgutkowski.kgraphql.schema.scalar.ShortScalarCoercion
import com.github.pgutkowski.kgraphql.schema.scalar.ScalarCoercion
import kotlin.reflect.KClass


class ShortScalarDSL<T : Any>(kClass: KClass<T>, block: ScalarDSL<T, Short>.() -> Unit)
    : ScalarDSL<T, Short>(kClass, block){

    override fun createCoercionFromFunctions(): ScalarCoercion<T, Short> {
        return object : ShortScalarCoercion<T> {

            val serializeImpl = serialize ?: throw SchemaException(PLEASE_SPECIFY_COERCION)

            val deserializeImpl = deserialize ?: throw SchemaException(PLEASE_SPECIFY_COERCION)

            override fun serialize(instance: T): Short = serializeImpl(instance)

            override fun deserialize(raw: Short): T = deserializeImpl(raw)
        }
    }

}