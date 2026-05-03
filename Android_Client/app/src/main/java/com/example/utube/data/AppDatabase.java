package com.example.utube.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.utube.models.UserEntity;
import com.example.utube.models.VideoEntity;
import com.example.utube.models.CommentEntity;

@Database(entities = {UserEntity.class, VideoEntity.class, CommentEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract VideoDao videoDao();
    public abstract CommentDao commentDao();

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add the new server_id column to the comments table
            database.execSQL("ALTER TABLE comments ADD COLUMN server_id TEXT");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "utube_database")
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}