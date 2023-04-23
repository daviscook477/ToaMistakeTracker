package com.coxmistaketracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CoxMistakeTrackerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CoxMistakeTrackerPlugin.class);
		RuneLite.main(args);
	}
}