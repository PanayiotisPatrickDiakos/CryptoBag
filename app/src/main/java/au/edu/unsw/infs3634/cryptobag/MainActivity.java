package au.edu.unsw.infs3634.cryptobag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import au.edu.unsw.infs3634.cryptobag.API.Coin;
import au.edu.unsw.infs3634.cryptobag.API.CoinLoreResponse;
import au.edu.unsw.infs3634.cryptobag.API.CoinService;
import au.edu.unsw.infs3634.cryptobag.DB.CoinDatabase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private RecyclerView mRecyclerView;
  private CoinAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private CoinDatabase mDb;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Get a handle to the RecyclerView
    mRecyclerView = findViewById(R.id.rvList);
    mRecyclerView.setHasFixedSize(true);

    // Instantiate a LinearLayoutManager
    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);

    // Implement ClickListener for list items
    CoinAdapter.RecyclerViewListener listener = new CoinAdapter.RecyclerViewListener() {
      @Override
      public void onClick(View view, String coinSymbol) {
        // Launch DetailActivity
        launchDetailActivity(coinSymbol);
      }
    };

    // Create an adapter instance with an empty ArrayList of Coin objects
    mAdapter = new CoinAdapter(new ArrayList<Coin>(), listener);
    mRecyclerView.setAdapter(mAdapter);

    // Initialise the database
    mDb = Room.databaseBuilder(getApplicationContext(), CoinDatabase.class, "coin-database")
            .build();
    // Create an asynchronous database call using Java Runnable to
    // get the list of coins from the database
    // Set the adapter using the result
    Executors.newSingleThreadExecutor().execute(new Runnable() {
      @Override
      public void run() {
        ArrayList<Coin> coins = (ArrayList<Coin>) mDb.coinDao().getCoins();
        mAdapter.setData(coins);
        mAdapter.sort(CoinAdapter.SORT_METHOD_NAME);
      }
    });

    // Implement Retrofit to make API call
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.coinlore.net") // Set the base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    // Create object for the service interface
    CoinService service = retrofit.create(CoinService.class);
    Call<CoinLoreResponse> responseCall = service.getResponse();
    responseCall.enqueue(new Callback<CoinLoreResponse>() {
      @Override
      public void onResponse(Call<CoinLoreResponse> call, Response<CoinLoreResponse> response) {
        Log.d(TAG, "API call successful!");
        List<Coin> coins = response.body().getData();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
          @Override
          public void run() {
            // Delete all rows currently in your coins entity in the Database
            mDb.coinDao().deleteCoins(mDb.coinDao().getCoins().toArray(new Coin[0]));
            // Add all rows from the List<Coin> you created from the HTTP request to the Database
            mDb.coinDao().insertCoins(coins.toArray(new Coin[0]));
          }
        });

        // Supply data to the adapter to be displayed
        mAdapter.setData((ArrayList)coins);
        mAdapter.sort(CoinAdapter.SORT_METHOD_NAME);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference messageRef = database.getReference(FirebaseAuth.getInstance().getUid());
        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            String result = (String) snapshot.getValue();
            if (result != null) {
              for(Coin coin : coins) {
                if(coin.getSymbol().equals(result)) {
                  Toast.makeText(MainActivity.this, coin.getName() + ": $" + coin.getPriceUsd(), Toast.LENGTH_LONG).show();
                }
              }
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {

          }
        });
      }

      @Override
      public void onFailure(Call<CoinLoreResponse> call, Throwable t) {
        Log.d(TAG, "API call failure.");
      }
    });
  }

  @Override
  // Instantiate the menu
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        mAdapter .getFilter().filter(query);
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        mAdapter.getFilter().filter(newText);
        return false;
      }
    });
    return true;
  }

  @Override
  // React to user interaction with the menu
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.sortName:
        mAdapter.sort(CoinAdapter.SORT_METHOD_NAME);
        return true;
      case R.id.sortValue:
        mAdapter.sort(CoinAdapter.SORT_METHOD_VALUE);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  // Called when user taps on a row on the RecyclerView
  private void launchDetailActivity(String message){
    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
    intent.putExtra(DetailActivity.INTENT_MESSAGE, message);
    startActivity(intent);
  }

}