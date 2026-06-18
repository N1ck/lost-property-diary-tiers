package com.lostpropertydiary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DiaryRewardTest
{
	@Test
	public void everyRewardHasFourTiersAndFourVarbits()
	{
		for (DiaryReward reward : DiaryReward.values())
		{
			assertEquals(reward + " should have 4 tier item ids", 4, reward.getTierItemIds().length);
			assertEquals(reward + " should have 4 tier varbits", 4, reward.getTierVarbits().length);
			assertTrue(reward + " should have a base item id", reward.getBaseItemId() > 0);
		}
	}

	@Test
	public void allItemIdsAreUniqueAndMapBackToTheirReward()
	{
		// Every base id and tier id must be distinct across all families, otherwise byItemId would
		// return the wrong reward for a shop slot.
		Map<Integer, DiaryReward> owner = new HashMap<>();
		for (DiaryReward reward : DiaryReward.values())
		{
			recordUnique(owner, reward.getBaseItemId(), reward);
			for (int tierId : reward.getTierItemIds())
			{
				recordUnique(owner, tierId, reward);
			}
		}

		// And the lookup the plugin actually uses must agree.
		for (Map.Entry<Integer, DiaryReward> e : owner.entrySet())
		{
			assertSame("byItemId mismatch for " + e.getKey(), e.getValue(), DiaryReward.byItemId(e.getKey()));
		}
	}

	private static void recordUnique(Map<Integer, DiaryReward> owner, int id, DiaryReward reward)
	{
		DiaryReward prev = owner.put(id, reward);
		assertNull("item id " + id + " is shared by " + prev + " and " + reward, prev);
	}

	@Test
	public void unknownItemIdReturnsNull()
	{
		assertNull(DiaryReward.byItemId(-1));
		assertNull(DiaryReward.byItemId(0));
		assertNull(DiaryReward.byItemId(Integer.MAX_VALUE));
	}

	@Test
	public void highestCompletedTierPicksTheTopCompletedTier()
	{
		DiaryReward karamja = DiaryReward.KARAMJA_GLOVES;
		int[] varbits = karamja.getTierVarbits();

		// nothing complete
		assertEquals(-1, karamja.highestCompletedTier(completed()));
		assertEquals(-1, karamja.highestCompletedItemId(completed()));

		// only easy
		assertEquals(0, karamja.highestCompletedTier(completed(varbits[0])));
		assertEquals(karamja.getTierItemIds()[0], karamja.highestCompletedItemId(completed(varbits[0])));

		// up to hard (easy+medium+hard set) -> tier 2
		assertEquals(2, karamja.highestCompletedTier(completed(varbits[0], varbits[1], varbits[2])));

		// elite -> tier 3
		assertEquals(3, karamja.highestCompletedTier(completed(varbits[0], varbits[1], varbits[2], varbits[3])));
		assertEquals(karamja.getTierItemIds()[3], karamja.highestCompletedItemId(completed(varbits[0], varbits[1], varbits[2], varbits[3])));
	}

	@Test
	public void onlyTheConfiguredVarbitsAreConsulted()
	{
		// A reward must not be influenced by another family's varbit being set.
		DiaryReward falador = DiaryReward.FALADOR_SHIELD;
		int someoneElsesVarbit = DiaryReward.ARDOUGNE_CLOAK.getTierVarbits()[3];
		assertEquals(-1, falador.highestCompletedTier(completed(someoneElsesVarbit)));
	}

	/** Returns a varbit resolver where the given varbit ids report value 1 and all others 0. */
	private static IntUnaryOperator completed(int... setVarbits)
	{
		Set<Integer> set = new HashSet<>();
		for (int v : setVarbits)
		{
			set.add(v);
		}
		return id -> set.contains(id) ? 1 : 0;
	}
}
