package au.edu.unsw.infs3634.cryptobag.DB;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import au.edu.unsw.infs3634.cryptobag.API.Coin;

@Database(entities = {Coin.class}, version = 1)
public abstract class CoinDatabase extends RoomDatabase {
  public abstract CoinDao coinDao();
}
