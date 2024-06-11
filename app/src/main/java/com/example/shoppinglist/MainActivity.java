package com.example.shoppinglist;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ArrayAdapter<String> productsAdapter;
    private ArrayList<String> productList;
    private ArrayList<Integer> productIdList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_addProduct = findViewById(R.id.btn_AddProduct);
        Button btnn_Back = findViewById(R.id.btnn_Back);
        ListView lv_ShoppingList = findViewById(R.id.lv_ShoppingList);
        SharedPreferences preferences = getSharedPreferences("userPreferences", Activity.MODE_PRIVATE);
        String userId = preferences.getString("userId", "0");

        productList = new ArrayList<>();
        productIdList = new ArrayList<>(); // Initialize the list
        productsAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, productList);
        lv_ShoppingList.setAdapter(productsAdapter);

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://dev.imagit.pl/wsg_zaliczenie/api/items/" + userId;
        Log.d(TAG, "Request URL: " + url);
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String JSON = new String(responseBody);
                Log.d(TAG, "Response: " + JSON);
                try {
                    JSONArray jArray = new JSONArray(JSON);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jObject = jArray.getJSONObject(i);

                        String productName = jObject.getString("ITEM_NAME");
                        String productDescription = jObject.getString("ITEM_DESCRIPTION");
                        int productId = jObject.getInt("ITEM_ID"); // Assuming there is an ID field

                        productList.add(productName + ", " + productDescription);
                        productIdList.add(productId);
                    }
                    productsAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "Request failed: " + error.getMessage());
                if (responseBody != null) {
                    Log.e(TAG, "Response body: " + new String(responseBody));
                }
            }
        });

        lv_ShoppingList.setOnItemClickListener((parent, view, position, id) -> {
            // Check if productIdList is not null and contains the position
            if (productIdList != null && position < productIdList.size()) {
                // Get the selected product ID
                int selectedProductId = productIdList.get(position);

                // Create a dialog to confirm deletion
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.DeleteText)
                        .setMessage(R.string.DeleteConfirmText)
                        .setPositiveButton("Usuń", (dialog, which) -> {
                            // Call the delete API
                            deleteProduct(userId, selectedProductId, position);
                        })
                        .setNegativeButton(R.string.buttonCancel, null)
                        .show();
            } else {
                Log.e(TAG, "Invalid position or productIdList is null");
            }
        });

        btn_addProduct.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        btnn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Zamknięcie bieżącej aktywności
            }
        });
    }

    private void deleteProduct(String userId, int productId, int position) {
        String url = "http://dev.imagit.pl/wsg_zaliczenie/api/item/delete/" + userId + "/" + productId;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.d(TAG, "Delete Response: " + response);
                if (response.equals("OK")) {
                    // Remove the product from the list and notify the adapter
                    productList.remove(position);
                    productIdList.remove(position);
                    productsAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, R.string.DeleteConfirmedText, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.errorDeleteFailed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "Delete Request failed: " + error.getMessage());
                if (responseBody != null) {
                    Log.e(TAG, "Delete Response body: " + new String(responseBody));
                }
                Toast.makeText(MainActivity.this, R.string.errorApi, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

