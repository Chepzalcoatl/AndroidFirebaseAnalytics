package com.chepzalcoatl.testfirebaseanalytics;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Registra pantalla
        AnalyticsHelper.getInstance(this).RegistraPantallas(this,"MainActivity",this.getLocalClassName());
    }


    public void actionGuardaPropiedades(View view) {
        AnalyticsHelper.getInstance(this).RegistraPropiedadesUsuario("Nombre","Juan");
    }

    public void actionGuardarEvento(View view) {
        AnalyticsHelper.getInstance(this).RegistraEvento("EventoPrueba",new HashMap<String, String>(){{put("ClavePrueba","Valor prueba");}});
    }
}
