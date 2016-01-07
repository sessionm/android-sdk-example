package com.sessionm.example.view.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sessionm.api.AchievementActivity;
import com.sessionm.api.AchievementActivity.AchievementDismissType;
import com.sessionm.api.AchievementActivityIllegalStateException;
import com.sessionm.api.AchievementData;
import com.sessionm.example.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * By providing appropriate achievement data from the server, the SessionM platform enables developers to blend
 * native achievement displays into applications for a more integrated user experience.
 * Using native achievements helps maintain branding along with a more native look and feel.
 *
 * This class is meant to provide an example of how to implement a custom achievement.
 * The purposes of this class is for demonstration only. It is not meant to be used in production.
 * 
 * It provides two styles of presentation.
 * SCALE_UP_SCALE_DOWN - Achievement appears from nowhere by scaling it's height.
 * SLIDE_IN_SLIDE_OUT - Achievement slides into view from left.
 * 
 * Here is how it you might go about presenting this achievement:
 * 
 * CustomAchievement customAchievement = (CustomAchievement) getLayoutInflater().inflate(R.layout.custom_achievement, null);				
 * customAchievement.setAchievementData(achievement);
 * customAchievement.setAchievementViewGroup(mainLayout);
 * customAchievement.setAchievementPresentationStyle(AchievementPresentationStyle.SLIDE_IN_SLIDE_OUT);
 * customAchievement.present();
 *
 * Please see http://www.sessionm.com/documentation/android-custom-achievement.php for more info.
 */

public class CustomAchievementView extends RelativeLayout {

	/**
	 * An interface that publishes when the achievement is presented/dismissed.
	 */
	public interface CustomAchievementListener {
		public void onDismiss(CustomAchievementView customAchievement);
		public void onPresent(CustomAchievementView customAchievement);
	}

	/**
	 * Enum for encoding how you can present your achievement.
	 */
	public enum AchievementPresentationStyle {
		SCALE_UP_SCALE_DOWN,
		SLIDE_IN_SLIDE_OUT
	}
	
	private enum AchievementState {
		INITIALIZED, PRESENTED, CLAIMED, DISMISSED
	}
	

	private class DismissTask extends TimerTask {

		@Override
		public void run() {
			((Activity) CustomAchievementView.this.getContext())
					.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							dismiss(true);

						}

					});
		}

	}

	private class TransitionTask extends TimerTask {

		@Override
		public void run() {
			((Activity) CustomAchievementView.this.getContext())
					.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							transition();

						}

					});

		}
	}

	/**
	 * How long to wait before dismissing the achievement.
	 * It is recommended to give users ample to engage with achievement.
	 */
	private static final int DEFAULT_DISMISS_TIME = 15 * 1000;
	
	
	/**
	 * How long to wait before transitioning content of the achievement.
	 */
	private static final int DEFAULT_TRANSITION_DELAY = 4 * 1000;
	
	private static final String TAG = "CustomAchievement";
	private AchievementActivity achievementActivity;
	private Timer dismissTimer;
	private Timer transitionTimer;
	private AchievementState state;
	private TextView initialTextView;
	private TextView achievementTextView;
	private ImageView closeButton;
	private CustomAchievementListener achievementListener;
	private ViewGroup achievementViewGroup;
	private AchievementPresentationStyle presentationStyle = AchievementPresentationStyle.SLIDE_IN_SLIDE_OUT;

	/**
	 * Constructor
	 * @param context Context
	 */
	public CustomAchievementView(Context context) {
		super(context);
		LayoutParams layoutParams = new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, getResources()
						.getDimensionPixelSize(R.dimen.achievement_height));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.bottomMargin = getResources().getDimensionPixelSize(
				R.dimen.achievement_height);
		setLayoutParams(layoutParams);
	}

	/**
	 * Constructor
	 * @param context Context
	 * @param attributes AttributeSet
	 */
	public CustomAchievementView(Context context, AttributeSet attributes) {
		super(context, attributes);
		LayoutParams layoutParams = new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, getResources()
						.getDimensionPixelSize(R.dimen.achievement_height));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.bottomMargin = getResources().getDimensionPixelSize(
				R.dimen.achievement_height);
		setLayoutParams(layoutParams);
	}

	public void setAchievementListener(CustomAchievementListener listener) {
		this.achievementListener = listener;
	}

	public void setAchievementViewGroup(ViewGroup achievementViewGroup) {
		this.achievementViewGroup = achievementViewGroup;
	}

	/**
	 * This will populate the view will all the achievement data.
	 * @param achievement Achievement Data
	 */
	public void setAchievementData(AchievementData achievement) {
		achievementActivity = new AchievementActivity(achievement);
		initialTextView = (TextView) findViewById(R.id.unlockedTextView);
		achievementTextView = (TextView) findViewById(R.id.achievementDetailsTextView);
		achievementTextView.setText(Html.fromHtml("<b>" + achievement.getName()
				+ "</b> +" + achievement.getMpointValue() + " mPOINTS"));
		closeButton = (ImageView) findViewById(R.id.closeButton);
		closeButton.setBackgroundColor(Color.TRANSPARENT);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss(true);
			}
		});
		initializeSubViews();
		setState(AchievementState.INITIALIZED);
	}

	/**
	 * Present the achievement.
	 * Must be called on the main thread.
	 * Be sure to set achievement data, and achievement view group before calling this.
	 */
	public void present() {
		if(getState() != AchievementState.INITIALIZED) {
			if(Log.isLoggable(TAG, Log.ERROR)) {
				Log.e(TAG, "Achievement presented without achievement data.");
			}
			return;
		}
		achievementViewGroup.addView(this, this.getLayoutParams());
		Animation presentationAnimation;
		if(this.presentationStyle == AchievementPresentationStyle.SLIDE_IN_SLIDE_OUT) {
			presentationAnimation = AnimationUtils.loadAnimation(this.getContext(), R.anim.left_to_right);
		} else {
			presentationAnimation = AnimationUtils.loadAnimation(this.getContext(), R.anim.scale_up);
		}
				
		this.startAnimation(presentationAnimation);
		try {
			//NOTE: after we begin presenting we notify the SDK that achievement was presented.
			achievementActivity.notifyPresented();
			setState(AchievementState.PRESENTED);
			scheduleDismissTimer();
			scheduleTransitionTimer();
		} catch (AchievementActivityIllegalStateException e) {
			if (Log.isLoggable(TAG, Log.ERROR)) {
				Log.e(TAG, "Error presenting achievement", e);
			}
		}
	}
	
	/**
	 * Dismiss the achievement from view.
	 * @param animated - determine whether dismissal is animated or not
	 */
	public void dismiss(final boolean animated) {
		AchievementState currentState = getState();
		if (currentState != AchievementState.PRESENTED) {
			return;
		}
		cancelDismissTimer();
		cancelTransitionTimer();
		if (animated) {
			Animation dismissAnimation;
			if(this.presentationStyle == AchievementPresentationStyle.SLIDE_IN_SLIDE_OUT) {
				dismissAnimation =  AnimationUtils.loadAnimation(
						CustomAchievementView.this.getContext(), R.anim.right_to_left);
			} else {
				dismissAnimation =  AnimationUtils.loadAnimation(
						CustomAchievementView.this.getContext(), R.anim.scale_down);
			}
			CustomAchievementView.this.startAnimation(dismissAnimation);
			dismissAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					CustomAchievementView.this.post(new Runnable() {

						@Override
						public void run() {
							finishDismissal();
						}
					});
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationStart(Animation animation) {
				}

			});
		} else {
			finishDismissal();
		}
	}
	
	/**
	 * Determines how achievement will be presented.
	 * @param presentationStyle Presentation style
	 */
	public void setAchievementPresentationStyle(AchievementPresentationStyle presentationStyle) {
		this.presentationStyle = presentationStyle;
	}

	//private
	
	private void scheduleTransitionTimer() {
		transitionTimer = new Timer();
		transitionTimer
				.schedule(new TransitionTask(), DEFAULT_TRANSITION_DELAY);
	}

	private void scheduleDismissTimer() {
		dismissTimer = new Timer();
		dismissTimer.schedule(new DismissTask(), DEFAULT_DISMISS_TIME);
	}

	private void cancelDismissTimer() {
		if (dismissTimer != null) {
			dismissTimer.cancel();
			dismissTimer = null;
		}
	}

	private void cancelTransitionTimer() {
		if (transitionTimer != null) {
			transitionTimer.cancel();
			transitionTimer = null;
		}
	}
	
	//Claim the achievement only called when the achievement is tapped
	private void claim() {
		try {
			//If the user engages with the achievement we notify the SDK it was claimed.
			achievementActivity.notifyDismissed(AchievementDismissType.CLAIMED);
			setState(AchievementState.CLAIMED);
			dismiss(false);
		} catch (AchievementActivityIllegalStateException e) {
			if (Log.isLoggable(TAG, Log.ERROR)) {
				Log.e(TAG, "Error presenting achievement", e);
			}
		}
	}

	private void finishDismissal() {
		try {
			achievementViewGroup.removeView(CustomAchievementView.this);
			if (getState() != AchievementState.CLAIMED) {
				//If the user didn't claim the achievement and it was dismissed we
				//let the SDK know it was cancelled.
				achievementActivity
						.notifyDismissed(AchievementDismissType.CANCELLED);
			}
			setState(AchievementState.DISMISSED);
			;
			if (achievementListener != null) {
				achievementListener.onDismiss(this);
			}
		} catch (AchievementActivityIllegalStateException e) {
			if (Log.isLoggable(TAG, Log.ERROR)) {
				Log.e(TAG, "Error presenting achievement", e);
			}
		}
	}

	private AchievementState getState() {
		return state;
	}

	private void setState(AchievementState state) {

		switch (state) {
		case INITIALIZED:
			if (this.state != null) {
				throw new IllegalStateException("Incorrect state transition."
						+ this.state);
			}
			break;
		case PRESENTED:
			if (this.state != AchievementState.INITIALIZED) {
				throw new IllegalStateException("Incorrect state transition."
						+ this.state);
			}
			break;
		case DISMISSED:
			if (!(this.state == AchievementState.PRESENTED || this.state == AchievementState.CLAIMED)) {
				if (Log.isLoggable(TAG, Log.WARN)) {
					Log.w(TAG,
							"Dismissing already dismissed achievement. This is probably ok.");
				}
			}
			break;
		case CLAIMED:
			if (this.state != AchievementState.PRESENTED) {
				throw new IllegalStateException("Incorrect state transition."
						+ this.state);
			}
			break;
		default:
			break;
		}

		this.state = state;
	}
	
	private void initializeSubViews() {
		closeButton = (ImageView) findViewById(R.id.closeButton);
		closeButton.setBackgroundColor(Color.TRANSPARENT);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss(true);
			}
		});
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				claim();
			}
		});
	}
	
	private void transition() {
		if (getState().equals(AchievementState.DISMISSED)) {
			return;
		}

		Animation toBottom = AnimationUtils.loadAnimation(
				CustomAchievementView.this.getContext(), R.anim.to_bottom);
		initialTextView.startAnimation(toBottom);
		toBottom.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (getState().equals(AchievementState.DISMISSED)) {
					return;
				}

				CustomAchievementView.this.post(new Runnable() {

					@Override
					public void run() {
						initialTextView.setVisibility(View.GONE);
						achievementTextView.setVisibility(View.VISIBLE);

						Animation fromTop = AnimationUtils.loadAnimation(
								CustomAchievementView.this.getContext(),
								R.anim.from_top);
						achievementTextView.startAnimation(fromTop);
					}
				});
			}
		});
	}

}
