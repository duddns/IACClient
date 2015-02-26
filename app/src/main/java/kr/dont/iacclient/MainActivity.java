package kr.dont.iacclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends Activity implements View.OnClickListener {

    private Messenger mMessenger;
    private boolean mIsBound;
    private ServiceConnection mServiceConnection;
    private Messenger mReplyTo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        View connectButton = findViewById(R.id.button_connect);
        connectButton.setOnClickListener(this);

        View disconnectButton = findViewById(R.id.button_disconnect);
        disconnectButton.setOnClickListener(this);

        View sendButton = findViewById(R.id.button_send);
        sendButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (R.id.button_connect == id) {
            connect();
        } else if (R.id.button_disconnect == id) {
            disconnect();
        } else if (R.id.button_send == id) {
            send();
        }
    }

    private void connect() {
        this.mServiceConnection = new RemoteServiceConnection();
        this.mReplyTo = new Messenger(new IncomingHandler());

        Intent intent = new Intent();
        intent.setClassName("kr.dont.iacservice", "kr.dont.iacservice.RemoteService");

        this.bindService(intent, this.mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void disconnect() {
        if (this.mIsBound) {
            this.unbindService(this.mServiceConnection);
            this.mIsBound = false;
        }
    }

    private void send() {
        if (this.mIsBound) {
            Message message = Message.obtain(null, 1, 0, 0);
            message.replyTo = MainActivity.this.mReplyTo;

            try {
                this.mMessenger.send(message);
            } catch (RemoteException e) {
                showToast("IACClient: Invocation Failed!!");
            }
        } else {
            showToast("IACClient: Service is Not Bound!!");
        }
    }


    private class RemoteServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName component, IBinder binder) {
            MainActivity.this.mMessenger = new Messenger(binder);
            MainActivity.this.mIsBound = true;

            showToast("IACClient: Service Connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName component) {
            MainActivity.this.mMessenger = null;
            MainActivity.this.mIsBound = false;

            showToast("IACClient: Service Disconnected!");
        }
    }

    private class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            int what = message.what;

            showToast("IACClient: Return successfully received - (" + what + ")");
        }
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(MainActivity.this.getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }
}
