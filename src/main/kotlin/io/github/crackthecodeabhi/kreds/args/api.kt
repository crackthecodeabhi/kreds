package io.github.crackthecodeabhi.kreds.args

// Args for the public API

/**
 * The EXPIRE,EXPIREAT commands supports a set of options since Redis 7.0:
 *
 * NX -- Set expiry only when the key has no expiry
 *
 * XX -- Set expiry only when the key has an existing expiry
 *
 * GT -- Set expiry only when the new expiry is greater than current one
 *
 * LT -- Set expiry only when the new expiry is less than current one
 */
public enum class ExpireOption : Argument {
    NX, XX, GT, LT;

    override fun toString(): String = name
}

public typealias PExpireOption = ExpireOption

public class SetOption private constructor(
    public val exSeconds: ULong? = null,
    public val pxMilliseconds: ULong? = null,
    public val exatTimestamp: ULong? = null,
    public val pxatMillisecondTimestamp: ULong? = null,
    public val keepTTL: Boolean? = null,
    public val nx: Boolean? = null,
    public val xx: Boolean? = null,
    public val get: Boolean? = null
) {

    public data class Builder(
        var exSeconds: ULong? = null,
        var pxMilliseconds: ULong? = null,
        var exatTimestamp: ULong? = null,
        var pxatMillisecondTimestamp: ULong? = null,
        var keepTTL: Boolean? = null,
        var nx: Boolean? = null,
        var xx: Boolean? = null,
        var get: Boolean? = null
    ) {
        public fun exSeconds(exSeconds: ULong): Builder = apply { this.exSeconds = exSeconds }
        public fun pxMilliseconds(pxMilliseconds: ULong): Builder = apply { this.pxMilliseconds = pxMilliseconds }
        public fun exatTimestamp(exatTimestamp: ULong): Builder = apply { this.exatTimestamp = exatTimestamp }
        public fun pxatMillisecondTimestamp(pxatMillisecondTimestamp: ULong): Builder =
            apply { this.pxatMillisecondTimestamp = pxatMillisecondTimestamp }

        public fun keepTTL(keepTTL: Boolean): Builder = apply { this.keepTTL = keepTTL }
        public fun nx(nx: Boolean): Builder = apply { this.nx = nx }
        public fun xx(xx: Boolean): Builder = apply { this.xx = xx }
        public fun get(get: Boolean): Builder = apply { this.get = get }

        /**
         * @throws IllegalArgumentException in case the argument provided conflict
         */
        public fun build(): SetOption {
            if (listOfNotNull(exSeconds, pxMilliseconds, exatTimestamp, pxatMillisecondTimestamp, keepTTL).size > 1)
                throw IllegalArgumentException("Only one of the options (EX,PX,EXAT,PXAT,KEEPTTL) allowed or none.")
            if (listOfNotNull(nx, xx).size > 1)
                throw IllegalArgumentException("Either NX or XX are allowed or none.")
            return SetOption(exSeconds, pxMilliseconds, exatTimestamp, pxatMillisecondTimestamp, keepTTL, nx, xx, get)
        }
    }
}

public class GetExOption private constructor(
    public val exSeconds: ULong? = null,
    public val pxMilliseconds: ULong? = null,
    public val exatTimestamp: ULong? = null,
    public val pxatMillisecondTimestamp: ULong? = null,
    public val persist: Boolean? = null
) {

    public data class Builder(
        var exSeconds: ULong? = null,
        var pxMilliseconds: ULong? = null,
        var exatTimestamp: ULong? = null,
        var pxatMillisecondTimestamp: ULong? = null,
        var persist: Boolean? = null
    ) {
        public fun exSeconds(exSeconds: ULong): Builder = apply { this.exSeconds = exSeconds }
        public fun pxMilliseconds(pxMilliseconds: ULong): Builder = apply { this.pxMilliseconds = pxMilliseconds }
        public fun exatTimestamp(exatTimestamp: ULong): Builder = apply { this.exatTimestamp = exatTimestamp }
        public fun pxatMillisecondTimestamp(pxatMillisecondTimestamp: ULong): Builder =
            apply { this.pxatMillisecondTimestamp = pxatMillisecondTimestamp }

        public fun persist(persist: Boolean): Builder = apply { this.persist = persist }

        /**
         * @throws IllegalArgumentException in case the argument provided conflict
         */
        public fun build(): GetExOption {
            if (listOfNotNull(exSeconds, pxMilliseconds, exatTimestamp, pxatMillisecondTimestamp, persist).size > 1)
                throw IllegalArgumentException("Only one option is valid.")
            return GetExOption(exSeconds, pxMilliseconds, exatTimestamp, pxatMillisecondTimestamp, persist)
        }
    }
}


public enum class ClientListType : Argument {
    normal, master, replica, pubsub;

    override fun toString(): String = "TYPE $name"
}

public enum class ClientPauseOption : Argument {
    WRITE, ALL;

    override fun toString(): String = name
}

public enum class ClientReplyOption : Argument {
    ON, OFF, SKIP;

    override fun toString(): String = name
}

public enum class BeforeAfterOption : Argument {
    BEFORE, AFTER;

    override fun toString(): String = name
}

public enum class LeftRightOption : Argument {
    LEFT, RIGHT;

    override fun toString(): String = name
}

public enum class FailOverOption: Argument {
    FORCE,TAKEOVER;

    override fun toString(): String = name
}