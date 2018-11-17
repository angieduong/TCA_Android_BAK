package com.seta.tollroaddroid.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Convert;
import com.seta.tollroaddroid.app.custom.MyProgressDialog;
import com.seta.tollroaddroid.app.json.CommonResponse;

import static android.view.Gravity.CENTER;

public class BaseActivity extends FragmentActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private MyProgressDialog myProgressDialog;
    private Toast warningToast;
    private BaseActivity activity;
    private DialogInterface.OnClickListener onLogOutBtnClickListener;
    private DialogInterface.OnClickListener dismissListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        onLogOutBtnClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position)
            {
                if(dialog != null)
                {
                    dialog.dismiss();
                }
                logoutNormally();
            }
        };
        dismissListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position)
            {
                dialog.dismiss();
            }
        };
        Convert.mDensity = getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        closeProgressDialog();
        super.onDestroy();
    }

    public void showProgressDialog(String text)
    {
        if (myProgressDialog == null)
        {
            myProgressDialog = MyProgressDialog.show(this, text);;
        }
    }

    public void showProgressDialog()
    {
        showProgressDialog("");
    }

    public void closeProgressDialog()
    {
        if (myProgressDialog != null)
        {
            if(myProgressDialog.isShowing()) {
                myProgressDialog.cancel();
                myProgressDialog.dismiss();
            }

            myProgressDialog = null;
        }
    }

    public void showKeyboard(EditText editText)
    {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null)
        {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public void hideKeyboard(EditText editText)
    {
        if(editText != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                    editText.getWindowToken(), 0);
        }
    }

    public void showToastMessage(String msg)
    {
        if(warningToast != null)
        {
            warningToast.cancel();
        }
        warningToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
//        warningToast.setGravity(CENTER, 0, 0);
//        TextView v = (TextView) warningToast.getView().findViewById(android.R.id.message);
//        if( v != null)
//        {
//            v.setGravity(Gravity.CENTER);
//        }
        warningToast.show();
    }

    public void showDialog(int messageId, int positiveLabelId,
                           DialogInterface.OnClickListener positiveOnClick)
    {
        showDialog("", getString(messageId), getString(positiveLabelId), positiveOnClick, false);
    }

    public void showDialog(String title,String msg, String positiveLabel,
                           DialogInterface.OnClickListener positiveOnClick, boolean isCancelAble)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);/* getString(R.string.app_name) */
        builder.setCancelable(isCancelAble);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnClick);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showDialog(String title,String msg, String positiveLabel,
                           DialogInterface.OnClickListener positiveOnClick,
                           String negativeLabel, DialogInterface.OnClickListener negativeOnClick,
                           boolean isCancelAble)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);/* getString(R.string.app_name) */
        builder.setCancelable(isCancelAble);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnClick);
        builder.setNegativeButton(negativeLabel, negativeOnClick);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void gotoActivity(Context context, Class<?> cla)
    {
        Intent intent = new Intent(context, cla);
        startActivity(intent);
    }

    public void gotoActivity(Context context, Class<?> cla, int flag)
    {
        Intent intent = new Intent(context, cla);
        intent.setFlags(flag);
        startActivity(intent);
    }

    public void gotoActivity(Context context, Class<?> cla, Bundle b)
    {
        Intent intent = new Intent(context, cla);
        intent.putExtras(b);
        startActivity(intent);
    }
    public void gotoActivity(Context context, Class<?> cla, int flag, Bundle b)
    {
        Intent intent = new Intent(context, cla);
        intent.setFlags(flag);
        intent.putExtras(b);
        startActivity(intent);
    }
    public void gotoActivityForResult(Context context, Class<?> cla,
                                      int requestCode)
    {
        Intent intent = new Intent(context, cla);
        startActivityForResult(intent, requestCode);
    }

    public void gotoActivityForResult(Context context, Class<?> cla,
                                      Bundle bundle, int requestCode)
    {
        Intent intent = new Intent(context, cla);
        intent.putExtras(bundle);
        startActivityForResult(intent, requestCode);
    }

    protected void logoutInvalidExpireToken()
    {
        showDialog(R.string.message_invalid_token_and_logout_message,
                R.string.ok, onLogOutBtnClickListener);
    }

    protected void logoutNormally()
    {
        TollRoadsApp.getInstance().setToken("");
        gotoActivity(activity, LandingPageActivity.class,
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    public boolean checkResponse(String response)
    {
        if(response != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            CommonResponse commonResponse = gson.fromJson(response, CommonResponse.class);
            String serverReturnKey = commonResponse.getUniqueID();
            
            if(commonResponse.getSuccess() != 1)
            {
                checkInvalidMessage(commonResponse.getMessage());
                return false;
            }

            return checkUniqueID(serverReturnKey);
        }
        else {
            return false;
        }
    }

    public boolean checkUserNameResponse(String response)
    {
        if(response != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            CommonResponse commonResponse = gson.fromJson(response, CommonResponse.class);

            if(commonResponse.getSuccess() != 1)
            {
                checkInvalidMessage(commonResponse.getMessage());
                return false;
            }
            else if(commonResponse.getStatus() != 200)
            {
                showDialog(getString(R.string.dialog_title_warning),
                        commonResponse.getMessage(), getString(R.string.ok), dismissListener, false);
                return false;
            }
            return true;
        }
        else {
            return false;
        }
    }

    public boolean checkUniqueID(String serverReturnID)
    {
        String ownKey = TollRoadsApp.getInstance().getUniqueID();

        if (serverReturnID != null && ownKey != null) {
            if (serverReturnID.equalsIgnoreCase(ownKey)) {
                return true;
            }
        }
        return false;
    }

    public void checkInvalidMessage(String message)
    {
        if (message.equalsIgnoreCase(getString(R.string.STRING_INVALID_TOKEN))
                || message.equalsIgnoreCase(getString(R.string.STRING_INVALID_TOKEN_2))
                || message.equalsIgnoreCase(getString(R.string.STRING_INVALID_TOKEN_SPAINISH))
                || message.equalsIgnoreCase(getString(R.string.STRING_EXPIRE_TOKEN))
                || message.equalsIgnoreCase(getString(R.string.STRING_EXPIRE_TOKEN_2))
                || message.equalsIgnoreCase(getString(R.string.STRING_EXPIRE_TOKEN_SPAINISH)))
        {
            logoutInvalidExpireToken();
        }
        else
        {
            Log.i(LOG_TAG, "checkInvalidMessage - message:" + message);
            showDialog(getString(R.string.dialog_title_error),
                    message, getString(R.string.ok), dismissListener, false);
        }
    }

    public static void setTotalHeightToListView(ListView listView)
    {
        ListAdapter mAdapter = listView.getAdapter();
        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getCount(); i++)
        {
            View mView = mAdapter.getView(i, null, listView);
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += mView.getMeasuredHeight();
//	        Log.w("HEIGHT " + i, String.valueOf(totalHeight));
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
        Log.e("base","params.height:"+params.height);
        listView.setLayoutParams(params);
        //listView.requestLayout();
    }
}
