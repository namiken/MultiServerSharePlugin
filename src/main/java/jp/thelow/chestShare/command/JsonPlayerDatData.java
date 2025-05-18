package jp.thelow.chestShare.command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.bukkit.Location;

import com.google.gson.annotations.SerializedName;

import jp.thelow.chestShare.util.CommonUtil;
import jp.thelow.thelowSql.annotation.Column;
import jp.thelow.thelowSql.annotation.Id;
import jp.thelow.thelowSql.annotation.Table;
import lombok.Data;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

@Table("PLAYER_DAT_DATA")
@Data
public class JsonPlayerDatData {
  @Id("UUID")
  @Column("UUID")
  private String uuid;

  @SerializedName("dat_data")
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

  @SerializedName("update_at")
  @Column("UPDATE_AT")
  private String updateAt;

  public Location getPlayerLocation() {
    return CommonUtil.getLocationByString(location);
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
