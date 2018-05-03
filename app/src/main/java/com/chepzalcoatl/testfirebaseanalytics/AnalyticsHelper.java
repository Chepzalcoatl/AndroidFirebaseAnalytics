package com.chepzalcoatl.testfirebaseanalytics;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.StatFs;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;

public class AnalyticsHelper {
    private static AnalyticsHelper instance = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Bundle bundle;
    private Context context;

    /**
     * Metodo que te devuelve la instancia de la clase para poder hacer uso de los metodos.
     * @param context
     * @return
     */
    public static AnalyticsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsHelper(context);
        }
        return instance;
    }

    /**
     * Metodo constructor que necesta del contexto para poder conectarse a Analytics.
     * @param c
     */
    protected AnalyticsHelper(Context c) {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(c);
        context = c;
    }

    /**
     * Metodo que registra eventos en Analytics.
     * @param evento
     * @param valores
     */
    public void RegistraEvento(String evento, HashMap<String, String> valores) {
        bundle = new Bundle();
        if (valores.size() <= 0)
            throw new RuntimeException("La cantidad de valores debe ser mayor a 0.");
        Iterator<Map.Entry<String, String>> entries = valores.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            bundle.putString(entry.getKey(), entry.getValue());
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
        mFirebaseAnalytics.logEvent(evento, bundle);
    }

    /**
     * Metodo que registra las propiedades del usuario en Analytics
     * @param clave
     * @param valor
     */
    public void RegistraPropiedadesUsuario(String clave, String valor) {
        mFirebaseAnalytics.setUserProperty(clave, valor);
    }

    /**
     *
     * @param activity
     * @param nombrePantalla
     * @param nombreClase
     */
    public void RegistraPantallas(Activity activity, String nombrePantalla, String nombreClase) {
        mFirebaseAnalytics.setCurrentScreen(activity, nombrePantalla, nombreClase);
    }

    /**
     * Metodo que registra la memoria ram disponible  en MB.
     */
    public void RegistrarRAMDisponible() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        final double availableMegs = mi.availMem / 0x100000L;
        //Percentage can be calculated for API 16+
        final double percentAvail = mi.availMem / (double) mi.totalMem * 100.0;
        Log.i("RAM", "Ram disponible = " + availableMegs);
        Log.i("RAM", "Ram porcentaje = " + percentAvail);
        RegistraEvento("RAM_DISPONIBLE", new HashMap<String, String>() {{
            put("RAM_disponible", "" + availableMegs);
        }});
        RegistraEvento("RAM_DISPONIBLE", new HashMap<String, String>() {{
            put("RAM_porcentaje", "" + percentAvail);
        }});
    }

    /**
     * Metodo que registra en analytics el porcentaje de bateria. Ejemplo: 0.39
     */
    public void RegistraBateria() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        final float batteryPct = level / (float) scale;
        Log.i("BATERIA", "Bateria porcentaje: " + batteryPct);
        RegistraEvento("BATERIA", new HashMap<String, String>() {{
            put("BATERIA_porcentaje", "" + batteryPct);
        }});
    }

    /**
     * Metodo que registra en analytics el almacenamiento disponible en MB.
     */
    public void RegistraAlmacenamientoDisponible() {
        StatFs stat = new StatFs(context.getFilesDir().getAbsolutePath());
        final float disponible = ((long) stat.getBlockSize() * (long) stat.getAvailableBlocks()) / (1024.f * 1024.f);
        RegistraEvento("ALMACENAMIENTO", new HashMap<String, String>() {{
            put("ALMACENAMIENTO_disponible", "" + disponible);
        }});
    }

    /**
     * Metodo que registra en analytics el nivel de señal de wifi de 0 a 5.
     */
    public void RegistraNivelWIFI() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final int levelW = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        Log.i("WIFI", "LEVEL = " + levelW);
        RegistraEvento("WIFI", new HashMap<String, String>() {{
            put("WIFI_nivel", "" + levelW);
        }});
    }

    /**
     * Metodo que registra el nivel de red telefonica de 0 a 4.
     */
    public void RegistraNivelRedTelefonica() {
        try {
            if (ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            TelephonyManager tm = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
            CellSignalStrengthGsm gsm = null;
            CellSignalStrengthCdma cdma = null;
            CellSignalStrengthLte lte = null;

            HashMap<String,String> map = new HashMap();

            for (final CellInfo info : tm.getAllCellInfo()) {
                if (info instanceof CellInfoGsm) {
                    gsm = ((CellInfoGsm) info).getCellSignalStrength();
                    map.put("RED_GSM_nivel",""+gsm.getLevel());
                }
                if (info instanceof CellInfoCdma) {
                    cdma = ((CellInfoCdma) info).getCellSignalStrength();
                    map.put("RED_CDMA_nivel",""+cdma.getLevel());
                }
                if (info instanceof CellInfoLte) {
                    lte = ((CellInfoLte) info).getCellSignalStrength();
                    map.put("RED_LTE_nivel",""+lte.getLevel());
                }
            }
            RegistraEvento("RED",map);
        } catch (Exception e) {
            Log.e("", "Unable to obtain cell signal information", e);
        }
    }

}

