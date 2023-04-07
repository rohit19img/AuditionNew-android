package com.img.audition.cashfree;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cashfree.pg.CFPaymentService;
import com.img.audition.R;
import com.img.audition.globalAccess.MyApplication;
import com.img.audition.network.APITags;
import com.img.audition.network.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.cashfree.pg.CFPaymentService.PARAM_APP_ID;
import static com.cashfree.pg.CFPaymentService.PARAM_CUSTOMER_EMAIL;
import static com.cashfree.pg.CFPaymentService.PARAM_CUSTOMER_NAME;
import static com.cashfree.pg.CFPaymentService.PARAM_CUSTOMER_PHONE;
import static com.cashfree.pg.CFPaymentService.PARAM_NOTIFY_URL;
import static com.cashfree.pg.CFPaymentService.PARAM_ORDER_AMOUNT;
import static com.cashfree.pg.CFPaymentService.PARAM_ORDER_ID;
import static com.cashfree.pg.CFPaymentService.PARAM_ORDER_NOTE;
import static com.cashfree.pg.CFPaymentService.PARAM_PAYMENT_MODES;

public class PaymentActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    String appid,secret,orderid,paymentOption,amount,id,phone,email,name,checksum;
    boolean UPI = false;
    String orderNote = "Recharge Wallet";

    String MODE = "TEST";    // TEST or PROD

    SessionManager session;

    MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        myApplication = new MyApplication(this);
        requestQueue = Volley.newRequestQueue(PaymentActivity.this);
        session = new SessionManager(this);

        if(MODE.equals("TEST")) {
            appid = "11334052512e79cd7c2c354190043311";
            secret = "a421ee18eb5b7e7575d776891541508fedf062e6";
        } else {
            appid = "162723c3e28c09418c47c8dbc8327261";
            secret = "2c182e6b42f9c1de8b3a59ac88a70ba95fa6985f";
        }

        amount = getIntent().getExtras().getString("price");
        orderid = getIntent().getExtras().getString("orderid");
//        UPI = getIntent().getExtras().getBoolean("UPI");
//        paymentOption = getIntent().getExtras().getString("paymentOption");

        id = getIntent().getExtras().getString("id");
//        if (session.getMobile().equals("0") || session.getMobile().equals("") || session.getMobile() == null){
            phone = "7777777777";
//        }else {
//            phone = session.get;
//        }
        email = "testuser@gmail.com";
        name = session.getUserName();

        if(MODE.equals("TEST"))
            CallVolley("https://test.cashfree.com/api/v2/cftoken/order");  // test
        else
            CallVolley("https://api.cashfree.com/api/v2/cftoken/order");  // Live

    }

    JSONObject jsonObject = new JSONObject();

    public void CallVolley(final String a)
    {

        try {
            jsonObject.put("orderId",orderid);
            jsonObject.put("orderAmount",amount);
            jsonObject.put("orderCurrency","INR");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("params",jsonObject.toString());

        try {

            JsonObjectRequest strRequest = new JsonObjectRequest(Request.Method.POST, a, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    try {
                        Log.i("Response is",response.toString());
                        JSONObject jsonObject = new JSONObject(response.toString());
                        checksum = jsonObject.getString("cftoken");
                        triggerPayment(UPI);
                    }
                    catch (JSONException je)
                    {
                        je.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    AlertDialog.Builder d = new AlertDialog.Builder(PaymentActivity.this);
                    d.setTitle("Something went wrong");
                    d.setCancelable(false);
                    d.setMessage("Something went wrong, Please try again");
                    d.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CallVolley(a);
                        }
                    });
                    d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            finish();
                        }
                    });
                }
            })

            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("Content-Type","application/json");
                    map.put("x-client-id", appid);
                    map.put("x-client-secret", secret);

                    Log.e("MAP", map.toString());

                    return map;
                }
            };
            strRequest.setShouldCache(false);
            strRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(strRequest);
        }
        catch (Exception e) {
            Toast.makeText(this, "--"+e, Toast.LENGTH_SHORT).show();
        }

    }


    private void triggerPayment(boolean isUpiIntent) {
        /*
         * token can be generated from your backend by calling cashfree servers. Please
         * check the documentation for details on generating the token.
         * READ THIS TO GENERATE TOKEN: https://bit.ly/2RGV3Pp
         */
        String token = checksum;


        /*
         * stage allows you to switch between sandboxed and production servers
         * for CashFree Payment Gateway. The possible values are
         *
         * 1. TEST: Use the Test server. You can use this service while integrating
         *      and testing the CashFree PG. No real money will be deducted from the
         *      cards and bank accounts you use this stage. This mode is thus ideal
         *      for use during the development. You can use the cards provided here
         *      while in this stage: https://docs.cashfree.com/docs/resources/#test-data
         *
         * 2. PROD: Once you have completed the testing and integration and successfully
         *      integrated the CashFree PG, use this value for stage variable. This will
         *      enable live transactions
         */
        String stage = MODE;

        /*
         * appId will be available to you at CashFree Dashboard. This is a unique
         * identifier for your app. Please replace this appId with your appId.
         * Also, as explained below you will need to change your appId to prod
         * credentials before publishing your app.
         */

        Map<String, String> params = new HashMap<>();

        params.put(PARAM_APP_ID, appid);
        params.put(PARAM_ORDER_ID, orderid);
        params.put(PARAM_ORDER_AMOUNT, amount);
        params.put(PARAM_ORDER_NOTE, orderNote);
        params.put(PARAM_CUSTOMER_NAME, name);
        params.put(PARAM_CUSTOMER_PHONE, phone);
        params.put(PARAM_CUSTOMER_EMAIL,email);
//        params.put(PARAM_PAYMENT_MODES,paymentOption);
        params.put(PARAM_NOTIFY_URL, APITags.APIBASEURL +"webhook_detail");

        Log.i("params",params.toString());

        for(Map.Entry entry : params.entrySet()) {
            Log.d("CFSKDSample", entry.getKey() + " " + entry.getValue());
        }

        CFPaymentService cfPaymentService = CFPaymentService.getCFPaymentServiceInstance();
        cfPaymentService.setOrientation(0);

        if (isUpiIntent) {
            // Use the following method for initiating UPI Intent Payments
            cfPaymentService.upiPayment(this, params, token, stage);
        }
        else {
            // Use the following method for initiating regular Payments
            cfPaymentService.doPayment(this, params, token, stage);
        }

    }

    public void doPayment(View view) {
        triggerPayment(false);
    }

    public void upiPayment(View view) {
        triggerPayment(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("requestCode", String.valueOf(requestCode));
        Log.i("resultCode", String.valueOf(resultCode));
        Log.i("data", String.valueOf(data.toString()));

        if (data != null) {
            final Bundle bundle = data.getExtras();

            if(bundle != null) {

                for (String key : bundle.keySet()) {
                    if (bundle.getString(key) != null) {
                        Log.d("key value", key + " : " + bundle.getString(key));
                    }
                }

                if (bundle.getString("txStatus").equals("SUCCESS")) {
                    Toast.makeText(this, "Payment Done", Toast.LENGTH_SHORT).show();
                    finish();
                } else if(bundle.getString("txStatus").equals("FAILED")){
                    Log.i("where","failed");
                    Toast.makeText(this, bundle.getString("txMsg"), Toast.LENGTH_SHORT).show();
                    finish();
                } else if(bundle.getString("txStatus").equals("CANCELLED")){
                    Log.i("where","cancelled");
                    Toast.makeText(this, bundle.getString("txMsg"), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    if(bundle.getString("txMsg") != null) {
                        Toast.makeText(this, bundle.getString("txMsg"), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Transaction Cancelled", Toast.LENGTH_SHORT).show();
                        finish();

                    }
                }
            } else
                finish();
        }
    }

    @Override
    public void onBackPressed() {
        SweetAlertDialog d = new SweetAlertDialog(PaymentActivity.this, SweetAlertDialog.WARNING_TYPE);
        d.setContentText("Are you sure to cancel your transaction ?")
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        finish();
                    }
                })
                .setCancelText("No")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        triggerPayment(UPI);
                    }
                });
        try {if(d!=null){d.show();}}catch (Exception f){f.printStackTrace();}

    }
}

