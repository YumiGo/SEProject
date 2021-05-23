package com.example.movie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PayActivity extends AppCompatActivity {

    EditText editName;
    EditText editPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        editName=findViewById(R.id.editName);
        editPrice=findViewById(R.id.editPrice);

        editName.setText("Movie");
        editPrice.setText("14000");

        // 버튼 클릭 이벤트
        Button buttonPay = findViewById(R.id.buttonPay);
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                String price = editPrice.getText().toString();

                KakaopayActivity kakaopayActivity = new KakaopayActivity(name, price);

                Intent intent = new Intent(getApplicationContext(), kakaopayActivity.getClass());
                startActivity(intent);
            }
        });
    }
}
