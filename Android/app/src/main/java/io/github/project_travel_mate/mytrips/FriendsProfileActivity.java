package io.github.project_travel_mate.mytrips;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.project_travel_mate.R;
import objects.Trip;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.Constants.API_LINK_V2;
import static utils.Constants.EXTRA_MESSAGE_FRIEND_ID;
import static utils.Constants.EXTRA_MESSAGE_TRIP_OBJECT;
import static utils.Constants.USER_TOKEN;
import static utils.DateUtils.getDate;
import static utils.DateUtils.rfc3339ToMills;


public class FriendsProfileActivity extends AppCompatActivity {

    @BindView(R.id.display_image)
    ImageView friendDisplayImage;
    @BindView(R.id.display_email)
    TextView friendsEmail;
    @BindView(R.id.display_name)
    TextView friendUserName;
    @BindView(R.id.display_joining_date)
    TextView friendJoiningDate;
    @BindView(R.id.ib_edit_display_name)
    ImageView changeUserName;
    @BindView(R.id.change_image)
    ImageView changeProfileImage;
    @BindView(R.id.display_status)
    EditText displayStatus;
    @BindView(R.id.status_icon)
    ImageView statusIcon;
    @BindView(R.id.profile_icon)
    ImageView profileIcon;
    @BindView(R.id.date_joined_icon)
    ImageView dateJoinedIcon;
    @BindView(R.id.email_icon)
    ImageView emailIcon;
    @BindView(R.id.ib_edit_display_status)
    ImageButton changeStatus;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;

    private SharedPreferences mSharedPreferences;
    private String mToken;
    private Handler mHandler;
    private int mFriendId;
    private Trip mTrip;
    private String mTripId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        //disabling option to edit username
        changeUserName.setVisibility(View.INVISIBLE);
        //disabling option to edit profile picture
        changeProfileImage.setVisibility(View.INVISIBLE);
        //disabling option to edit status
        changeStatus.setVisibility(View.INVISIBLE);
        //displaying no status so that default status is not displayed
        displayStatus.setText(" ");
        
        Intent intent = getIntent();
        mFriendId = (int) intent.getSerializableExtra(EXTRA_MESSAGE_FRIEND_ID);
        mTrip = (Trip) intent.getSerializableExtra(EXTRA_MESSAGE_TRIP_OBJECT);
        mTripId = mTrip.getId();
        mHandler = new Handler(Looper.getMainLooper());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPreferences.getString(USER_TOKEN, null);
        getFriendDetails(String.valueOf(mFriendId));
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friend_profile_menu, menu);
        return true;
    }

    public static Intent getStartIntent(Context context, int id) {
        Intent intent = new Intent(context, FriendsProfileActivity.class);
        intent.putExtra(EXTRA_MESSAGE_FRIEND_ID, id);
        return intent;
    }

    private void getFriendDetails(final String friendId) {
        friendDisplayImage.setVisibility(View.GONE);
        friendJoiningDate.setVisibility(View.GONE);
        friendsEmail.setVisibility(View.GONE);
        friendUserName.setVisibility(View.GONE);
        profileIcon.setVisibility(View.GONE);
        emailIcon.setVisibility(View.GONE);
        dateJoinedIcon.setVisibility(View.GONE);
        statusIcon.setVisibility(View.GONE);

        String uri;
        if (friendId != null)
            uri = API_LINK_V2 + "get-user/" + friendId;
        else
            uri = API_LINK_V2 + "get-user";
        Log.v("EXECUTING", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
                .header("Authorization", "Token " + mToken)
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = Objects.requireNonNull(response.body()).string();

                mHandler.post(() -> {
                    try {
                        JSONObject object = new JSONObject(res);
                        String userName = object.getString("username");
                        String firstName = object.getString("first_name");
                        String lastName = object.getString("last_name");
                        String fullname = firstName + " " + lastName;
                        String imageURL = object.getString("image");
                        String dateJoined = object.getString("date_joined");
                        String status = object.getString("status");
                        Long dateTime = rfc3339ToMills(dateJoined);
                        String date = getDate(dateTime);
                        Picasso.with(FriendsProfileActivity.this).load(imageURL)
                                .placeholder(R.drawable.default_user_icon)
                                .error(R.drawable.default_user_icon).into(friendDisplayImage);
                        if (!status.equals("null")) {
                            displayStatus.setText(status);
                            friendUserName.setText(fullname);
                            friendJoiningDate.setText(String.format(getString(R.string.text_joining_date), date));
                            friendsEmail.setText(userName);
                            dateJoinedIcon.setVisibility(View.VISIBLE);
                            friendJoiningDate.setVisibility(View.VISIBLE);

                        } else {
                            displayStatus.setText(fullname);
                            friendsEmail.setText(String.format(getString(R.string.text_joining_date), date));
                            friendUserName.setText(userName);
                            dateJoinedIcon.setVisibility(View.GONE);
                            friendJoiningDate.setVisibility(View.GONE);
                            statusIcon.setImageResource(R.drawable.ic_person_black_24dp);
                            profileIcon.setImageResource(R.drawable.ic_email_black_24dp);
                            emailIcon.setImageResource(R.drawable.baseline_date_range_black);
                        }
                        setTitle(fullname);
                        statusIcon.setVisibility(View.VISIBLE);
                        friendsEmail.setVisibility(View.VISIBLE);
                        friendDisplayImage.setVisibility(View.VISIBLE);
                        profileIcon.setVisibility(View.VISIBLE);
                        emailIcon.setVisibility(View.VISIBLE);
                        friendUserName.setVisibility(View.VISIBLE);
                        animationView.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ERROR : ", "Message : " + e.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // return back to trip activity
                finish();
                return true;
            case R.id.action_remove_friend:
                ContextThemeWrapper crt = new ContextThemeWrapper(this, R.style.AlertDialog);
                AlertDialog.Builder builder = new AlertDialog.Builder(crt);
                builder.setMessage(R.string.remove_friend_message)
                        .setPositiveButton(R.string.positive_button,
                                (dialog, which) -> {
                                    removeFriend();
                                })
                        .setNegativeButton(android.R.string.cancel,
                                (dialog, which) -> {

                                });
                builder.create().show();
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void removeFriend() {

        String uri = API_LINK_V2 + "remove-friend-from-trip/" + mTripId + "/" + mFriendId;
        Log.v("EXECUTING", uri);
        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
                .header("Authorization", "Token " + mToken)
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                final String res = Objects.requireNonNull(response.body()).string();
                mHandler.post(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(FriendsProfileActivity.this, R.string.removed_friend_message,
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(FriendsProfileActivity.this, MyTripInfoActivity.class);
                        intent.putExtra(EXTRA_MESSAGE_TRIP_OBJECT, mTrip);
                        startActivity(intent);
                        finish();
                    } else
                        Toast.makeText(FriendsProfileActivity.this, res, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}