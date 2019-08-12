package expo.modules.notifications.push;

import android.content.Context;

public interface Engine {

    void generateToken(String appId, Context context, CompletionHandler completionHandler);

    void onNewToken(String token, Context context);

}
