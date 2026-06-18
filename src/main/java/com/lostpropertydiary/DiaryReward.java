package com.lostpropertydiary;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Varbits;

/**
 * One achievement-diary reward family (e.g. Karamja gloves), holding the completion varbit and the
 * item id for each of its four tiers. Tiers are 0-indexed internally: 0 = easy/1, 3 = elite/4.
 *
 * <p>{@code baseItemId} is the generic, pre-tier item id that the Lost Property shop displays
 * for this reward. It always shows that base icon regardless of which tier you've completed, which
 * is what this plugin corrects.
 *
 * <p>The completion varbits come from {@link net.runelite.api.Varbits}; the item ids were pulled
 * from the OSRS Wiki and confirmed in-game. If any reward shows the wrong tier, enable debug
 * logging in the config to print the shop slots and verify these.
 */
@Getter
enum DiaryReward
{
	KARAMJA_GLOVES(1686,
		new int[]{Varbits.DIARY_KARAMJA_EASY, Varbits.DIARY_KARAMJA_MEDIUM, Varbits.DIARY_KARAMJA_HARD, Varbits.DIARY_KARAMJA_ELITE},
		new int[]{11136, 11138, 11140, 13103}),
	ARDOUGNE_CLOAK(770,
		new int[]{Varbits.DIARY_ARDOUGNE_EASY, Varbits.DIARY_ARDOUGNE_MEDIUM, Varbits.DIARY_ARDOUGNE_HARD, Varbits.DIARY_ARDOUGNE_ELITE},
		new int[]{13121, 13122, 13123, 13124}),
	FALADOR_SHIELD(762,
		new int[]{Varbits.DIARY_FALADOR_EASY, Varbits.DIARY_FALADOR_MEDIUM, Varbits.DIARY_FALADOR_HARD, Varbits.DIARY_FALADOR_ELITE},
		new int[]{13117, 13118, 13119, 13120}),
	FREMENNIK_SEA_BOOTS(10510,
		new int[]{Varbits.DIARY_FREMENNIK_EASY, Varbits.DIARY_FREMENNIK_MEDIUM, Varbits.DIARY_FREMENNIK_HARD, Varbits.DIARY_FREMENNIK_ELITE},
		new int[]{13129, 13130, 13131, 13132}),
	KANDARIN_HEADGEAR(6450,
		new int[]{Varbits.DIARY_KANDARIN_EASY, Varbits.DIARY_KANDARIN_MEDIUM, Varbits.DIARY_KANDARIN_HARD, Varbits.DIARY_KANDARIN_ELITE},
		new int[]{13137, 13138, 13139, 13140}),
	DESERT_AMULET(5573,
		new int[]{Varbits.DIARY_DESERT_EASY, Varbits.DIARY_DESERT_MEDIUM, Varbits.DIARY_DESERT_HARD, Varbits.DIARY_DESERT_ELITE},
		new int[]{13133, 13134, 13135, 13136}),
	EXPLORERS_RING(5095,
		new int[]{Varbits.DIARY_LUMBRIDGE_EASY, Varbits.DIARY_LUMBRIDGE_MEDIUM, Varbits.DIARY_LUMBRIDGE_HARD, Varbits.DIARY_LUMBRIDGE_ELITE},
		new int[]{13125, 13126, 13127, 13128}),
	MORYTANIA_LEGS(5093,
		new int[]{Varbits.DIARY_MORYTANIA_EASY, Varbits.DIARY_MORYTANIA_MEDIUM, Varbits.DIARY_MORYTANIA_HARD, Varbits.DIARY_MORYTANIA_ELITE},
		new int[]{13112, 13113, 13114, 13115}),
	VARROCK_ARMOUR(5087,
		new int[]{Varbits.DIARY_VARROCK_EASY, Varbits.DIARY_VARROCK_MEDIUM, Varbits.DIARY_VARROCK_HARD, Varbits.DIARY_VARROCK_ELITE},
		new int[]{13104, 13105, 13106, 13107}),
	WILDERNESS_SWORD(3981,
		new int[]{Varbits.DIARY_WILDERNESS_EASY, Varbits.DIARY_WILDERNESS_MEDIUM, Varbits.DIARY_WILDERNESS_HARD, Varbits.DIARY_WILDERNESS_ELITE},
		new int[]{13108, 13109, 13110, 13111}),
	WESTERN_BANNER(3983,
		new int[]{Varbits.DIARY_WESTERN_EASY, Varbits.DIARY_WESTERN_MEDIUM, Varbits.DIARY_WESTERN_HARD, Varbits.DIARY_WESTERN_ELITE},
		new int[]{13141, 13142, 13143, 13144}),
	RADAS_BLESSING(22803,
		new int[]{Varbits.DIARY_KOUREND_EASY, Varbits.DIARY_KOUREND_MEDIUM, Varbits.DIARY_KOUREND_HARD, Varbits.DIARY_KOUREND_ELITE},
		new int[]{22941, 22943, 22945, 22947});

	private final int baseItemId;
	private final int[] tierVarbits;
	private final int[] tierItemIds;

	DiaryReward(int baseItemId, int[] tierVarbits, int[] tierItemIds)
	{
		this.baseItemId = baseItemId;
		this.tierVarbits = tierVarbits;
		this.tierItemIds = tierItemIds;
	}

	private static final Map<Integer, DiaryReward> BY_ITEM_ID = new HashMap<>();

	static
	{
		for (DiaryReward reward : values())
		{
			// Map the shop's base id and every tier id back to this family, so a slot is matched
			// whether it shows the base icon (the usual case) or an already-tiered icon.
			BY_ITEM_ID.put(reward.baseItemId, reward);
			for (int itemId : reward.tierItemIds)
			{
				BY_ITEM_ID.put(itemId, reward);
			}
		}
	}

	/**
	 * @return the reward family that {@code itemId} belongs to (its base id or any tier id), or
	 * {@code null} if the id is not a tracked diary reward.
	 */
	static DiaryReward byItemId(int itemId)
	{
		return BY_ITEM_ID.get(itemId);
	}

	int highestCompletedTier(Client client)
	{
		return highestCompletedTier(client::getVarbitValue);
	}

	/**
	 * @param varbitValue resolves a varbit id to its current value (a tier is complete when &gt; 0)
	 * @return the 0-indexed highest tier the player has completed (0 = easy .. 3 = elite), or -1 if
	 * no tier of this diary is complete.
	 */
	int highestCompletedTier(IntUnaryOperator varbitValue)
	{
		int highest = -1;
		for (int i = 0; i < tierVarbits.length; i++)
		{
			if (varbitValue.applyAsInt(tierVarbits[i]) > 0)
			{
				highest = i;
			}
		}
		return highest;
	}

	int highestCompletedItemId(Client client)
	{
		return highestCompletedItemId(client::getVarbitValue);
	}

	/**
	 * @return the item id of the highest completed tier, or -1 if no tier is complete.
	 */
	int highestCompletedItemId(IntUnaryOperator varbitValue)
	{
		int tier = highestCompletedTier(varbitValue);
		return tier < 0 ? -1 : tierItemIds[tier];
	}
}
