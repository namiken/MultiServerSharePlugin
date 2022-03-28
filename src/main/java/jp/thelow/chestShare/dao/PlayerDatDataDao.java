package jp.thelow.chestShare.dao;

import java.util.UUID;
import java.util.function.Consumer;

import jp.thelow.chestShare.playerdata.PlayerDatData;
import jp.thelow.chestShare.util.BooleanConsumer;
import jp.thelow.thelowSql.DataStore;
import jp.thelow.thelowSql.DataStoreFactory;
import jp.thelow.thelowSql.database.ConnectionFactory;
import jp.thelow.thelowSql.database.dao.ThelowDao;

public class PlayerDatDataDao {

  private static ThelowDao thelowDao = new ThelowDao(PlayerDatData.class);

  private static DataStore<PlayerDatData> dataStore = DataStoreFactory.getDataStore(PlayerDatData.class);

  public void upsert(PlayerDatData playerDatData, BooleanConsumer callback) {
    dataStore.updateInsert(playerDatData, r -> callback.accept(!r.isError()));
  }

  public void upsertSync(PlayerDatData playerDatData) {
    thelowDao.updateInsert(playerDatData);
  }

  public void select(UUID uuid, Consumer<PlayerDatData> consumer) {
    dataStore.getOneData(" UUID = ?", new Object[] { uuid.toString() }, c -> {
      consumer.accept(c.getResult());
    });
  }

  public PlayerDatData syncSelect(UUID uuid) {
    try {
      return thelowDao.selectOne(" UUID = ?", new Object[] { uuid.toString() });
    } finally {
      ConnectionFactory.safeClose();
    }
  }
}
