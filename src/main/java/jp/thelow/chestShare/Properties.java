package jp.thelow.chestShare;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;

import lombok.Data;
import lombok.Getter;

@Data
public class Properties {

  private String readFileDir;

  private String writeFileDir;

  private boolean isDebug;

  private boolean isDungeonOnly;

  private boolean isAutoMoveServer;

  private String overworldServer;

  @Getter
  private boolean autoSave;

  @Getter
  private List<UUID> allowUuid;

  public Properties(FileConfiguration config) {
    reload(config);
  }

  public void reload(FileConfiguration config) {
    readFileDir = config.getString("read-file-dir");
    writeFileDir = config.getString("write-file-dir");
    isDebug = config.getBoolean("debug");
    isDungeonOnly = config.getBoolean("dungeon-only");
    overworldServer = config.getString("overworld-server");
    isAutoMoveServer = config.getBoolean("auto-move-server");
    autoSave = config.getBoolean("auto-save");
    allowUuid = config.getStringList("allow-uuid").stream().map(UUID::fromString).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "Properties [readFileDir=" + readFileDir + ", writeFileDir=" + writeFileDir + ", isDebug=" + isDebug
        + ", isDungeonOnly=" + isDungeonOnly + ", isAutoMoveServer=" + isAutoMoveServer + ", overworldServer="
        + overworldServer + ", autoSave=" + autoSave + ", allowUuid=" + allowUuid + "]";
  }

}
