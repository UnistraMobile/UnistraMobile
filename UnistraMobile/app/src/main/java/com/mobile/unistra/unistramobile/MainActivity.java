package com.mobile.unistra.unistramobile;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn_annu = (Button) findViewById(R.id.button_annu);
        btn_annu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goAnnu= new Intent(MainActivity.this,AnnuaireActivity.class);
                startActivity(goAnnu);
            }
        });
        Button btn_calen = (Button) findViewById(R.id.button_calen);
        btn_calen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goCalen= new Intent(MainActivity.this,CalendrierActivity.class);
                startActivity(goCalen);
            }
        });

        Button btn_map = (Button) findViewById(R.id.button_map);
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goMap= new Intent(MainActivity.this,MapsActivity.class);
                startActivity(goMap);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}