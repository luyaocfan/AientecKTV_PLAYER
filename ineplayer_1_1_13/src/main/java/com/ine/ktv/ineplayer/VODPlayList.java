package com.ine.ktv.ineplayer;


import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class VODPlayList {
    public static final int Song_Ordered = 1;
    public static final int Song_Ready = 2;
    public static final int Song_Playing = 3;
    public static final int Song_Ended = 4;
    public static final int Song_DisplayNext = 9;
    public static class OrderPlayItem{
        private int Id;
        private int SongId;
        private String SongName;
        private String Singer;
        private String SongImg;
        private String SongFile;
        private int PlayStatus; //狀態1-未播放2-待播放3-播放中4-已播畢
        private String PlayStatusName;
        public OrderPlayItem(int id, int songId, String songName, String singer, String songImg, String songFile, int playStatus, String playStatusName) {
            Id = id;
            SongId = songId;
            SongName = songName;
            Singer = singer;
            SongImg = songImg;
            SongFile = songFile;
            PlayStatus = playStatus;
            PlayStatusName = playStatusName;
        }

        public int getId() {
            return Id;
        }

        public void setId(int id) {
            Id = id;
        }

        public int getSongId() {
            return SongId;
        }

        public void setSongId(int songId) {
            SongId = songId;
        }

        public String getSongName() {
            return SongName;
        }

        public void setSongName(String songName) {
            SongName = songName;
        }

        public String getSinger() {
            return Singer;
        }

        public void setSinger(String singer) {
            Singer = singer;
        }

        public String getSongImg() {
            return SongImg;
        }

        public void setSongImg(String songImg) {
            SongImg = songImg;
        }

        public String getSongFile() {
            return SongFile;
        }

        public void setSongFile(String songFile) {
            SongFile = songFile;
        }

        public int getPlayStatus() {
            return PlayStatus;
        }

        public void setPlayStatus(int playStatus) {
            PlayStatus = playStatus;
        }

        public String getPlayStatusName() {
            return PlayStatusName;
        }

        public void setPlayStatusName(String playStatusName) {
            PlayStatusName = playStatusName;
        }
    }
    public static class PublicPlayItem{
        private int Id;
        private int SongId;
        private String SongName;
        private String Singer;
        private String SongImg;
        private String SongFile;

        public PublicPlayItem(int id, int songId, String songName, String singer, String songImg, String songFile) {
            Id = id;
            SongId = songId;
            SongName = songName;
            Singer = singer;
            SongImg = songImg;
            SongFile = songFile;
        }

        public int getId() {
            return Id;
        }

        public void setId(int id) {
            Id = id;
        }

        public int getSongId() {
            return SongId;
        }

        public void setSongId(int songId) {
            SongId = songId;
        }

        public String getSongName() {
            return SongName;
        }

        public void setSongName(String songName) {
            SongName = songName;
        }

        public String getSinger() {
            return Singer;
        }

        public void setSinger(String singer) {
            Singer = singer;
        }

        public String getSongImg() {
            return SongImg;
        }

        public void setSongImg(String songImg) {
            SongImg = songImg;
        }

        public String getSongFile() {
            return SongFile;
        }

        public void setSongFile(String songFile) {
            SongFile = songFile;
        }
    }
    private static class OrderPlayListResponse{
        private String Status;
        private String Message;
        private OrderPlayItem[] SongList;
        public OrderPlayListResponse(String status, String message, OrderPlayItem[] songList) {
            Status = status;
            Message = message;
            SongList = songList;
        }

        public String getStatus() {
            return Status;
        }

        public void setStatus(String status) {
            Status = status;
        }

        public String getMessage() {
            return Message;
        }

        public void setMessage(String message) {
            Message = message;
        }

        public OrderPlayItem[] getPlayItems() {
            return SongList;
        }

        public void setPlayItems(OrderPlayItem[] playItems) {
            SongList = playItems;
        }
    }
    private static class PublicPlayListResponse{
        private String Status;
        private String Message;
        private PublicPlayItem[] SongList;
        public PublicPlayListResponse(String status, String message, PublicPlayItem[] songList) {
            Status = status;
            Message = message;
            SongList = songList;
        }

        public String getStatus() {
            return Status;
        }

        public void setStatus(String status) {
            Status = status;
        }

        public String getMessage() {
            return Message;
        }

        public void setMessage(String message) {
            Message = message;
        }

        public PublicPlayItem[] getPlayItems() {
            return SongList;
        }

        public void setPlayItems(PublicPlayItem[] playItems) {
            SongList = playItems;
        }
    }
    public interface EventListener {
        void onOrderSongData(VODPlayList sender, OrderPlayItem[] newItems);
        void onPublicSongData(VODPlayList sender);
    }
    private ArrayList<OrderPlayItem> orderSongItems = new ArrayList<>();
    private ArrayList<PublicPlayItem> publicSongItems = new ArrayList<>();
    private Context context;
    private int boxId;
    private String APIServer;
    private String token = "xxx";
    private RequestQueue queue;
    protected EventListener listener;
    public VODPlayList(Context context, int boxId, String apiServer, EventListener listener) {
        this.context = context;
        this.boxId = boxId;
        this.APIServer = apiServer;
        this.listener = listener;
        queue = Volley.newRequestQueue(context);
    }
    public ArrayList<OrderPlayItem> getOrderSongItems() {
        return orderSongItems;
    }

    public ArrayList<PublicPlayItem> getPublicSongItems() {
        return publicSongItems;
    }
    public void SetSongPlayStatus(int id, int status) {
        String url;
        if(status == Song_Playing)
            url = APIServer+"SetSongPlaying";
        else
            url = APIServer+"PlayNextSong";
        HashMap data = new HashMap();
        data.put("token",token);
        data.put("boxId",boxId);
        data.put("playId",id);
        JsonObjectRequest jsonObjectRequest  = new JsonObjectRequest (Request.Method.POST, url, new JSONObject(data),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // 連線失敗
                    Log.d("HKT", error.toString());
            }
        });

        queue.add(jsonObjectRequest);
    }
    public void RefreshOrderSong() {
        
        String url = APIServer+"ListPlaySong";
        HashMap data = new HashMap();
        data.put("token",token);
        data.put("boxId",boxId);
        JsonObjectRequest jsonObjectRequest  = new JsonObjectRequest (Request.Method.POST, url, new JSONObject(data),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // 連線成功
                    //使用 Gson 解析 Json 資料
                    OrderPlayListResponse orderPlayListResponse = new Gson().fromJson(response.toString(), OrderPlayListResponse.class);
                    //orderSongItems.clear();
                    ArrayList<OrderPlayItem> newOrderSongItems = new  ArrayList<>();
                    if(orderPlayListResponse.SongList!=null){
                        for (OrderPlayItem playItem: orderPlayListResponse.SongList) {
                            boolean found = false;
                            if(playItem.PlayStatus != Song_Ended) {
                                for (OrderPlayItem existPlayItem : orderSongItems) {
                                    if (playItem.Id == existPlayItem.Id) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    orderSongItems.add(playItem);
                                    newOrderSongItems.add(playItem);
                                }
                            }
                        }
                        OrderPlayItem[] newOrderSongItemsArray = new OrderPlayItem[newOrderSongItems.size()];
                        newOrderSongItemsArray = newOrderSongItems.toArray(newOrderSongItemsArray);
                        listener.onOrderSongData(VODPlayList.this, newOrderSongItemsArray);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // 連線失敗
                    Log.d("HKT", error.toString());
                }
            });

        queue.add(jsonObjectRequest);
    }
    public void RefreshPublicSong() {
        String url = APIServer+"ListPublicPlaySong";
        HashMap data = new HashMap();
        data.put("token",token);
        data.put("boxId",boxId);
        JsonObjectRequest jsonObjectRequest  = new JsonObjectRequest (Request.Method.POST, url, new JSONObject(data),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // 連線成功
                    //使用 Gson 解析 Json 資料
                    PublicPlayListResponse publicPlayListResponse = new Gson().fromJson(response.toString(), PublicPlayListResponse.class);
                    publicSongItems.clear();
                    if(publicPlayListResponse.SongList!=null){
                        for (PublicPlayItem playItem: publicPlayListResponse.SongList) {
                            publicSongItems.add(playItem);
                        }
                        listener.onPublicSongData(VODPlayList.this);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // 連線失敗
                    Log.d("HKT", error.toString());
                }
            });

        queue.add(jsonObjectRequest);
    }
}
