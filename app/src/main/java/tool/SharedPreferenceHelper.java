package tool;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by 123 on 2018/5/23.
 */

public class SharedPreferenceHelper {
    public static boolean isLogging(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences("userLogStatus", MODE_PRIVATE);
        return sp.getBoolean("LogStatus", false);
    }
    public static String getTableNameBySP(Context context)
    {
        SharedPreferences preference = context.getSharedPreferences("UserIDAndPassword", MODE_PRIVATE);
        String username = preference.getString("username", "");
        return username;
    }
    public static void  setLoggingStatus(Context context,boolean status)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userLogStatus", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("LogStatus", status);
        editor.apply();
    }

}
