package com.example.mdns_browser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ServiceInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class mDNSBrowser {
    NsdHelper mNsdHelper;
    private Handler handler = new Handler();
    public static final String TAG = "mdns_browser";
    public File file;
    public List<String> listOfSerials = new ArrayList<String>();
    public Map<String, String> name2Serial = new HashMap<String, String>();

    public mDNSBrowser(Context context) {
        mNsdHelper = new NsdHelper(context);
        mNsdHelper.initializeNsd();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public String[] getListOfSerials() {
        return listOfSerials.toArray(new String[0]);
    }

    public void startScanService() {
        mNsdHelper.discoverServices();
    }

    public void stopScanService() {
        mNsdHelper.stopDiscovery();
    }

    public void clickAdvertise(View v) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                try {
                    String iNetAddress = getIPAddress(true);
                    InetAddress inetAddress = InetAddress
                            .getByName(iNetAddress);
                    Log.e(TAG, "ip " + iNetAddress);
                    mNsdHelper.registerService(getport(), inetAddress);
                    Log.e("NSD", "Registered");
                    Log.e("inetaddress", inetAddress.toString());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public int getport() {

        try {
            ServerSocket mServerSocket;
            mServerSocket = new ServerSocket(0);
            return mServerSocket.getLocalPort();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 123;
        }

    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf
                        .getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = addr instanceof Inet4Address;
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port
                                // suffix
                                return delim < 0 ? sAddr : sAddr.substring(0,
                                        delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public void clickDiscover(View v) {
        mNsdHelper.discoverServices();
    }

    public void listService(final NsdServiceInfo service) {
        handler.postDelayed(new Runnable() {
            @SuppressWarnings("static-access")
            @Override
            public void run() {
                String serial = "";
                String serviceStr = String.valueOf(service);
                int index = serviceStr.indexOf("ASN");
                if (index > -1) {
                    serial = serviceStr.substring(index, index + 10);
                } else {
                    return;
                }

                if (listOfSerials.contains(serial)) return; // already added

                listOfSerials.add(serial);

                String serviceName = service.getServiceName();
                name2Serial.put(serviceName, serial);

                String msg = "name :" + serviceName
                        + "\n port: " + service.getPort();

                InetAddress host = service.getHost();
                if (host != null) {
                    msg = msg + "\n ip: " + ""
                            + service.getHost().getHostAddress()
                            + "\n serial number: " + ""
                            + serial;
                } else {
                    msg = msg + "host is null" + ""
                            + "\n serial number: " + ""
                            + serial;
                }
                msg = msg + "\n============== " + "\n ";

            }
        }, 100);
    }

    public void listLostService(final NsdServiceInfo service) {
        handler.postDelayed(new Runnable() {
            @SuppressWarnings("static-access")
            @Override
            public void run() {
                String serial = "";
                String serviceName = service.getServiceName();
                if (name2Serial.containsKey(serviceName)) {
                    serial = name2Serial.get(serviceName);
                } else {
                    return;
                }

                if (listOfSerials.contains(serial)) {
                    listOfSerials.remove(serial);
                    name2Serial.remove(serviceName);

                    String msg = "Lost service: " + serial + "\n============== " + "\n ";
                }

            }
        }, 100);
    }

    public class NsdHelper {

        Context mContext;

        NsdManager mNsdManager;
        NsdManager.ResolveListener mResolveListener;
        NsdManager.DiscoveryListener mDiscoveryListener;
        NsdManager.RegistrationListener mRegistrationListener;

        public static final String SERVICE_TYPE ="_http._tcp."; //use your service type you want to use
        public static final String TAG = "NsdHelper";
        public String mServiceName = getDeviceName() + "";
        NsdServiceInfo mService;

        public NsdHelper(Context context) {
            mContext = context;
            mNsdManager = (NsdManager) context
                    .getSystemService(Context.NSD_SERVICE);
        }

        public String getDeviceName() {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            int version = Build.VERSION.SDK_INT;
            String filename = model + "_" + version;

            if (model.startsWith(manufacturer)) {
                return capitalize(model);
            } else {
                return capitalize(manufacturer) + " " + model;
            }
        }

        public String getDeviceVersion() {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            int version = Build.VERSION.SDK_INT;
            String filename = model + "_" + version;

            if (model.startsWith(manufacturer)) {
                return capitalize(model) + "_" + filename;
            } else {
                return capitalize(manufacturer) + "_" + model + " " + filename;
            }
        }

        private String capitalize(String s) {
            if (s == null || s.length() == 0) {
                return "";
            }
            char first = s.charAt(0);
            if (Character.isUpperCase(first)) {
                return s;
            } else {
                return Character.toUpperCase(first) + s.substring(1);
            }
        }

        public void initializeNsd() {
            initializeResolveListener();
            initializeDiscoveryListener();
            initializeRegistrationListener();

        }

        public void initializeDiscoveryListener() {
            mDiscoveryListener = new NsdManager.DiscoveryListener() {

                @Override
                public void onDiscoveryStarted(String regType) {
                    Log.e(TAG, "Service discovery started");
                }

                @Override
                public void onServiceFound(NsdServiceInfo service) {
                    if (service.getServiceType().contains("_http._tcp")) {
                        mNsdManager.resolveService(service, mResolveListener);
                        Log.e(TAG, "service info :: " + service + "..");
                        mService = service;
                    }
                }

                @Override
                public void onServiceLost(NsdServiceInfo service) {
                    Log.e(TAG, "service lost" + service);
                    listLostService(service);
                    if (mService == service) {
                        mService = null;
                    }
                }

                @Override
                public void onDiscoveryStopped(String serviceType) {
                    Log.i(TAG, "Discovery stopped: " + serviceType);
                }

                @Override
                public void onStartDiscoveryFailed(String serviceType,
                                                   int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }

                @Override
                public void onStopDiscoveryFailed(String serviceType,
                                                  int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }
            };
        }

        public void initializeResolveListener() {
            mResolveListener = new NsdManager.ResolveListener() {

                @Override
                public void onResolveFailed(NsdServiceInfo serviceInfo,
                                            int errorCode) {
                    Log.e(TAG, "Resolve failed" + errorCode);
                }

                @Override
                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                    Log.e(TAG, "host :  "
                            + serviceInfo.getHost().getHostAddress());
                    Log.e(TAG, "Address :  "
                            + serviceInfo.getHost().getAddress());
                    listService(serviceInfo);
                    if (serviceInfo.getServiceName().equals(mServiceName)) {
                        Log.d(TAG, "Same IP.");
                        return;
                    }
                    mService = serviceInfo;
                }
            };
        }

        public void initializeRegistrationListener() {
            mRegistrationListener = new NsdManager.RegistrationListener() {

                @Override
                public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                    mServiceName = NsdServiceInfo.getServiceName();
                    Toast.makeText(mContext,
                            "device registerd  :" + mServiceName, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                }

                @Override
                public void onServiceUnregistered(NsdServiceInfo arg0) {
                }

                @Override
                public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
                                                   int errorCode) {
                }

            };
        }

        public void registerService(int port, InetAddress ip) {
            NsdServiceInfo serviceInfo = new NsdServiceInfo();
            serviceInfo.setPort(port);
            serviceInfo.setServiceName(mServiceName);
            serviceInfo.setServiceType(SERVICE_TYPE);
            serviceInfo.setHost(ip);

            mNsdManager.registerService(serviceInfo,
                    NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

        }

        public NsdServiceInfo discoverServices() {
            mNsdManager.discoverServices(SERVICE_TYPE,
                    NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
            return mService;
        }

        public void stopDiscovery() {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }

        public NsdServiceInfo getChosenServiceInfo() {
            return mService;
        }

        public void tearDown() {
        }

    }

}
