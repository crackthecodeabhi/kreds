# Kreds

[![Maven Central](https://img.shields.io/maven-central/v/io.github.crackthecodeabhi/kreds.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.crackthecodeabhi%22%20AND%20a:%22kreds%22)
[![javadoc](https://javadoc.io/badge2/io.github.crackthecodeabhi/kreds/javadoc.svg)](https://javadoc.io/doc/io.github.crackthecodeabhi/kreds)
[![CI](https://github.com/crackthecodeabhi/kreds/actions/workflows/ci.yml/badge.svg)](https://github.com/crackthecodeabhi/kreds/actions/workflows/ci.yml)
[![CD](https://github.com/crackthecodeabhi/kreds/actions/workflows/gradle-publish.yml/badge.svg?branch=release)](https://github.com/crackthecodeabhi/kreds/actions/workflows/gradle-publish.yml)
[![Pure Kotlin](https://img.shields.io/badge/100%25-kotlin-blue.svg)](https://kotlinlang.org/)
[![codecov](https://codecov.io/gh/crackthecodeabhi/kreds/branch/main/graph/badge.svg?token=Y4XBBIH4BC)](https://codecov.io/gh/crackthecodeabhi/kreds)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcrackthecodeabhi%2Fkreds.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcrackthecodeabhi%2Fkreds?ref=badge_shield)
[![Join the chat at https://gitter.im/kreds-redis/community](https://badges.gitter.im/kreds-redis/community.svg)](https://gitter.im/kreds-redis/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/crackthecodeabhi/kreds)
![GitHub](https://img.shields.io/github/license/crackthecodeabhi/kreds)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=crackthecodeabhi_kreds&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=crackthecodeabhi_kreds)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=crackthecodeabhi_kreds&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=crackthecodeabhi_kreds)


Kreds is a thread-safe, idiomatic, coroutine based Redis client written in 100% Kotlin.

## Why Kreds? 

* Kreds is designed to be EASY to use. 
* Kreds has clean API, clear return types, **avoid the dreaded null pointer exception at compile time!**
* Kreds is built around coroutines, providing you an **imperative paradigm of programming without blocking threads, futures or callback hell**, thanks to Kotlin Coroutines!
* Run blocking commands **without blocking** Java threads. (Only the inexpensive coroutines are blocked)
* Kreds uses Netty under the hood and is **truly asynchronous**.
* High throughput.

## Use cases
* Web app cache client: Don't open multiple connections to redis, for each http request, use single connection in a thread-safe manner.
* Pub/Sub: Subscribe to multiple channels from one or multiple Redis, without being limited by java threads.

## Compatibility
* Java 11 and above
* Compatible with Redis: 6 and above.
* Tested against Redis: 6.2.7, 7.0

## Documentation
You can find the user guide and documentation [here](https://crackthecodeabhi.github.io/kreds) :construction:

## So what can I do with Kreds?

All the following redis features are supported:

* Commands operating on Strings, Hash, Lists, Keys, Sets, Sorted Sets. :heavy_check_mark:
* Blocking commands. :heavy_check_mark: 
* Pipelining. :heavy_check_mark:
* Publish/Subscribe. :heavy_check_mark:
* Connection handling commands. :heavy_check_mark:
* RedisJSON support. :heavy_check_mark:
* Scripting support. :heavy_check_mark:
* Functions. :heavy_check_mark:
* Transactions. :construction: [Implementation done, testing in progress.]

## How do I use it?

To use it just:
```kotlin
launch {
    newClient(Endpoint.from("127.0.0.1:6379")).use { client ->
        client.set("foo","100") 
        println("incremented value of foo ${client.incr("foo")}") // prints 101
        client.expire("foo",3u) // set expiration to 3 seconds
        delay(3000)
        assert(client.get("foo") == null)
    }
}
```

## How to get it?

```xml
<dependency>
  <groupId>io.github.crackthecodeabhi</groupId>
  <artifactId>kreds</artifactId>
  <version>0.8</version>
</dependency>
```

Gradle Groovy DSL

```groovy
implementation 'io.github.crackthecodeabhi:kreds:0.8'

```
Gradle Kotlin DSL
```kotlin
implementation("io.github.crackthecodeabhi:kreds:0.8")
```

## Sponsor the project on Github or Patreon!
Do you find this project useful? Please show your support by sponsoring the project.
* [Patreon](https://patreon.com/abhicreates)
* [Github Sponsors](https://github.com/sponsors/crackthecodeabhi)

## Contribution
Most of the frequently used commands are implemented, if you find any command unimplemented, you are welcome to open a pull request or request for feature.

### Please write test cases which cover your new code sections.

## License

Copyright (c) 2021 Abhijith Shivaswamy

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.




[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fcrackthecodeabhi%2Fkreds.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fcrackthecodeabhi%2Fkreds?ref=badge_large)
