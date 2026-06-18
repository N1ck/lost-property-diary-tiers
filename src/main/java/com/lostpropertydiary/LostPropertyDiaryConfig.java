package com.lostpropertydiary;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lostpropertydiary")
public interface LostPropertyDiaryConfig extends Config
{
	@ConfigItem(
		keyName = "debugLogging",
		name = "Debug logging",
		description = "Log each reward the plugin swaps in the Lost Property shop, with the tier it detected. Use this to verify item ids in-game.",
		position = 0
	)
	default boolean debugLogging()
	{
		return false;
	}
}
