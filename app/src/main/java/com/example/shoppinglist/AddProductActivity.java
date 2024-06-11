package com.example.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.w3c.dom.Text;

import cz.msebera.android.httpclient.Header;

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "AddProductActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        Button btn_SubmitProduct = findViewById(R.id.btn_SubmitProduct);
        Button btn_Back = findViewById(R.id.btn_Back);
        TextView et_ProductName = findViewById(R.id.et_ProductName);
        TextView et_ProductDescription = findViewById(R.id.et_ProductDescription);

        SharedPreferences preferences = getSharedPreferences("userPreferences", Activity.MODE_PRIVATE);
        String userId = preferences.getString("userId", "0");

        btn_SubmitProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = et_ProductName.getText().toString();
                String description = et_ProductDescription.getText().toString();

                if (name.isEmpty() || description.isEmpty()) {
                    Toast.makeText(AddProductActivity.this, R.string.ErrorEmptyFields, Toast.LENGTH_LONG).show();
                    return;
                }

                String url = "http://dev.imagit.pl/wsg_zaliczenie/api/item/add";
                Log.d(TAG, "Request URL: " + url);

                RequestParams params = new RequestParams();
                params.put("user", userId);
                params.put("name", name);
                params.put("desc", description);
                Log.d(TAG, "Request Params: " + params.toString());

                AsyncHttpClient client = new AsyncHttpClient();
                client.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody);
                        Log.d(TAG, "Response: " + response);

                        if (response.equals("OK")) {
                            Toast.makeText(AddProductActivity.this, R.string.productAdded, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AddProductActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AddProductActivity.this, R.string.errorApi, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(TAG, "Request failed: " + error.getMessage());
                        if (responseBody != null) {
                            Log.e(TAG, "Response body: " + new String(responseBody));
                        }
                        Toast.makeText(AddProductActivity.this, R.string.errorTitle, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
