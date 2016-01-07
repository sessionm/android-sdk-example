package com.sessionm.example.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.sessionm.api.SessionM;
import com.sessionm.api.user.order.data.Order;
import com.sessionm.api.user.receipt.data.Receipt;
import com.sessionm.example.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class OfferDetailsActivity extends AppCompatActivity {

    private List<Order> orders = SessionM.getInstance().getUserActivitiesManager().getOrders();
    private Order orderToShow;
    private Receipt receipt;
    ImageLoader imageLoader = SEApplication.getInstance().getImageLoader();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);

        Intent getOrder = getIntent();
        TextView offerID = (TextView) findViewById(R.id.offer_id);
        TextView header = (TextView) findViewById(R.id.offer_header);
        TextView subheader = (TextView) findViewById(R.id.offer_subheader);
        TextView desc = (TextView) findViewById(R.id.offer_description);
        FeedImageView image = (FeedImageView) findViewById(R.id.offer_image);
        Button redeem = (Button) findViewById(R.id.redeem_button);
        TextView off_redeemed = (TextView) findViewById(R.id.offer_redeemed);
        String orderID = getOrder.getStringExtra("orderID");

        for(int i = 0; i < orders.size(); i++){
            if (orders.get(i).getID().equals(orderID)){
                orderToShow = orders.get(i);
                break;
            }
        }

        offerID.setText("Offer ID: "+orderID);
        header.setText(orderToShow.getHeader());
        subheader.setText(orderToShow.getSubheader());
        desc.setText(orderToShow.getDescription());

        if (orderToShow.getImageURL() != null && !orderToShow.getImageURL().equals("null")) {
            //Returns the SessionM Offer item's image URL, String
            image.setImageUrl(orderToShow.getImageURL(), imageLoader);
            image.setVisibility(View.VISIBLE);
        } else {
            image.setVisibility(View.GONE);
        }

        if(orderToShow.getStatus() == Order.OrderStatus.AVAILABLE){
            redeem.setVisibility(View.VISIBLE);
            off_redeemed.setVisibility(View.GONE);
        }else {
            redeem.setVisibility(View.GONE);
            off_redeemed.setVisibility(View.VISIBLE);
            off_redeemed.setText("REDEEMED on "+orderToShow.getUpdatedTime());
        }

    }

    public void redeemOfferOrder(View view){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("retailer", "Your Retailer Here");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<Receipt> receipts = SessionM.getInstance().getUserActivitiesManager().getReceipts();
        for(int i = 0; i< receipts.size(); i++) {
            if (receipts.get(i).getOrder() != null && receipts.get(i).getOrder().getID().equals(orderToShow.getID())) {
                receipt = receipts.get(i);
                break;
            }
        }

        orderToShow.updateStatus(Order.OrderStatus.REDEEMED, jsonObject);
        if(receipt != null){
            SessionM.getInstance().logAction("order_redeemed", 1, receipt.getPayloads());
        }else{
            Toast.makeText(getApplicationContext(), "Order's Receipt is null using Campaign ID to log Action", Toast.LENGTH_LONG);
            SessionM.getInstance().logAction("order_redeemed", 1, orderToShow.getCampaignID());
        }

        if(orderToShow.getStatus() == Order.OrderStatus.REDEEMED){
            findViewById(R.id.redeem_button).setVisibility(View.GONE);
            findViewById(R.id.offer_redeemed).setVisibility(View.VISIBLE);
        }

    }

}
