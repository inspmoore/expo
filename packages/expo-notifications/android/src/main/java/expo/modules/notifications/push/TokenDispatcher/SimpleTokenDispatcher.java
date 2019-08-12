package expo.modules.notifications.push.TokenDispatcher;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

import expo.modules.notifications.push.TokenDispatcher.engines.Engine;

class SimpleTokenDispatcher implements TokenDispatcher {

  private SharedPreferences sharedPreferences;
  private Engine engine;
  private HashMap<String, OnTokenChangeListener> listeners;
  private Context mContext = null;

  SimpleTokenDispatcher(Context context, Engine engine) {
    sharedPreferences = context.getSharedPreferences("expo.host.exp.notifications.token.dispatcher", Context.MODE_PRIVATE);
    mContext = context;
  }

  @Override
  public void onNewToken(String token, Runnable continuation) {
    String lastToken = sharedPreferences.getString("token", null);
    if (!lastToken.equals(token)) {
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString("token", token);
      editor.commit();

      engine.sendTokenToServer(token, mContext);

      for (String key : listeners.keySet()) {
        OnTokenChangeListener listener = listeners.get(key);
        listener.onTokenChange(engine.generateToken(key, token, mContext));
      }
    }
  }

  @Override
  public void registerForTokenChange(String appId, OnTokenChangeListener onTokenChangeListener) {
    String currentToken = sharedPreferences.getString("token", null);
    String lastTokenSendToApp = sharedPreferences.getString(appId, null);

    if (!currentToken.equals(lastTokenSendToApp)) {
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString(appId,currentToken);
      editor.commit();
    }

    onTokenChangeListener.onTokenChange(engine.generateToken(appId, currentToken, mContext));
    listeners.put(appId, onTokenChangeListener);
  }

  @Override
  public void unregister(String appId) {
    listeners.remove(appId);
  }

}
