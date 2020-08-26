package jp.thelow.chestShare.domain;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface ShareData extends ConfigurationSerializable {

  void applyServer();
}
