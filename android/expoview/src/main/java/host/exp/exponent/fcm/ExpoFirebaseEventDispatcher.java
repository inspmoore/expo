package host.exp.exponent.fcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;

import com.google.firebase.messaging.RemoteMessage;

import host.exp.exponent.notifications.PushNotificationHelper;

public class ExpoFirebaseEventDispatcher extends IntentService {

  public final static String METHOD_KEY = "METHOD_KEY";
  public final static String ON_NEW_TOKEN = "onNewToken";
  public final static String ON_NEW_MESSAGE = "onNewMessage";

  public final static String TOKEN_KEY = "tokenKey";
  public final static String MESSAGE_KEY = "messageKey";

  public ExpoFirebaseEventDispatcher() {
    super("ExpoFirebaseEventDispatcher");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    switch (intent.getStringExtra(METHOD_KEY)) {
      case ON_NEW_TOKEN:
        onNewToken(intent.getStringExtra(TOKEN_KEY));
        break;
      case ON_NEW_MESSAGE:
        onNewMessage(intent.getBundleExtra(MESSAGE_KEY));
        break;
      default:
        break;
    }
  }

  private void onNewMessage(Bundle message) {

    PushNotificationHelper.getInstance().onMessageReceived(
        this,
        message.getString("experienceId"),
        message.getString("channelId"),
        message.getString("message"),
        message.getString("body"),
        message.getString("title"),
        message.getString("categoryId")
    );

    RemoteMessage remoteMessage = new RemoteMessage(message);

  }

  private void onNewToken(String token) {
    FcmRegistrationIntentService.registerForeground(getApplicationContext(), token);

    

  }

}
