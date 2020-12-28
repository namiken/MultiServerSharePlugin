package jp.thelow.chestShare;

import org.bukkit.configuration.file.FileConfiguration;

public class Properties {

  private String readFileDir;

  private String writeFileDir;

  private boolean isDebug;

  private boolean isDungeonOnly;

  private boolean isAutoMoveServer;

  private String overworldServer;

  public Properties(FileConfiguration config) {
    reload(config);
  }

  public String getReadFileDir() {
    return readFileDir;
  }

  public void setReadFileDir(String readFileDir) {
    this.readFileDir = readFileDir;
  }

  public String getWriteFileDir() {
    return writeFileDir;
  }

  public void setWriteFileDir(String writeFileDir) {
    this.writeFileDir = writeFileDir;
  }

  public boolean isDebug() {
    return isDebug;
  }

  public void setDebug(boolean isDebug) {
    this.isDebug = isDebug;
  }

  public boolean isDungeonOnly() {
    return isDungeonOnly;
  }

  public void setDungeonOnly(boolean isDungeonOnly) {
    this.isDungeonOnly = isDungeonOnly;
  }

  public String getOverworldServer() {
    return overworldServer;
  }

  public void setOverworldServer(String overworldServer) {
    this.overworldServer = overworldServer;
  }

  public boolean isAutoMoveServer() {
    return isAutoMoveServer;
  }

  public void setAutoMoveServer(boolean isAutoMoveServer) {
    this.isAutoMoveServer = isAutoMoveServer;
  }

  public void reload(FileConfiguration config) {
    readFileDir = config.getString("read-file-dir");
    writeFileDir = config.getString("write-file-dir");
    isDebug = config.getBoolean("debug");
    isDungeonOnly = config.getBoolean("dungeon-only");
    overworldServer = config.getString("overworld-server");
    isAutoMoveServer = config.getBoolean("auto-move-server");
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Properties [readFileDir=");
    builder.append(readFileDir);
    builder.append(", writeFileDir=");
    builder.append(writeFileDir);
    builder.append(", isDebug=");
    builder.append(isDebug);
    builder.append(", isDungeonOnly=");
    builder.append(isDungeonOnly);
    builder.append(", isAutoMoveServer=");
    builder.append(isAutoMoveServer);
    builder.append(", overworldServer=");
    builder.append(overworldServer);
    builder.append("]");
    return builder.toString();
  }

}
