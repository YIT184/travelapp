package edu.travels.travelapp.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.AMapException;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import edu.travels.travelapp.R;

public class MapFragment extends Fragment implements
        AMapLocationListener,
        AMap.OnMarkerClickListener,
        PoiSearch.OnPoiSearchListener {

    private MapView mapView;
    private AMap aMap;
    private AMapLocationClient locationClient;
    private ImageButton btnLocation;
    private View rootView;

    private static final int REQUEST_PERMISSION = 100;
    private LatLng currentLatLng;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // *** 添加隐私合规声明，必须在地图任何操作前调用 ***
        try {
            MapsInitializer.updatePrivacyShow(requireContext(), true, true);
            MapsInitializer.updatePrivacyAgree(requireContext(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.map_view);

        // 搜索菜单按钮
        FloatingActionButton btnSearchMenu = view.findViewById(R.id.btn_search_menu);
        btnSearchMenu.setOnClickListener(v -> showSearchCategoryDialog());

        // 定位按钮
        btnLocation = view.findViewById(R.id.btn_location);
        btnLocation.setOnClickListener(v -> {
            if (currentLatLng != null) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
            } else {
                Toast.makeText(requireContext(), "正在定位…", Toast.LENGTH_SHORT).show();
                startLocation();
            }
        });

        mapView.onCreate(savedInstanceState);

        initMap();
        initLocation();

        return view;
    }

    private void showSearchCategoryDialog() {
        // 创建 BottomSheetDialog
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);  // 可自定义主题透明背景

        // 自定义布局：垂直列出四个按钮
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_search_categories, null);

        sheetView.findViewById(R.id.btn_category_toilet).setOnClickListener(__ -> {
            searchPoi("公厕|厕所|卫生间|洗手间");
            dialog.dismiss();
        });
        sheetView.findViewById(R.id.btn_category_parking).setOnClickListener(__ -> {
            searchPoi("停车场");
            dialog.dismiss();
        });
        sheetView.findViewById(R.id.btn_category_attraction).setOnClickListener(__ -> {
            searchPoi("景点|景区|公园|山|旅游|湖");
            dialog.dismiss();
        });
        sheetView.findViewById(R.id.btn_category_mall).setOnClickListener(__ -> {
            searchPoi("商场|购物中心|超市");
            dialog.dismiss();
        });

        dialog.setContentView(sheetView);
        dialog.show();
    }

    private void initMap() {
        aMap = mapView.getMap();

        //隐藏高德默认的右上角定位按钮
        aMap.getUiSettings().setMyLocationButtonEnabled(false);

        //启用高德内置的原生定位蓝点
        aMap.setMyLocationEnabled(true);

        aMap.setOnMarkerClickListener(this);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void navigateToAmapApp(double destLat, double destLon, String poiName) {
        try {
            // 经典scheme + 官方推荐参数
            String uri = "androidamap://navi?" +
                    "sourceApplication=" + getString(R.string.app_name) +
                    "&poiname=" + Uri.encode(poiName) +                    //终点名，编码防特殊字符
                    "&lat=" + destLat +
                    "&lon=" + destLon +
                    "&dev=1" +
                    "&style=2";                                            //步行导航

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.autonavi.minimap");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(requireContext(), "成功调起高德地图导航！", Toast.LENGTH_SHORT).show();
            } else {
                throw new Exception("resolveActivity null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "调起失败，请手动打开高德地图导航", Toast.LENGTH_LONG).show();

            // 直接跳转市场更新高德（不显示失败Toast循环）
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.autonavi.minimap")));
            } catch (Exception ex) {}
        }
    }


    private void initLocation() {
        try {
            locationClient = new AMapLocationClient(requireActivity().getApplicationContext());
            locationClient.setLocationListener(this);

            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(false);
            option.setNeedAddress(true);
            locationClient.setLocationOption(option);

            startLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLocation() {
        if (locationClient == null) {
            Toast.makeText(requireContext(), "定位服务初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_PERMISSION);
            return;
        }
        locationClient.startLocation();
    }

    private void searchPoi(String keyword) {
        if (currentLatLng == null) {
            Toast.makeText(requireContext(), "正在获取位置，请稍等", Toast.LENGTH_SHORT).show();
            return;
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            Toast.makeText(requireContext(), "请输入搜索关键词", Toast.LENGTH_SHORT).show();
            return;
        }

        String trimmedKeyword = keyword.trim();

        try {
            //查询对象：关键字放宽，分类码针对性设置
            //对于“景点”，使用高德推荐的分类码 "110000"（风景名胜）或 "140000"（旅游景点）
            String categoryCode = "";
            if (trimmedKeyword.contains("景点") || trimmedKeyword.contains("景区") || trimmedKeyword.contains("公园")) {
                categoryCode = "110000|140000";  // 风景名胜 + 旅游景点
            } else if (trimmedKeyword.contains("商场") || trimmedKeyword.contains("购物")) {
                categoryCode = "060000";  // 购物
            } else if (trimmedKeyword.contains("停车")) {
                categoryCode = "150900";  // 停车场
            } else if (trimmedKeyword.contains("厕所") || trimmedKeyword.contains("公厕")) {
                categoryCode = "200300";  // 公共厕所
            }

            PoiSearch.Query query = new PoiSearch.Query(trimmedKeyword, categoryCode, "");
            query.setPageSize(40);  // 最大30条
            query.setPageNum(0);

            //搜索范围优化：半径控制在 15公里
            LatLonPoint centerPoint = new LatLonPoint(currentLatLng.latitude, currentLatLng.longitude);
            int radius = 15000;
            PoiSearch.SearchBound bound = new PoiSearch.SearchBound(centerPoint, radius, true);

            //创建搜索实例
            PoiSearch poiSearch = new PoiSearch(requireContext(), query);
            poiSearch.setBound(bound);
            poiSearch.setOnPoiSearchListener(this);

            poiSearch.searchPOIAsyn();

        } catch (AMapException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "POI搜索失败：" + e.getErrorCode() + " - " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasMovedCamera = false;  // 添加一个标志位（放在类成员变量）

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location != null && location.getErrorCode() == 0) {
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            // 只在首次定位成功时自动居中并放大
            if (!hasMovedCamera) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
                hasMovedCamera = true;
            }
            // 后续定位不再自动移动相机，让用户自由操作
        } else {
            // 可选：打印详细错误，便于调试
            String errText = "定位失败: " + location.getErrorCode() + " - " + location.getErrorInfo();
            Toast.makeText(requireContext(), errText, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int code) {
        if (code == 1000 && result != null && result.getPois() != null) {
            aMap.clear();

            for (PoiItem poi : result.getPois()) {
                LatLng pos = new LatLng(poi.getLatLonPoint().getLatitude(), poi.getLatLonPoint().getLongitude());

                int iconRes = (poi.getTitle() != null && poi.getTitle().contains("停车")) ||
                        (poi.getTypeDes() != null && poi.getTypeDes().contains("停车"))
                        ? R.drawable.ic_parking : R.drawable.ic_toilet;

                MarkerOptions options = new MarkerOptions()
                        .position(pos)
                        .title(poi.getTitle())
                        .snippet(poi.getSnippet() + "\n距离约 " + poi.getDistance() + "米")
                        .icon(BitmapDescriptorFactory.fromBitmap(
                                BitmapFactory.decodeResource(getResources(), iconRes)));

                aMap.addMarker(options);
            }
        } else {
            Toast.makeText(requireContext(), "没有找到相关地点", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {}

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();  // 先显示信息窗

        // 获取该Marker的位置和标题
        LatLng position = marker.getPosition();
        String title = marker.getTitle() != null ? marker.getTitle() : "未知地点";

        // 弹出对话框，让用户选择“导航到这里”
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("导航到 " + title)
                .setMessage(marker.getSnippet())  // 显示距离等信息
                .setPositiveButton("用高德地图导航", (dialog, which) -> {
                    navigateToAmapApp(position.latitude, position.longitude, title);
                })
                .setNegativeButton("取消", null)
                .show();

        return true;  // 返回true表示已处理点击事件
    }

    // ==================== 生命周期管理 ====================
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        }
    }
}