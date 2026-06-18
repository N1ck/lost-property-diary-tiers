package com.perdudiary;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PerduDiaryPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PerduDiaryPlugin.class);
		RuneLite.main(args);
	}
}
