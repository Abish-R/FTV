package helper;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Review{
        private int rating;
        private String name, description, title, date, reviewImage, profileImage;

        public Review(JSONObject item) throws JSONException {
            title = item.getString("review_title");
            description = item.getString("review_description");
            name = item.getString("customer_name");
            date = item.getString("reviewed_date");
            rating = item.getInt("star_rating");
            reviewImage = item.getString("review_imageurl");
            profileImage = item.getString("profilepic");

        }


        public String getReviewImage() {
            return reviewImage;
        }

        public String getProfileImage() {
            return profileImage;
        }

        public int getRating() {
            return rating;
        }

        public String getName() {
            return "by " + name;
        }

        public String getDescription() {
            return description;
        }

        public String getTitle() {
            return title;
        }

        public String getDate() {
            //todo format date
            return date;
        }
    }