package com.perdudiary;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("perdudiary")
public interface PerduDiaryConfig extends Config
{
	@ConfigItem(
		keyName = "debugLogging",
		name = "Debug logging",
		description = "Log each reward the plugin swaps in Perdu's shop, with the tier it detected. Use this to verify item ids in-game.",
		position = 0
	)
	default boolean debugLogging()
	{
		return false;
	}
}
