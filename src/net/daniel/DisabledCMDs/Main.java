package net.daniel.DisabledCMDs;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	private final Set<String> disabledCMDS = new HashSet<>();

	private boolean sendDeny_msg = true;
	private boolean applyForOp = true;

	private String denyMSG =
			"§b§l[ §f§lServer §b§l] §cCommand %cmd% has been disabled by an administrator.";

	private String reloaded =
			"§b§l[ §f§lServer §b§l] §aConfiguration reloaded successfully.";

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		reloadConfiguration();

		getLogger().info("Plugin enabled.");
	}

	@Override
	public void onDisable() {
		getLogger().info("Plugin disabled.");
	}

	@Override
	public boolean onCommand(
			final CommandSender sender,
			final Command command,
			final String commandLabel,
			final String[] args) {

		if (commandLabel.equalsIgnoreCase("disabledcmds")
				|| commandLabel.equalsIgnoreCase("명령어제한리로드")) {

			if (sender.hasPermission("DisabledCMDs.reload")
					|| sender.isOp()) {

				reloadConfiguration();
				sender.sendMessage(reloaded);

			} else {
				sender.sendMessage(
						ChatColor.RED + "You do not have permission to use this command.");
			}

			return true;
		}

		return false;
	}

	public void reloadConfiguration() {
		saveDefaultConfig();
		reloadConfig();

		FileConfiguration config = getConfig();

		sendDeny_msg = config.getBoolean("send_deny_msg");
		applyForOp = config.getBoolean("also_apply_for_op");

		String denyMessage = config.getString("Deny_message");

		if (denyMessage != null && !denyMessage.isEmpty()) {
			denyMSG = ChatColor.translateAlternateColorCodes(
					'&', denyMessage);
		}

		String reloadMessage = config.getString("reloaded");

		if (reloadMessage != null && !reloadMessage.isEmpty()) {
			reloaded = ChatColor.translateAlternateColorCodes(
					'&', reloadMessage);
		}

		disabledCMDS.clear();

		List<String> commands =
				config.getStringList("Disabled-CMDs");

		for (String cmd : commands) {
			disabledCMDS.add(cmd.toLowerCase(Locale.ROOT));
		}
	}


	@EventHandler(priority = EventPriority.NORMAL)
	public void PreCommoand(PlayerCommandPreprocessEvent e) {

		String[] cmd = e.getMessage().toLowerCase()
				.replaceAll("^/([^ :]*:)?", "/")
				.split(" ");

		if (!disabledCMDS.contains(cmd[0])) {
			return;
		}

		Player p = e.getPlayer();

		if (!applyForOp && p.isOp()) {
			return;
		}

		e.setCancelled(true);

		if (!sendDeny_msg) {
			return;
		}

		p.sendMessage(
				denyMSG.replaceAll(
						"%cmd%",
						Matcher.quoteReplacement(cmd[0])
				)
		);
	}

}
