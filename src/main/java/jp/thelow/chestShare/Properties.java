package jp.thelow.chestShare;

import org.bukkit.configuration.file.FileConfiguration;

public class Properties {

  private String readFileDir;

  private String writeFileDir;

  private boolean isDebug;

  public Properties(FileConfiguration config) {
    readFileDir = config.getString("read-file-dir");
    writeFileDir = config.getString("write-file-dir");
    isDebug = config.getBoolean("debug");
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

}
