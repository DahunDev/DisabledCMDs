package net.daniel.DisabledCMDs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin

		implements Listener {

	protected HashSet<String> disabledCMDS = new HashSet<String>();

	protected boolean sendDeny_msg = true;
	protected boolean applyForOp = true;
	protected String denyMSG = "§b§l[ §f§lServer §b§l] §c명령어 %cmd%(은)는 관리자에 의해 비활성화 되어있습니다.";
	protected String reloaded = "§b§l[ §f§lServer §b§l] §a성공적으로 설정을 재로드 하였습니다.";

	@Override
	public void onDisable() {
		PluginDescriptionFile pdFile = this.getDescription();
		System.out.println(
				String.valueOf(String.valueOf(pdFile.getName())) + " " + pdFile.getVersion() + " 이(가) 비활성화 되었습니다.");
	}

	@Override
	public void onEnable() {

		PluginDescriptionFile pdFile = this.getDescription();
		Bukkit.getPluginManager().registerEvents((Listener) this, (Plugin) this);

		this.reloadConfiguration();

		System.out.println(
				String.valueOf(String.valueOf(pdFile.getName())) + " " + pdFile.getVersion() + " 이(가) 활성화 되었습니다.");

	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel,
			final String[] args) {
		if (commandLabel.equalsIgnoreCase("disabledcmds") || commandLabel.equalsIgnoreCase("명령어제한리로드")) {
			if (sender.hasPermission("DisabledCMDs.reload") || sender.isOp()) {
				this.reloadConfiguration();
				sender.sendMessage(reloaded);
			}

			return false;
		}
		return false;
	}

	public void reloadConfiguration() {

		PluginDescriptionFile pdFile = this.getDescription();
		File config = new File("plugins/" + pdFile.getName() + "/config.yml");
		if (config.exists()) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(config);
			this.saveDefaultConfig();
			for (String key : cfg.getConfigurationSection("").getKeys(true)) {
				if (!this.getConfig().contains(key)) {
					this.getConfig().set(key, cfg.get(key));
				}
			}
		} else {
			this.saveDefaultConfig();
		}
		this.reloadConfig();

		this.sendDeny_msg = this.getConfig().getBoolean("send_deny_msg");
		this.applyForOp = this.getConfig().getBoolean("also_apply_for_op");

		if (!getConfig().getString("Deny_message").isEmpty()) {
			this.denyMSG = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("Deny_message"));

		}
		if (!getConfig().getString("reloaded").isEmpty()) {
			this.reloaded = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("reloaded"));

		}

		disabledCMDS.removeAll(disabledCMDS);

		List<String> temp = getConfig().getStringList("Disabled-CMDs");

		for (String cmd : temp) {
			disabledCMDS.add(cmd.toLowerCase());

		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void PreCommoand(PlayerCommandPreprocessEvent e) {

		String[] cmd = e.getMessage().toLowerCase().replaceAll("^/([^ :]*:)?", "/").split(" ");
		String[] input = e.getMessage().toLowerCase().replaceAll("^/([^ :]*:)?", "/").split(" ");

		if (disabledCMDS.contains(cmd[0])) {
			Player p = e.getPlayer();
			if (applyForOp) {
				e.setCancelled(true);
				if (sendDeny_msg) {
					p.sendMessage(denyMSG.replaceAll("%cmd%", Matcher.quoteReplacement(input[0])));
				}

			} else {

				if (!p.isOp()) {
					e.setCancelled(true);
					if (sendDeny_msg) {
						p.sendMessage(denyMSG.replaceAll("%cmd%", Matcher.quoteReplacement(input[0])));
					}

				}

			}

		}

	}

}
