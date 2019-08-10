package services

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class LongArg(override val name: String = "Integer") : ArgumentType {
    companion object : LongArg()

    override val examples = arrayListOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent) =
            arg.toLongOrNull()?.let { ArgumentResult.Single(it) } ?:
            ArgumentResult.Error("Expected a long number, got $arg")
}
