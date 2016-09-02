package helper;

import org.json.JSONException;

public interface ProductRemovedListener {
        public void onProductRemoved(int position) throws JSONException;
    }