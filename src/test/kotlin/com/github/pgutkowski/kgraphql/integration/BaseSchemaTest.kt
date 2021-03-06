package com.github.pgutkowski.kgraphql.integration

import com.github.pgutkowski.kgraphql.Actor
import com.github.pgutkowski.kgraphql.Director
import com.github.pgutkowski.kgraphql.Film
import com.github.pgutkowski.kgraphql.FilmType
import com.github.pgutkowski.kgraphql.Id
import com.github.pgutkowski.kgraphql.IdScalarSupport
import com.github.pgutkowski.kgraphql.Person
import com.github.pgutkowski.kgraphql.Scenario
import com.github.pgutkowski.kgraphql.defaultSchema
import com.github.pgutkowski.kgraphql.deserialize
import com.github.pgutkowski.kgraphql.schema.DefaultSchema
import com.github.pgutkowski.kgraphql.schema.dsl.SchemaBuilder
import org.junit.After


abstract class BaseSchemaTest {

    companion object {
        val INTROSPECTION_QUERY = """
            query IntrospectionQuery {
                __schema {
                    queryType { name description kind}
                    mutationType { name description kind }
                    subscriptionType { name description kind }
                    types {
                        name
                        kind
                        description
                        ...FullType
                    }
                    directives {
                        name
                        description
                        locations
                        args {
                            ...InputValue
                        }
                    }
                }
            }


            fragment FullType on __Type {
                fields(includeDeprecated: true) {
                    name
                    description
                    args {
                        ...InputValue
                    }
                    type {
                        ...TypeRef
                    }
                    isDeprecated
                    deprecationReason
                }
                inputFields {
                    ...InputValue
                }
                interfaces {
                    ...TypeRef
                }
                enumValues(includeDeprecated: true) {
                    name
                    description
                    isDeprecated
                    deprecationReason
                }
                possibleTypes {
                    ...TypeRef
                }
            }

            fragment InputValue on __InputValue {
                name
                description
                type { ...TypeRef }
                defaultValue
            }

            fragment TypeRef on __Type {
                kind
                name
                description
                ofType {
                    kind
                    name
                    description
                    ofType {
                        kind
                        name
                        description
                        ofType {
                            kind
                            name
                            description
                            ofType {
                                kind
                                name
                                description
                                ofType {
                                    kind
                                    name
                                    description
                                    ofType {
                                        kind
                                        name
                                        description
                                        ofType {
                                            kind
                                            name
                                            description
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """
    }

    //test film 1
    val tomHardy = Actor("Tom Hardy", 232)
    val christianBale = Actor("Christian Bale", 232)
    val christopherNolan = Director("Christopher Nolan", 43, listOf(tomHardy, christianBale))
    val prestige = Film(Id("Prestige", 2006), 2006, "Prestige", christopherNolan)

    //test film 2
    val bradPitt = Actor("Brad Pitt", 763)
    val morganFreeman = Actor("Morgan Freeman", 1212)
    val kevinSpacey = Actor("Kevin Spacey", 2132)
    val davidFincher = Director("David Fincher", 43, listOf(bradPitt, morganFreeman, kevinSpacey))
    val se7en = Film(Id("Se7en", 1995), 1995, "Se7en", davidFincher)

    //new actors created via mutations in schema
    val createdActors = mutableListOf<Actor>()

    val testedSchema = defaultSchema {
        configure {
            useDefaultPrettyPrinter = true
        }

        query("number") {
            description = "returns little of big number"
            resolver { big : Boolean -> if(big) 10000 else 0 }
        }
        query("film") {
            description = "mock film"
            resolver { -> prestige }
        }
        query("actors") {
            description = "all actors"
            resolver { ->
                listOf(bradPitt, morganFreeman, kevinSpacey, tomHardy, christianBale) }
        }
        query("filmByRank") {
            description = "ranked films"
            resolver { rank: Int -> when(rank){
                1 -> prestige
                2 -> se7en
                else -> null
            }}
        }
        query("filmsByType") {
            description = "film categorized by type"
            resolver {type: FilmType -> listOf(prestige, se7en) }
        }
        query("people") {
            description = "List of all people"
            resolver { -> listOf(davidFincher, bradPitt, morganFreeman, christianBale, christopherNolan) }
        }
        query("randomPerson") {
            description = "not really random person"
            resolver { -> davidFincher as Person /*not really random*/}
        }
        mutation("createActor") {
            description = "create new actor"
            resolver { name : String, age : Int ->
                val actor = Actor(name, age)
                createdActors.add(actor)
                actor
            }
        }
        query("scenario") {
            resolver { -> Scenario(Id("GKalus", 234234), "Gamil Kalus", "Very long scenario") }
        }
        stringScalar<Id> {
            description = "unique, concise representation of film"
            coercion = IdScalarSupport()
        }
        enum<FilmType>{ description = "type of film, base on its length" }
        type<Person>{ description = "Common data for any person"}
        type<Scenario>{
            property(Scenario::author){
                ignore = true
            }
            transformation(Scenario::content) { content : String, uppercase: Boolean? ->
                if(uppercase == true) content.toUpperCase() else content
            }
        }
        val favouriteID = unionType("Favourite") {
            type<Actor>()
            type<Scenario>()
            type<Director>()
        }

        type<Actor>{
            description = "An actor is a person who portrays a character in a performance"
            property<Boolean?>("isOld") {
                resolver { actor -> (actor.age > 500) as Boolean? }
            }
            property<String>("picture") {
                resolver { actor, big : Boolean? ->
                    val actorName = actor.name.replace(' ', '_')
                    if(big == true){
                        "http://picture.server/pic/$actorName?big=true"
                    } else {
                        "http://picture.server/pic/$actorName?big=false"
                    }
                }
            }
            unionProperty("favourite") {
                returnType = favouriteID
                resolver { actor -> when(actor){
                    bradPitt -> tomHardy
                    tomHardy -> christopherNolan
                    morganFreeman -> Scenario(Id("234", 33), "Paulo Coelho", "DUMB")
                    else -> christianBale
                }}
            }
        }

        inputType<Actor>() {
            name = "ActorInput"
        }

        mutation("createActorWithAliasedInputType") {
            description = "create new actor from full fledged ActorInput as input type"
            resolver { newActor: Actor ->
                createdActors.add(newActor)
                newActor
            }
        }
    }

    @After
    fun cleanup() = createdActors.clear()

    fun execute(query: String, variables : String? = null) = deserialize(testedSchema.execute(query, variables))
}