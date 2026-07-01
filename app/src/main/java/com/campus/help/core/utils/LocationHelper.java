package com.campus.help.core.utils;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

/**
 * 高德定位 + 逆地理工具（成员 D）。
 * <p>
 * 封装 {@link AMapLocationClient}（单次定位，带地址）与 {@link GeocodeSearch}（经纬度 → 地址文案），
 * 供地图「定位到我的位置」与任务地点展示使用。隐私合规已在 {@link com.campus.help.CampusHelpApp}
 * 通过 {@link AmapPrivacyHelper} 调用，SDK 引入后自动生效。
 */
public class LocationHelper {

    /** 定位 / 逆地理回调。成功 onResult，失败 onError。 */
    public interface Callback {
        void onResult(double lat, double lng, String address);
        void onError(int code, String message);
    }

    private AMapLocationClient locationClient;

    public LocationHelper(Context ctx) {
        try {
            locationClient = new AMapLocationClient(ctx.getApplicationContext());
            locationClient.setLocationOption(buildOption());
        } catch (Exception e) {
            locationClient = null;
        }
    }

    private AMapLocationClientOption buildOption() {
        AMapLocationClientOption opt = new AMapLocationClientOption();
        // 高精度：网络 + GPS，单次定位，返回地址
        opt.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        opt.setOnceLocation(true);
        opt.setOnceLocationLatest(true);
        opt.setNeedAddress(true);
        opt.setHttpTimeOut(20000);
        opt.setInterval(2000);
        return opt;
    }

    /** 单次获取当前位置。结果在主线程回调。 */
    public void getCurrentLocation(Callback cb) {
        if (locationClient == null) {
            if (cb != null) cb.onError(-1, "定位客户端未初始化");
            return;
        }
        locationClient.setLocationListener((AMapLocationListener) loc -> {
            if (loc == null) {
                if (cb != null) cb.onError(-1, "定位结果为空");
                return;
            }
            if (loc.getErrorCode() == 0) {
                if (cb != null) cb.onResult(loc.getLatitude(), loc.getLongitude(),
                        loc.getAddress() != null ? loc.getAddress() : loc.getCity());
            } else {
                if (cb != null) cb.onError(loc.getErrorCode(), loc.getErrorInfo());
            }
            // 单次定位：拿到结果后停止，下次调用会重新 startLocation
            if (locationClient != null) {
                locationClient.stopLocation();
            }
        });
        locationClient.startLocation();
    }

    /**
     * 经纬度 → 地址文案（逆地理）。用于展示任务地点对应的文字地址。
     */
    public static void latlngToAddress(Context ctx, double lat, double lng, Callback cb) {
        GeocodeSearch search;
        try {
            search = new GeocodeSearch(ctx.getApplicationContext());
        } catch (com.amap.api.services.core.AMapException e) {
            if (cb != null) cb.onError(-1, "GeocodeSearch 初始化失败");
            return;
        }
        search.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
                if (rCode == 1000 && result != null && result.getRegeocodeAddress() != null) {
                    String addr = result.getRegeocodeAddress().getFormatAddress();
                    if (cb != null) cb.onResult(lat, lng, addr);
                } else {
                    if (cb != null) cb.onError(rCode, "逆地理失败");
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult result, int rCode) {
                // 正向地理编码，本场景不用
            }
        });
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(lat, lng), 200, GeocodeSearch.AMAP);
        search.getFromLocationAsyn(query);
    }

    /** 在 Fragment/Activity 销毁时调用，释放定位资源。 */
    public void destroy() {
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
            locationClient = null;
        }
    }
}
