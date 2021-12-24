package io.github.crackthecodeabhi.kreds.commands

import io.github.crackthecodeabhi.kreds.args.SetOption
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.KredsClientGroup
import io.github.crackthecodeabhi.kreds.protocol.KredsRedisDataException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

lateinit var client: KredsClient
lateinit var c : StringCommands

class StringCommandsTest : BehaviorSpec({
    beforeSpec{
        client = KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379"))
        //client.flushAll(SYNC) // TODO: clear DB before spec
        c = client
    }
    afterSpec{
        client.close()
    }

    Given("set"){
        When("new key"){
            Then("return OK"){
                c.set("newKey","100") shouldBe "OK"
            }
        }
        When("existing key with GET option"){
            Then("return old value"){
                c.set("newKey","101",SetOption.Builder(get = true).build()) shouldBe "100"
            }
        }
        When("key does not exist with GET option"){
            Then("return null"){
                c.set("noKey","100",SetOption.Builder(get = true).build()) shouldBe null
            }
        }
    }

    Given("get"){
        When("non-existing key"){
            Then("return null"){
                c.get("non-existing") shouldBe null
            }
        }
        When("non-string type"){
            Then("throws exception"){
                client.hset("hashKey","foo" to "bar")
                shouldThrow<KredsRedisDataException>{
                    c.get("hashKey")
                }
            }
        }
    }
})