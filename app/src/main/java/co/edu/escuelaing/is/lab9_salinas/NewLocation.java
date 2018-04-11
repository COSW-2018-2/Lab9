package co.edu.escuelaing.is.lab9_salinas;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;

public class NewLocation extends AppCompatActivity {
    private EditText name;
    private EditText description;
    private EditText longitude;
    private EditText latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);
        name = (EditText) findViewById(R.id.name);
        description = (EditText) findViewById(R.id.description);
        longitude = (EditText) findViewById(R.id.longitude);
        latitude = (EditText) findViewById(R.id.latitude);
    }


    public void onAddLocation(View view) {
        if (name.getText().toString().equals("") || description.getText().toString().equals("") || latitude.getText().toString().equals("") || longitude.getText().toString().equals("")) Toast.makeText(this,"You have to complete all the blanks",Toast.LENGTH_SHORT).show();
        else{
            Location targetLocation = new Location(name.getText().toString());//provider name is unnecessary
            targetLocation.setLatitude(Double.parseDouble(latitude.getText().toString()));//your coords of course
            targetLocation.setLongitude(Double.parseDouble(longitude.getText().toString()));

            Intent passInfToMap = new Intent();
            passInfToMap.putExtra("newLocation",targetLocation);
            setResult(this.RESULT_OK,passInfToMap);
            finish();
        }

    }
}
