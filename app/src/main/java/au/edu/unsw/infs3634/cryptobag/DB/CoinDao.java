package au.edu.unsw.infs3634.cryptobag.DB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import au.edu.unsw.infs3634.cryptobag.API.Coin;

@Dao
public interface CoinDao {

  // Query and return all results currently in the DB
  @Query("SELECT * FROM Coin")
  List<Coin> getCoins();

  // Query and return ONE coin’s results based on the received symbol
  @Query("SELECT * FROM Coin WHERE symbol == :coinSymbol")
  Coin getCoin(String coinSymbol);

  // Use @Delete to delete an Array list of objects
  // "..." is a Java operator that accepts an object or array list of objects as parameter
    @Delete
    void deleteCoins(Coin... coins);

  // Use @Query to delete all objects from Coin entity
  @Query("DELETE FROM Coin")
  void deleteCoins();

  // Query to insert all received records into the DB
  @Insert
  void insertCoins(Coin... coins);

}
