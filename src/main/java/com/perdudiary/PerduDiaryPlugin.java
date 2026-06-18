package com.perdudiary;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Perdu Diary Icons",
	description = "Shows the correct achievement diary reward tier in Perdu's reclaim shop instead of always the easy tier",
	tags = {"perdu", "diary", "achievement", "shop", "reclaim", "lost", "property"}
)
public class PerduDiaryPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PerduDiaryConfig config;

	private boolean shopOpen;

	@Override
	protected void shutDown()
	{
		shopOpen = false;
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.LOST_PROPERTY)
		{
			shopOpen = true;
			applyDiaryIcons();
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == InterfaceID.LOST_PROPERTY)
		{
			shopOpen = false;
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		// While the shop is open, re-apply after every script. The interface fills its slots
		// incrementally over several scripts and re-writes them with the base icons, so a one-shot
		// apply would either run too early (before the items exist) or get overwritten. Re-applying
		// on each script's completion corrects the slots within the same cycle the populate runs,
		// before that frame renders, so there's no flash and it copes with the incremental fill.
		// It's a no-op (a cheap boolean check) whenever the shop isn't open.
		if (shopOpen)
		{
			applyDiaryIcons();
		}
	}

	private void applyDiaryIcons()
	{
		Widget list = client.getWidget(InterfaceID.LostProperty.LIST);
		if (list == null)
		{
			return;
		}

		Widget[] slots = list.getDynamicChildren();
		if (slots == null)
		{
			return;
		}

		for (Widget slot : slots)
		{
			int displayedId = slot.getItemId();
			DiaryReward reward = DiaryReward.byItemId(displayedId);
			if (reward == null)
			{
				continue;
			}

			int correctId = reward.highestCompletedItemId(client);
			if (correctId != -1 && correctId != displayedId)
			{
				slot.setItemId(correctId);
				// The shop labels the slot with the generic base name (e.g. "Karamja gloves"),
				// which doesn't update with the icon, so set it to match the tier we're showing,
				// using the same "<col=ff9040>" formatting the shop uses for its slot names.
				slot.setName("<col=ff9040>" + client.getItemDefinition(correctId).getName());

				if (config.debugLogging())
				{
					log.info("Swapped {} from id {} to tier id {}", reward, displayedId, correctId);
				}
			}
		}
	}

	@Provides
	PerduDiaryConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PerduDiaryConfig.class);
	}
}
