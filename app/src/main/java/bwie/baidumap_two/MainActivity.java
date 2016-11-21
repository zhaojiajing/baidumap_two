package bwie.baidumap_two;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MapView mMapView;
    private Button bt_commen;
    private Button bt_location;
    private Button bt_site;
    private BaiduMap mBaiduMap;//BaiduMap对象
    private Button bt_none;
    private Button bt_tranffic;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         *  在使用SDK各组件之前初始化context信息，传入ApplicationContext
         注意该方法要再setContentView方法之前实现
         */
        SDKInitializer.initialize(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        /**
         * 初始化控件
         */
        initView();
        mLocationClient = new LocationClient(getApplicationContext()); //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();//不调用此方法, 不显示具体的地址及周边

        //设置缩放比例 3-21
        //   mBaiduMap.setMaxAndMinZoomLevel(10,20);

        mBaiduMap = mMapView.getMap();
        MapStatusUpdate statusUpdate = MapStatusUpdateFactory.zoomTo(15.0f);  //500米
        mBaiduMap.setMapStatus(statusUpdate);
    }

    private void initView() {
        /**
         *获取地图控件引用  找控件MapView
         */
        mMapView = (MapView) findViewById(R.id.bmapView);
        bt_commen = (Button) findViewById(R.id.bt_commen);
        bt_location = (Button) findViewById(R.id.bt_location);
        bt_site = (Button) findViewById(R.id.bt_site);
        bt_none = (Button) findViewById(R.id.bt_none);
        bt_tranffic = (Button) findViewById(R.id.bt_tranffic);

        bt_commen.setOnClickListener(this);
        bt_location.setOnClickListener(this);
        bt_site.setOnClickListener(this);
        bt_none.setOnClickListener(this);
        bt_tranffic.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_commen:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); //普通地图
                break;
            case R.id.bt_site:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE); //卫星地图
                break;
            case R.id.bt_none:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);//空白地图
                break;
            case R.id.bt_tranffic:
                //判断是否开启交通地图
                if (!mBaiduMap.isTrafficEnabled()) {
                    mBaiduMap.setTrafficEnabled(true);//开启交通地图
                    bt_tranffic.setText("交通地图(on)");
                } else {
                    mBaiduMap.setTrafficEnabled(false);//关闭交通地图
                    bt_tranffic.setText("交通地图(off)");
                }

                break;
            case R.id.bt_location:
                //开启定位
                mLocationClient.start();
                break;
        }
    }

    public class MyLocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(BDLocation location) {

            /**
             *添加 Maker坐标点   用的location 也可吧location用handler发出去,进行操作,那么此方法就只打Log
             */
//            //定义Maker坐标点
//            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
//            //构建Marker图标
//            BitmapDescriptor bitmap = BitmapDescriptorFactory
//                    .fromResource(R.drawable.public_label_bg_red);
//            //构建MarkerOption，用于在地图上添加Marker
//            OverlayOptions option = new MarkerOptions()
//                    .position(point)
//                    .icon(bitmap);
//            //在地图上添加Marker，并显示
//            mBaiduMap.addOverlay(option);

            // 开启定位图层
            mBaiduMap.setMyLocationEnabled(true);
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            //新的定位
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(point);
            //移动到新的定位点 //默认有个小蓝点
            mBaiduMap.animateMapStatus(u);
            //吐司具体的地址
            Toast.makeText(MainActivity.this, location.getAddrStr(), Toast.LENGTH_SHORT).show();
            mLocationClient.stop();

            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.e("BaiduLocationApiDem", sb.toString());
        }
    }
}
