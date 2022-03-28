package jp.thelow.chestShare.playerdata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import jp.thelow.chestShare.util.CommonUtil;
import jp.thelow.thelowSql.annotation.Column;
import jp.thelow.thelowSql.annotation.Id;
import jp.thelow.thelowSql.annotation.Table;
import lombok.Data;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

@Table("PLAYER_DAT_DATA")
@Data
public class PlayerDatData {
  @Id("UUID")
  @Column("UUID")
  private String uuid;

  @Column("DAT_DATA")
  private String datData;

  @Column("MP")
  private int mp;

  @Column("HP")
  private double hp;

  @Column("LOCATION")
  private String location;

  @Column("SERVER")
  private String server;

  @Column("UPDATE_AT")
  private Timestamp updateAt;

  public Location getPlayerLocation() {
    return CommonUtil.getLocationByString(location);
  }

  public static PlayerDatData fromEntity(Player p, Location loc) {
    PlayerDatData playerDatData = new PlayerDatData();
    playerDatData.setUuid(p.getUniqueId().toString());
    playerDatData.setHp(p.getHealth());
    playerDatData.setMp(p.getLevel());
    playerDatData.setLocation(CommonUtil.getLocationString3(loc));
    playerDatData.setUpdateAt(Timestamp.valueOf(LocalDateTime.now()));

    return playerDatData;
  }

  public NBTTagCompound toNbtCompound() throws IOException {
    Decoder decoder = Base64.getDecoder();
    byte[] decode = decoder.decode(getDatData());
    ByteArrayInputStream inputStream = new ByteArrayInputStream(decode);
    return NBTCompressedStreamTools.a(inputStream);
  }

  public NBTTagCompound toNbtCompoundIgnoreException() {
    try {
      return toNbtCompound();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
