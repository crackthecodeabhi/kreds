# Kreds 

[![Maven Central](https://img.shields.io/maven-central/v/io.github.crackthecodeabhi/kreds.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.crackthecodeabhi%22%20AND%20a:%22kreds%22)
[![javadoc](https://javadoc.io/badge2/io.github.crackthecodeabhi/kreds/javadoc.svg)](https://javadoc.io/doc/io.github.crackthecodeabhi/kreds)
[![CI](https://github.com/crackthecodeabhi/kreds/actions/workflows/ci.yml/badge.svg)](https://github.com/crackthecodeabhi/kreds/actions/workflows/ci.yml)
[![CD](https://github.com/crackthecodeabhi/kreds/actions/workflows/gradle-publish.yml/badge.svg?branch=release)](https://github.com/crackthecodeabhi/kreds/actions/workflows/gradle-publish.yml)
[![Pure Kotlin](https://img.shields.io/badge/100%25-kotlin-blue.svg)](https://kotlinlang.org/)
[![codecov](https://codecov.io/gh/crackthecodeabhi/kreds/branch/main/graph/badge.svg?token=Y4XBBIH4BC)](https://codecov.io/gh/crackthecodeabhi/kreds)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcrackthecodeabhi%2Fkreds.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcrackthecodeabhi%2Fkreds?ref=badge_shield)
[![Join the chat at https://gitter.im/kreds-redis/community](https://badges.gitter.im/kreds-redis/community.svg)](https://gitter.im/kreds-redis/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub commit activity](https://img.shields.io/github/commit-activity/m/crackthecodeabhi/kreds)

Kreds is a non-blocking, coroutine based redis client for Kotlin.

Kreds is designed to be EASY to use.

Kreds is built around coroutines, providing you an imperative paradigm of programming 
without blocking threads or callback hell.

Kreds is compatible with redis 6.x.x and above.

## So what can I do with Kreds?

All the following redis features are supported:

* Connection handling commands
* Server commands
* Cluster commands
* Commands operating on strings,hashes,lists,keys,sets,sorted sets 
* Transactions 
* Pipelining 
* Publish/Subscribe

More features will be added in upcoming releases.

## How do I use it?

To use it just:
```kotlin
launch {
    KredsClientGroup.newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
        client.set("foo","100") 
        println("incremented value of foo ${client.incr("foo")}") // prints 101
        client.expire("foo",3u) // set expiration to 3 seconds
        delay(3000)
        assert(client.get("foo") == null)
    }
    KredsClientGroup.shutdown() // shutdown the client group.
}
```

## How to get it?

```xml
<dependency>
  <groupId>io.github.crackthecodeabhi</groupId>
  <artifactId>kreds</artifactId>
  <version>0.3</version>
</dependency>
```

Gradle Groovy DSL

```groovy
implementation 'io.github.crackthecodeabhi:kreds:0.3'

```
Gradle Kotlin DSL
```kotlin
implementation("io.github.crackthecodeabhi:kreds:0.3")
```

## License

Copyright (c) 2021 Abhijith Shivaswamy

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.




[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcrackthecodeabhi%2Fkreds.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcrackthecodeabhi%2Fkreds?ref=badge_large)
