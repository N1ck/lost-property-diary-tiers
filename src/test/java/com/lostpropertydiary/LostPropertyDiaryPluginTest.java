package com.lostpropertydiary;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LostPropertyDiaryPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LostPropertyDiaryPlugin.class);
		RuneLite.main(args);
	}
}
