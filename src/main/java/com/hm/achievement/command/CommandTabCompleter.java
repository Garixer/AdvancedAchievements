package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.Reloadable;

/**
 * Class in charge of handling auto-completion for achievements and categories when using /aach check, /aach reset,
 * /aach give or /aach delete commands.
 * 
 * @author Pyves
 *
 */
public class CommandTabCompleter implements TabCompleter, Reloadable {

	private static final int MAX_LIST_LENGTH = 50;

	private final Set<String> enabledCategories;
	private final AdvancedAchievements plugin;

	private Set<String> configCommandsKeys;

	public CommandTabCompleter(AdvancedAchievements plugin) {
		enabledCategories = new HashSet<>(
				MultipleAchievements.values().length + NormalAchievements.values().length + 1);
		for (MultipleAchievements category : MultipleAchievements.values()) {
			enabledCategories.add(category.toString());
		}
		for (NormalAchievements category : NormalAchievements.values()) {
			enabledCategories.add(category.toString());
		}
		enabledCategories.add("Commands");
		// Only auto-complete with non-disabled categories.
		enabledCategories.removeAll(plugin.getDisabledCategorySet());

		this.plugin = plugin;
	}

	@Override
	public void extractConfigurationParameters() {
		configCommandsKeys = plugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!"aach".equals(command.getName()) || args.length == 3) {
			// Complete with players.
			return null;
		} else if (args.length == 2 && "reset".equalsIgnoreCase(args[0])) {
			return getPartialList(enabledCategories, args[1]);
		} else if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
			return getPartialList(configCommandsKeys, args[1]);
		} else if (args.length == 2 && ("delete".equalsIgnoreCase(args[0]) || "check".equalsIgnoreCase(args[0]))) {
			return getPartialList(plugin.getAchievementsAndDisplayNames().keySet(), args[1]);
		}
		// No completion.
		List<String> list = new ArrayList<>(1);
		list.add("");
		return list;
	}

	/**
	 * Returns a partial list based on the input set. Members of the returned list must start with what the player has
	 * types so far. The list also has a limited length to avoid filling the player's screen.
	 * 
	 * @param fullSet
	 * @param prefix
	 * @return a list limited in length, containing elements matching the prefix,
	 */
	private List<String> getPartialList(Set<String> fullSet, String prefix) {
		// Sort matching elements by alphabetical order.
		List<String> fullList = fullSet.stream().filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
				.map(s -> StringUtils.replace(s, " ", "\u2423")).sorted().collect(Collectors.toList());

		if (fullList.size() > MAX_LIST_LENGTH) {
			List<String> partialList = fullList.subList(0, MAX_LIST_LENGTH - 2);
			// Suspension points to show that list was truncated.
			partialList.add("\u2022\u2022\u2022");
			return partialList;
		}
		return fullList;
	}
}
