package jp.thelow.chestShare.dao;

import java.util.UUID;
import java.util.function.Consumer;

import jp.thelow.chestShare.playerdata.PlayerDatData;
import jp.thelow.thelowSql.DataStore;
import jp.thelow.thelowSql.DataStoreFactory;

public class PlayerDatDataDao {

  private static DataStore<PlayerDatData> dataStore = DataStoreFactory.getDataStore(PlayerDatData.class);

  public void upsert(PlayerDatData playerDatData) {
    dataStore.updateInsert(playerDatData, r -> {
    });
  }

  public void select(UUID uuid, Consumer<PlayerDatData> consumer) {
    dataStore.getOneData(" UUID = ?", new Object[] { uuid.toString() }, c -> {
      consumer.accept(c.getResult());
    });
  }
}
