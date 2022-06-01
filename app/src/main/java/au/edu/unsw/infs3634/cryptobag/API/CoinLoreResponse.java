package au.edu.unsw.infs3634.cryptobag.API;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CoinLoreResponse {

  @SerializedName("data")
  @Expose
  private List<Coin> data = null;
  @SerializedName("info")
  @Expose
  private Info info;

  public List<Coin> getData() {
    return data;
  }

  public void setData(List<Coin> data) {
    this.data = data;
  }

  public Info getInfo() {
    return info;
  }

  public void setInfo(Info info) {
    this.info = info;
  }

}