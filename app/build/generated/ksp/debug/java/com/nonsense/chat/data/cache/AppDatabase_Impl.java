package com.nonsense.chat.data.cache;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ChatCacheDao _chatCacheDao;

  private volatile MessageCacheDao _messageCacheDao;

  private volatile UserCacheDao _userCacheDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_chats` (`id` TEXT NOT NULL, `doc` TEXT NOT NULL, `sortKey` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_messages` (`id` TEXT NOT NULL, `chatId` TEXT NOT NULL, `doc` TEXT NOT NULL, `sortKey` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_messages_chatId` ON `cached_messages` (`chatId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_messages_sortKey` ON `cached_messages` (`sortKey`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_users` (`id` TEXT NOT NULL, `doc` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b1cb3eb79c518ac00c1574a2273738fc')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `cached_chats`");
        db.execSQL("DROP TABLE IF EXISTS `cached_messages`");
        db.execSQL("DROP TABLE IF EXISTS `cached_users`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCachedChats = new HashMap<String, TableInfo.Column>(3);
        _columnsCachedChats.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedChats.put("doc", new TableInfo.Column("doc", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedChats.put("sortKey", new TableInfo.Column("sortKey", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedChats = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedChats = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedChats = new TableInfo("cached_chats", _columnsCachedChats, _foreignKeysCachedChats, _indicesCachedChats);
        final TableInfo _existingCachedChats = TableInfo.read(db, "cached_chats");
        if (!_infoCachedChats.equals(_existingCachedChats)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_chats(com.nonsense.chat.data.cache.CachedChat).\n"
                  + " Expected:\n" + _infoCachedChats + "\n"
                  + " Found:\n" + _existingCachedChats);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedMessages = new HashMap<String, TableInfo.Column>(4);
        _columnsCachedMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("chatId", new TableInfo.Column("chatId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("doc", new TableInfo.Column("doc", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("sortKey", new TableInfo.Column("sortKey", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedMessages = new HashSet<TableInfo.Index>(2);
        _indicesCachedMessages.add(new TableInfo.Index("index_cached_messages_chatId", false, Arrays.asList("chatId"), Arrays.asList("ASC")));
        _indicesCachedMessages.add(new TableInfo.Index("index_cached_messages_sortKey", false, Arrays.asList("sortKey"), Arrays.asList("ASC")));
        final TableInfo _infoCachedMessages = new TableInfo("cached_messages", _columnsCachedMessages, _foreignKeysCachedMessages, _indicesCachedMessages);
        final TableInfo _existingCachedMessages = TableInfo.read(db, "cached_messages");
        if (!_infoCachedMessages.equals(_existingCachedMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_messages(com.nonsense.chat.data.cache.CachedMessage).\n"
                  + " Expected:\n" + _infoCachedMessages + "\n"
                  + " Found:\n" + _existingCachedMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedUsers = new HashMap<String, TableInfo.Column>(2);
        _columnsCachedUsers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("doc", new TableInfo.Column("doc", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCachedUsers = new TableInfo("cached_users", _columnsCachedUsers, _foreignKeysCachedUsers, _indicesCachedUsers);
        final TableInfo _existingCachedUsers = TableInfo.read(db, "cached_users");
        if (!_infoCachedUsers.equals(_existingCachedUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_users(com.nonsense.chat.data.cache.CachedUser).\n"
                  + " Expected:\n" + _infoCachedUsers + "\n"
                  + " Found:\n" + _existingCachedUsers);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "b1cb3eb79c518ac00c1574a2273738fc", "aba800dd482aa4a91e70876aecc65691");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "cached_chats","cached_messages","cached_users");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `cached_chats`");
      _db.execSQL("DELETE FROM `cached_messages`");
      _db.execSQL("DELETE FROM `cached_users`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ChatCacheDao.class, ChatCacheDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MessageCacheDao.class, MessageCacheDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserCacheDao.class, UserCacheDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ChatCacheDao chatCacheDao() {
    if (_chatCacheDao != null) {
      return _chatCacheDao;
    } else {
      synchronized(this) {
        if(_chatCacheDao == null) {
          _chatCacheDao = new ChatCacheDao_Impl(this);
        }
        return _chatCacheDao;
      }
    }
  }

  @Override
  public MessageCacheDao messageCacheDao() {
    if (_messageCacheDao != null) {
      return _messageCacheDao;
    } else {
      synchronized(this) {
        if(_messageCacheDao == null) {
          _messageCacheDao = new MessageCacheDao_Impl(this);
        }
        return _messageCacheDao;
      }
    }
  }

  @Override
  public UserCacheDao userCacheDao() {
    if (_userCacheDao != null) {
      return _userCacheDao;
    } else {
      synchronized(this) {
        if(_userCacheDao == null) {
          _userCacheDao = new UserCacheDao_Impl(this);
        }
        return _userCacheDao;
      }
    }
  }
}
