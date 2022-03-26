package jp.thelow.chestShare.playerdata;

import java.sql.Timestamp;

import org.bukkit.Location;

import jp.thelow.chestShare.util.CommonUtil;
import jp.thelow.thelowSql.annotation.Column;
import jp.thelow.thelowSql.annotation.Id;
import jp.thelow.thelowSql.annotation.Table;
import lombok.Data;

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
}
