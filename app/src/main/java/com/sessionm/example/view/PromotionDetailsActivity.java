package com.sessionm.example.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.sessionm.api.SessionM;
import com.sessionm.api.message.data.MessageData;
import com.sessionm.example.R;

import org.json.JSONException;

import java.util.List;

public class PromotionDetailsActivity extends AppCompatActivity {

    private List<MessageData> campaigns = SessionM.getInstance().getMessageManager().getMessagesList();
    private MessageData promToShow;
    ImageLoader imageLoader = SEApplication.getInstance().getImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_details);

        Intent getPromo = getIntent();
        FeedImageView image = (FeedImageView) findViewById(R.id.promo_detail_image);
        TextView header = (TextView) findViewById(R.id.promo_detail_header);
        TextView subheader = (TextView) findViewById(R.id.promo_detail_subheader);
        TextView desc = (TextView) findViewById(R.id.promo_detail_description);
        TextView legal = (TextView) findViewById(R.id.promo_legal);

        String promoID = getPromo.getStringExtra("messageID");
        for (int i = 0; i < campaigns.size(); i++) {
            if (campaigns.get(i).getMessageID().equals(promoID)) {
                promToShow = campaigns.get(i);
            }
        }

        if (promToShow == null) {
            Toast.makeText(this, "Expired!", Toast.LENGTH_SHORT).show();
            finish();
        } else {

            if (promToShow.getImageURL() != null && !promToShow.getImageURL().equals("null")) {
                image.setImageUrl(promToShow.getImageURL(), imageLoader);
                image.setVisibility(View.VISIBLE);
            } else {
                image.setVisibility(View.GONE);
            }

            header.setText(promToShow.getHeader());
            subheader.setText(promToShow.getSubHeader());
            desc.setText(promToShow.getDescription());

            if (promToShow.getData() != null && !promToShow.getData().equals(null)) {
                try {
                    String legalData = promToShow.getData().getString("legal");
                    if (!legal.equals(null)) {
                        legal.setText(legalData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void launchAd(View v) {
        if (promToShow.getActionType() == MessageData.MessageActionType.FULL_SCREEN) {
            //Launch the SessionM Portal Activity from current Promotion
            SessionM.getInstance().getMessageManager().executeMessageAction(promToShow.getMessageID());
            //Can also be implemented by presentActivity
            //SessionM.getInstance().presentActivity(SessionM.ActivityType.PORTAL, promToShow.getActionURL());
        } else if (promToShow.getActionType() == MessageData.MessageActionType.DEEP_LINK) {
            handleDeepLinkAd(promToShow.getActionURL());
        } else {
            Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse(promToShow.getActionURL()));
            startActivity(external);
        }
    }

    //Call this method to handle Deep link URLs
    private void handleDeepLinkAd(String url) {
        SessionM.getInstance().presentActivity(SessionM.ActivityType.PORTAL, url);
    }

}
