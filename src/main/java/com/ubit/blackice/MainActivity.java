package com.ubit.blackice;

import static android.speech.tts.TextToSpeech.ERROR;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
//import android.location.Location;
//import android.location.LocationManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
//import android.os.Looper;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Locale;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {
    private String URL = "http://202.31.147.129:25003/h_2023.php"; // 웹서버
    private List<blackiceData> blackiceList = new ArrayList<blackiceData>();
    private List<openapiData> openapiList = new ArrayList<openapiData>();
    private MapView mapView;
    private MapPOIItem marker;
    private TextView textView;
    private TextToSpeech tts;
    private ArrayList<String> lastData = new ArrayList<String>();
    private static final String LOG_TAG = "MainActivity";
    private ViewGroup mapViewContainer;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private double userLatitude;
    private double userLongitude;
    private Location markerLocate = new Location("pointA");
    private Location nowLocate = new Location("pointB");
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    private MapPOIItem startMarker; // 출발지 마커
    private MapPOIItem endMarker; // 목적지 마커
    private ArrayList<MapPOIItem> pathMarkers; // 경로 마커
    private int check = 0;
//    EditText editText = (EditText) findViewById(R.id.editText);
//    Button button = (Button) findViewById(R.id.button);

    public void returnSpinner(ArrayList<String> aList) {        // 날짜별로 데이터베이스 구분하기, 날짜 데이터만 추출

        Spinner sItems = findViewById(R.id.spinner);
        ArrayList<String> fList = new ArrayList<String>();
        for (String item : aList) {
            fList.add(item.substring(0, 13) + "시"); // yyyy-mm-dd 로 slicing
        }
        HashSet<String> distinctData = new HashSet<String>(fList); // aList의 중복을 제거합니다.
        ArrayList<String> spinnerArray = new ArrayList<String>(distinctData);
        Collections.sort(spinnerArray, Collections.reverseOrder());
        if (lastData.size() != spinnerArray.size()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray);
            sItems.setAdapter(adapter);
        }
        lastData = spinnerArray;
    }


    private void getHashKey() {             // 카카오맵 api용 해시키 출력하는 함수
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }

    private float distCondition() {
        float distance = nowLocate.distanceTo(markerLocate);
        Log.i("distance", "distance = " + distance);
        return distance;
    }

    private float minDist() {
        float distance = 0.0f;

        return distance;
    }

    private void getBlackIce() {                    // maria db 정보 가져오고 지도에 표시
        new Thread() {
            @Override
            public void run() {                             // 자동갱신, 실시간으로 삽입.삭제
                Spinner sItems = findViewById(R.id.spinner);
                while (true) {
                    float mindist = 10000.0f;
                    float dist = 10000.0f;
                    try {
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
                        String getTime = sdf.format(date);
//                        Log.i("test", printDate());
                        Document doc = Jsoup.connect(URL).timeout(5000).get();          // 웹서버에서 json형태 웹페이지를 구동한 후 가져옴
                        JSONObject json = new JSONObject(doc.body().text());
                        JSONArray blackiceArray = json.getJSONArray("blackice");        // db 내용 array에 저장
                        JSONArray openapiArray = json.getJSONArray("open_api");
                        try {                                           // 마커 초기화
                            mapView.removeAllPOIItems();
                        } catch (Exception e) {

                        }
                        ArrayList<String> dateList = new ArrayList<String>();

                        for (int i = 0; i < blackiceArray.length(); i++) {                      // for문 돌면서 데이터 날짜, 위도, 경도, 타입을 저장
                            JSONObject blackiceObj = blackiceArray.getJSONObject(i);
                            blackiceData bld = new blackiceData();
                            double markerLatitude = Float.parseFloat(blackiceObj.getString("latitude"));
                            double markerLongtitude = Float.parseFloat(blackiceObj.getString("longitude"));
                            bld.setDatetime(blackiceObj.getString("datetime"));
                            bld.setLatitude(Float.parseFloat(blackiceObj.getString("latitude")));
                            bld.setLongitude(Float.parseFloat(blackiceObj.getString("longitude")));
                            bld.setIce_type(blackiceObj.getString("type"));
                            try {
                                markerLocate.setLatitude(markerLatitude);
                                markerLocate.setLongitude(markerLongtitude);
//                                Log.i("test","passed");
                                String testValue = mapView.getCurrentLocationTrackingMode().toString();
                                Log.i("test location", testValue);
                                nowLocate.setLatitude(userLatitude);
                                nowLocate.setLongitude(userLongitude);
                            } catch (Exception e) {

                            }
                            blackiceList.add(bld);

                            marker = new MapPOIItem();                  // 위도 경도에 마커 표시
                            marker.setTag(0);
                            try {
                                marker.setMapPoint(
                                        MapPoint.mapPointWithGeoCoord(Float.parseFloat(blackiceObj.getString("latitude")),
                                                Float.parseFloat(blackiceObj.getString("longitude")))
                                );
                            } catch (Exception e) {

                            }

                            dateList.add(blackiceObj.getString("datetime"));
                            switch (blackiceObj.getString("type")) {                    // ice type에 따라 마커색 다르게
                                case "f_rock":
                                    try {
                                        if (sItems.getSelectedItem().toString().substring(0, 13).equals(blackiceObj.getString("datetime").substring(0, 13))) {
                                            Thread.sleep(7800);
                                            marker.setItemName("낙석");
                                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                            marker.setCustomImageResourceId(R.drawable.f_rock_icon);
                                            mapView.addPOIItem(marker);
                                            dist = distCondition();
                                            if (dist < mindist){
                                                mindist = dist;
                                            }
                                            try {
                                                tts.setPitch((float) 1.0); // 음성 톤 높이 지정
                                                tts.setSpeechRate((float) 1.5); // 음성 속도 지정
                                                mindist = (int)mindist;
                                                tts.speak((int)mindist + " 미터 근방 낙석이 감지되었습니다", TextToSpeech.QUEUE_ADD, null, null);
                                            } catch (Exception e) {
//                                              Log.d("except", "except");
                                            }
                                        }
                                    } catch (Exception e) {
//                                        Log.d("except", "except");
                                    }
                                    break;
                                case "ice":
                                    try {
                                        if (sItems.getSelectedItem().toString().substring(0, 13).equals(blackiceObj.getString("datetime").substring(0, 13))) {
                                            Thread.sleep(1000);
                                            marker.setItemName("블랙아이스");
                                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                            marker.setCustomImageResourceId(R.drawable.black_ice_icon);
                                            mapView.addPOIItem(marker);
                                            dist = distCondition();
                                            if (dist < mindist){
                                                mindist = dist;
                                            }
                                            try {
                                                tts.setPitch((float) 1.0); // 음성 톤 높이 지정
                                                tts.setSpeechRate((float) 1.5); // 음성 속도 지정
                                                mindist = (int)mindist;
                                                tts.speak((int)mindist + " 미터 근방 블랙아이스가 감지되었습니다", TextToSpeech.QUEUE_ADD, null, null);
                                            }catch (Exception e){
                                                System.out.println(e);
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.out.println("오류 ");
                                        System.out.println(e);
                                    }
                                    break;
                                case "port":
                                    try {
                                        if (sItems.getSelectedItem().toString().substring(0, 13).equals(blackiceObj.getString("datetime").substring(0, 13))) {
                                            Thread.sleep(8300);
                                            marker.setItemName("포트홀");
                                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                            marker.setCustomImageResourceId(R.drawable.porthole_icon);
                                            mapView.addPOIItem(marker);
                                            dist = distCondition();
                                            if (dist < mindist) {
                                                mindist = dist;
                                            }
                                            try {
                                                tts.setPitch((float) 1.0); // 음성 톤 높이 지정
                                                tts.setSpeechRate((float) 1.5); // 음성 속도 지정
                                                mindist = (int)mindist;
                                                tts.speak((int)mindist + " 미터 근방 포트홀이 감지되었습니다", TextToSpeech.QUEUE_ADD, null, null);
                                            } catch (Exception e) {

                                            }
                                        }
                                    } catch (Exception e) {
//                                        Log.d("except" , "Except");
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } // for문 끝 open api 시작

                        for (int i = 0; i < openapiArray.length(); i++) {                      // for문 돌면서 데이터 날짜, 위도, 경도, 타입을 저장하고 ?
                            JSONObject openapiObj = openapiArray.getJSONObject(i);
                            openapiData oad = new openapiData();
                            double markerLatitude = Float.parseFloat(openapiObj.getString("latitude"));
                            double markerLongtitude = Float.parseFloat(openapiObj.getString("longitude"));
                            oad.setDatetime(openapiObj.getString("datetime"));
                            oad.setLatitude(Float.parseFloat(openapiObj.getString("latitude")));
                            oad.setLongitude(Float.parseFloat(openapiObj.getString("longitude")));
                            oad.setIce_type(openapiObj.getString("type"));
                            oad.setIce_type(openapiObj.getString("detail_type"));
                            //oad.setIce_type(openapiObj.getString("message"));
                            System.out.println(oad);
                            try {
                                markerLocate.setLatitude(markerLatitude);
                                markerLocate.setLongitude(markerLongtitude);
                                String testValue = mapView.getCurrentLocationTrackingMode().toString();
                                Log.i("test location", testValue);
                                nowLocate.setLatitude(userLatitude);
                                nowLocate.setLongitude(userLongitude);
                            } catch (Exception e) {

                            }
                            openapiList.add(oad);

                            marker = new MapPOIItem();                  // 위 경도에 마크 표시
                            marker.setTag(0);
                            try {
                                marker.setMapPoint(
                                        MapPoint.mapPointWithGeoCoord(Float.parseFloat(openapiObj.getString("latitude")),
                                                Float.parseFloat(openapiObj.getString("longitude")))
                                );
                            } catch (Exception e) {

                            }

                            dateList.add(openapiObj.getString("datetime"));
                            switch (openapiObj.getString("type")) {                    // type에 따라 마커색 다르게
                                case "교통사고":
                                    try {
                                        if (sItems.getSelectedItem().toString().substring(0, 13).equals(openapiObj.getString("datetime").substring(0, 13))) {
                                            if (openapiObj.getString("detail_type").isEmpty())
                                                marker.setItemName(openapiObj.getString("type"));
                                            else marker.setItemName(openapiObj.getString("detail_type"));
                                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                            marker.setCustomImageResourceId(R.drawable.accident_icon);
                                            mapView.addPOIItem(marker);
                                            dist = distCondition();
                                            if (dist < mindist){
                                                mindist = dist;
                                            }
                                            try {
                                                if ((int)dist <= 30 && getTime.substring(0, 13).equals(sItems.getSelectedItem().toString().substring(0, 13))) {
                                                    tts.setPitch((float) 1.0); // 음성 톤 높이 지정
                                                    tts.setSpeechRate((float) 1.5); // 음성 속도 지정
                                                    mindist = (int)mindist;
                                                    tts.speak((int)mindist + " 미터 근방 사고 현장이 감지되었습니다", TextToSpeech.QUEUE_ADD, null, null);
                                                }
                                            }
                                            catch (Exception e){
                                                System.out.println(e);

                                            }
                                        }
                                    } catch (Exception e) {
                                    }
                                    break;
                                case "공사":
                                    try {
                                        if (sItems.getSelectedItem().toString().substring(0, 13).equals(openapiObj.getString("datetime").substring(0, 13))) {
                                            System.out.println(openapiObj.getString("detail_type"));
                                            if (openapiObj.getString("detail_type").isEmpty())
                                                marker.setItemName(openapiObj.getString("type"));
                                            else marker.setItemName(openapiObj.getString("detail_type"));
                                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                                            marker.setCustomImageResourceId(R.drawable.construction_icon);
                                            mapView.addPOIItem(marker);
                                            dist = distCondition();
                                            if (dist < mindist){
                                                mindist = dist;
                                            }
                                            try {
                                                if ((int)dist <= 30 && getTime.substring(0, 13).equals(sItems.getSelectedItem().toString().substring(0, 13))) {
                                                    tts.setPitch((float) 1.0); // 음성 톤 높이 지정
                                                    tts.setSpeechRate((float) 1.5); // 음성 속도 지정
                                                    mindist = (int)mindist;
                                                    tts.speak((int)mindist + " 미터 근방 공사 현장이 감지되었습니다", TextToSpeech.QUEUE_ADD, null, null);
                                                }
                                            }
                                            catch (Exception e){

                                            }
                                        }
                                    } catch (Exception e) {
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                returnSpinner(dateList);
                            }
                        });
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    try { // 시간지연
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Log.d("test", "time Error");
                    }
                }
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {            // tts 불러오기
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        Log.d(this.getClass().getName(), "onCreate: passed");
        getHashKey();   // 해시키 불러오는거
        getBlackIce();  // db에서 불러오는거
        mapView = new MapView(this);    // map 선언 후 띄우기
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        // 시작 위치..
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(35.9535392, 126.6849254), 0, true);
        mapView.setMapViewEventListener(this);

        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
        mapView.setCurrentLocationEventListener(this);
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

    }

    public void onInit(int status) { // OnInitListener를 통해서 TTS 초기화
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(Locale.KOREA); // TTS언어 한국어로 설정
            if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                Log.e("TTS", "This Language is not supported");
            }else{
                tts.setPitch((float)0.6); // 음성 톤 높이 지정
                tts.setSpeechRate((float)0.1); // 음성 속도 지정
                tts.speak("내비게이션 입니다.", TextToSpeech.QUEUE_ADD, null, null);

            }
        }else{
            Log.e("TTS", "Initialization Failed!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewContainer.removeAllViews();
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
        userLatitude = mapPointGeo.latitude;
        userLongitude = mapPointGeo.longitude;
        nowLocate.setLongitude(userLongitude);
        nowLocate.setLatitude(userLatitude);
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
        Log.i("failed to update", "failed to update");
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
        Log.i("failed to update", "failed to update");
    }

    private void onFinishReverseGeoCoding(String result) {
//        Toast.makeText(LocationDemoActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }

    // ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
                Log.d("@@@", "start");
                //위치 값을 가져올 수 있음

            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void findPath() {
        double startX = userLongitude;
        double startY = userLatitude;
        double endX = endMarker.getMapPoint().getMapPointGeoCoord().longitude;
        double endY = endMarker.getMapPoint().getMapPointGeoCoord().latitude;

        findPathWithAPI(startX, startY, endX, endY);
        // 사용자의 현재 위치 좌표 WGS84 : (userLatitude, userLongitude)
        // 목적지 좌표 WGS84 : (endMarker.getMapPoint().getMapPointGeoCoord().latitude, endMarker.getMapPoint().getMapPointGeoCoord().longitude)

        // 경로 탐색 API를 사용하여 가져온 경로 데이터를 기반으로 경로를 그려주는 코드를 추가합니다.
        // 이 부분에는 경로의 각 지점을 지도에 추가하고 연결하는 Mapper를 사용하거나 앱이 지원하는 방식에 따른 이벤트를 처리하는 코드를 추가해야 합니다.
    }

    private void findPathWithAPI(double startX, double startY, double endX, double endY) {
        // 카카오맵 길찾기 REST API를 이용한다고 가정했을 때,
        String kakaoApiKey = "d0068c14c1d7b6f7ef120e8ddd7cbf36";
        String kakaoApiUrl = "https://dapi.kakao.com/v2/local/search/category.json";
        String apiUrl = kakaoApiUrl + "?category_group_code=PO3&rect=" + startX + "," + startY + "," + endX + "," + endY;

        // 이제 위에서 구한 apiUrl을 이용해 API 요청을 실행하고 결과를 처리합니다.
        // 이 부분에서는 Volley 라브러리를 사용하던지, 다른 HTTP 라이브러리를 사용하여 API 호출을 수행합니다.
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                response -> {
                    // JSONObject response에 경로 정보가 포함되어 있습니다.
                    // 이 부분에서 경로 관련 데이터를 추출하고 지도에 경로를 그리는 작업을 수행합니다.
                },
                error -> Log.e("API REQUEST ERROR", "Server Connection Error: " + error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                // 카카오 API 키를 헤더에 추가합니다
                params.put("Authorization", "KakaoAK " + kakaoApiKey);
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void setDestination(MapPoint mapPoint) {
        if (endMarker != null) {
            mapView.removePOIItem(endMarker);
        }
        endMarker = new MapPOIItem();
        endMarker.setTag(1);
        endMarker.setMapPoint(mapPoint);
        endMarker.setItemName("도착지");
        endMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);

        mapView.addPOIItem(endMarker);
        // 경로 탐색 기능을 수행
        findPath();
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {             // 맵이 처음 불려질 때 불러와지는 함수
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {             // 맵의 중심 좌표가 이동할 때 불려지는 함수
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {            // 줌레벨의 변화가 있을 때 불려지는 함수
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {            // 맵을 사용자가 길게 눌렀을 때 불려지는 함수
        setDestination(mapPoint);
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reloadDB:
                getBlackIce();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}