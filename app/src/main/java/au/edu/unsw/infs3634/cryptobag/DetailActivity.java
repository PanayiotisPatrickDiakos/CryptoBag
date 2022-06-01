package au.edu.unsw.infs3634.cryptobag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
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

public class DetailActivity extends AppCompatActivity {
    public static final String INTENT_MESSAGE = "intent_message";
    private static final String TAG = "DetailActivity";
    private TextView mName;
    private TextView mSymbol;
    private TextView mValue;
    private TextView mChange1h;
    private TextView mChange24h;
    private TextView mChange7d;
    private TextView mMarketcap;
    private TextView mVolume;
    private ImageView mSearch;
    private ImageView mArt;
    private CoinDatabase mDb;
    private CheckBox mCoin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get handle for view elements
        mArt = findViewById(R.id.ivImage);

        mName = findViewById(R.id.tvName);
        mSymbol = findViewById(R.id.tvSymbol);
        mValue = findViewById(R.id.tvValueField);
        mChange1h = findViewById(R.id.tvChange1hField);
        mChange24h = findViewById(R.id.tvChange24hField);
        mChange7d = findViewById(R.id.tvChange7dField);
        mMarketcap = findViewById(R.id.tvMarketcapField);
        mVolume = findViewById(R.id.tvVolumeField);
        mSearch = findViewById(R.id.ivSearch);
        mCoin = findViewById(R.id.cbCoin);


        // Get the intent that started this activity
        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_MESSAGE)) {
            String coinSymbol = intent.getStringExtra(INTENT_MESSAGE);
            Log.d(TAG, "INTENT_MESSAGE = " + coinSymbol);

            // Create a CoinDatabase Object
            mDb = Room.databaseBuilder(getApplicationContext(), CoinDatabase.class, "coin-database")
                    .build();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    Coin coin = mDb.coinDao().getCoin(coinSymbol);
                    NumberFormat formatter = NumberFormat.getCurrencyInstance();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTitle(coin.getName());
                            mName.setText(coin.getName());
                            Glide.with(DetailActivity.this)
                                    .load("https://www.coinlore.com/img/" + coin.getNameid() + ".png")
                                    .fitCenter()
                                    .into(mArt);
                            mSymbol.setText(coin.getSymbol());
                            mValue.setText(formatter.format(Double.valueOf(coin.getPriceUsd())));
                            mChange1h.setText(String.valueOf(coin.getPercentChange1h()) + " %");
                            mChange24h.setText(String.valueOf(coin.getPercentChange24h()) + " %");
                            mChange7d.setText(String.valueOf(coin.getPercentChange7d()) + " %");
                            mMarketcap.setText(formatter.format(Double.valueOf(coin.getMarketCapUsd())));
                            mVolume.setText(formatter.format(coin.getVolume24()));
                            mSearch.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    searchCoin(coin.getName());
                                }
                            });
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference messageRef = database.getReference(FirebaseAuth.getInstance().getUid());
                            messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String result = (String) snapshot.getValue();
                                    if(result != null && result.equals(coin.getSymbol())) {
                                        mCoin.setChecked(true);
                                    } else {
                                        mCoin.setChecked(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            mCoin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    DatabaseReference messageRef = database.getReference(FirebaseAuth.getInstance().getUid());
                                    if(isChecked){
                                        messageRef.setValue(coin.getSymbol());
                                    } else {
                                        messageRef.setValue("");
                                    }
                                }
                            });

                        }
                    });
                }
            });

        }
    }

    private void searchCoin(String name) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + name));
        startActivity(intent);
    }

}