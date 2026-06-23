package com.nonsense.chat.data.cache;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserCacheDao_Impl implements UserCacheDao {
  private final RoomDatabase __db;

  private final EntityUpsertionAdapter<CachedUser> __upsertionAdapterOfCachedUser;

  public UserCacheDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__upsertionAdapterOfCachedUser = new EntityUpsertionAdapter<CachedUser>(new EntityInsertionAdapter<CachedUser>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `cached_users` (`id`,`doc`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedUser entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getDoc());
      }
    }, new EntityDeletionOrUpdateAdapter<CachedUser>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `cached_users` SET `id` = ?,`doc` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedUser entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getDoc());
        statement.bindString(3, entity.getId());
      }
    });
  }

  @Override
  public Object upsert(final List<CachedUser> items, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfCachedUser.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object get(final List<String> ids,
      final Continuation<? super List<CachedUser>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM cached_users WHERE id IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : ids) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CachedUser>>() {
      @Override
      @NonNull
      public List<CachedUser> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDoc = CursorUtil.getColumnIndexOrThrow(_cursor, "doc");
          final List<CachedUser> _result = new ArrayList<CachedUser>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedUser _item_1;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpDoc;
            _tmpDoc = _cursor.getString(_cursorIndexOfDoc);
            _item_1 = new CachedUser(_tmpId,_tmpDoc);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
