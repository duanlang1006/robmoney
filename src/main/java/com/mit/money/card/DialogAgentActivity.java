package com.mit.money.card;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.mit.fuli.ui.views.PopupAlertDialog;
import com.mit.mitanalytics.UmengAgentActivity;

public class DialogAgentActivity extends UmengAgentActivity implements DialogInterface.OnClickListener,DialogInterface.OnCancelListener{
    private String title;
    private String msg;
    private Intent extraIntent;
    private PopupAlertDialog alertDialog;

    public static Intent getIntent(Context context,String title,String msg,Intent extraIntent){
        Intent intent = new Intent(context, DialogAgentActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("msg", msg);
        intent.putExtra("extraIntent", extraIntent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title = getIntent().getStringExtra("title");
        msg = getIntent().getStringExtra("msg");
        extraIntent = getIntent().getParcelableExtra("extraIntent");

        PopupAlertDialog.Builder dialog = new PopupAlertDialog.Builder(this);
        alertDialog = dialog.setMessage(msg)
                .setTitle(title)
                .setOnCancelListener(this)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this)
                .show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        finish();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch(which){
            case PopupAlertDialog.BUTTON_NEGATIVE:
                alertDialog.dismiss();
                finish();
                break;
            case PopupAlertDialog.BUTTON_POSITIVE:
                try{
                    startActivity(extraIntent);
                }catch (Exception e){
                    e.printStackTrace();
                }
                alertDialog.dismiss();
                finish();
                break;
        }
    }
}
