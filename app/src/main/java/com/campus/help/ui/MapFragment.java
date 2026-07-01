package com.campus.help.ui;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.campus.help.core.base.BaseFragment;
import com.campus.help.core.utils.LocationHelper;
import com.campus.help.core.utils.PermissionUtils;
import com.campus.help.data.model.Task;
import com.campus.help.data.repo.TaskRepository;
import com.campus.help.databinding.FragmentMapBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地图模式（成员 D）。
 * <p>
 * 高德 {@link MapView} 展示周围任务 Marker，数据复用 {@link TaskRepository#observeAll()}
 * 的 List&lt;Task&gt;（取 latitude/longitude）。「定位」按钮单次定位并移动相机到当前位置。
 * 点 Marker 弹信息窗（标题 + 报酬/地址）；点信息窗 → 任务详情（依赖 B 的 TaskDetailActivity，待对接）。
 */
public class MapFragment extends BaseFragment<FragmentMapBinding> {

    private AMap aMap;
    private LocationHelper locationHelper;
    private final Map<Marker, Task> markerTaskMap = new HashMap<>();

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                        if (granted) {
                            locateMe();
                        } else {
                            toast("需要定位权限才能定位");
                        }
                    });

    @Nullable
    private Bundle savedState;

    @Override
    protected FragmentMapBinding createBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentMapBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedState = savedInstanceState;
    }

    @Override
    protected void initView() {
        // MapView 生命周期：必须先 onCreate，再拿 AMap
        binding.map.onCreate(savedState);
        aMap = binding.map.getMap();
        if (aMap != null) {
            // 默认 UI 设置
            aMap.getUiSettings().setMyLocationButtonEnabled(false);
            aMap.getUiSettings().setZoomControlsEnabled(true);
            // 点 Marker 弹信息窗（return false = 使用默认信息窗）
            aMap.setOnMarkerClickListener(marker -> {
                marker.showInfoWindow();
                return true;
            });
            // 点信息窗 → 任务详情（B 的产出，暂未交付）
            aMap.setOnInfoWindowClickListener(marker -> {
                Task t = markerTaskMap.get(marker);
                toast(t == null ? "任务详情待 B 对接" : "「" + t.title + "」详情待 B 对接");
            });
        }

        binding.btnLocate.setOnClickListener(v -> {
            if (PermissionUtils.hasLocation(requireContext())) {
                locateMe();
            } else {
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });

        locationHelper = new LocationHelper(requireContext());
    }

    @Override
    protected void initData() {
        LiveData<List<Task>> live = new TaskRepository().observeAll();
        live.observe(getViewLifecycleOwner(), this::renderMarkers);
    }

    private void renderMarkers(@Nullable List<Task> tasks) {
        if (aMap == null || tasks == null) {
            return;
        }
        aMap.clear();
        markerTaskMap.clear();
        LatLngBounds.Builder builder = LatLngBounds.builder();
        int added = 0;
        for (Task t : tasks) {
            if (t.latitude == 0 && t.longitude == 0) {
                continue; // 无坐标的任务跳过
            }
            LatLng pos = new LatLng(t.latitude, t.longitude);
            Marker marker = aMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(t.title == null ? "未命名任务" : t.title)
                    .snippet(snippetOf(t))
                    .icon(BitmapDescriptorFactory.defaultMarker()));
            markerTaskMap.put(marker, t);
            builder.include(pos);
            added++;
        }
        if (added > 0) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 64));
        }
    }

    private String snippetOf(Task t) {
        String reward = String.format("¥%.1f", t.reward);
        String loc = t.location == null || t.location.isEmpty() ? "无地址" : t.location;
        return reward + " · " + loc;
    }

    private void locateMe() {
        if (locationHelper == null) {
            return;
        }
        toast("定位中…");
        locationHelper.getCurrentLocation(new LocationHelper.Callback() {
            @Override
            public void onResult(double lat, double lng, String address) {
                if (aMap != null) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16f));
                }
                if (address != null && !address.isEmpty()) {
                    toast(address);
                }
            }

            @Override
            public void onError(int code, String message) {
                toast("定位失败：" + (message == null ? String.valueOf(code) : message));
            }
        });
    }

    // ===== MapView 生命周期转发 =====

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && binding.map != null) {
            binding.map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null && binding.map != null) {
            binding.map.onPause();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null && binding.map != null) {
            binding.map.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroyView() {
        if (locationHelper != null) {
            locationHelper.destroy();
            locationHelper = null;
        }
        if (binding != null && binding.map != null) {
            binding.map.onDestroy();
        }
        super.onDestroyView();
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}
