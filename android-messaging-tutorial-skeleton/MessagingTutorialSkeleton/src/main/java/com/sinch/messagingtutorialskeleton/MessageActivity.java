package com.sinch.messagingtutorialskeleton;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.messagingtutorialskeleton.R;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Alicia on 12/4/2015.
 */
public class MessageActivity extends Activity {
    private String recipientId;
    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;
    private String currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    MyMessageClientListener messageClientListener = new MyMessageClientListener();
    ListView messagesList;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        //get recipientId from the intent
        Intent intent = getIntent();
        recipientId = intent.getStringExtra("RECIPIENT_ID");
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);

        String[] userIds = {currentUserId, recipientId};
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereContainedIn("senderId", Arrays.asList(userIds));
        query.whereContainedIn("recipientId", Arrays.asList(userIds));
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        WritableMessage message = new WritableMessage(messageList.get(i).get("recipientId").toString(), messageList.get(i).get("messageText").toString());
                        if (messageList.get(i).get("senderId").toString().equals(currentUserId)) {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
                        } else {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
                        }
                    }
                }
            }
        });

        //listen for a click on the send button
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onClick(View view) {
                //send the message!
                messageBody = messageBodyField.getText().toString();
                if (messageBody.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_LONG).show();
                    return;
                }

                messageService.sendMessage(recipientId, messageBody);
                messageBodyField.setText("");
            }
        });
    }

    //unbind the service when the activity is destroyed
    @Override
    public void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
            messageService.removeMessageClientListener(messageClientListener);
        }
    }

    private class MyMessageClientListener implements MessageClientListener {

        //Notify the user if their message failed to send
        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
            Toast.makeText(MessageActivity.this, "Message failed to send.", Toast.LENGTH_LONG).show();
        }


        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            //Display an incoming message
            if (message.getSenderId().equals(recipientId)) {
                WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING);
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {
            //Display the message that was just sent
            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
            messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);

            //Later, I'll show you how to store the
            //message in Parse, so you can retrieve and
            //display them every time the conversation is opened
            //final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            //only add message to parse database if it doesn't already exist there
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            query.whereEqualTo("sinchId", message.getMessageId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                    if (e == null) {
                        if (messageList.size() == 0) {
                            ParseObject parseMessage = new ParseObject("ParseMessage");
                            parseMessage.put("senderId", currentUserId);
                            parseMessage.put("recipientId", writableMessage.getRecipientIds().get(0));
                            parseMessage.put("messageText", writableMessage.getTextBody());
                            parseMessage.put("sinchId", writableMessage.getMessageId());
                            parseMessage.saveInBackground();

                            messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);
                        }
                    }
                }
            });
        }

        //Do you want to notify your user when the message is delivered?
        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {}

        @Override
        public void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> list) {

        }

    }
}