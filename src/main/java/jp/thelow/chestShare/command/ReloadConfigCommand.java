package jp.thelow.chestShare.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import jp.thelow.chestShare.Main;
import jp.thelow.chestShare.Properties;

public class ReloadConfigCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    Properties prooerties = Main.getInstance().getProoerties();
    prooerties.reload(Main.getInstance().getConfig());

    sender.sendMessage(prooerties.toString());
    return true;
  }
}
