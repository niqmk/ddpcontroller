package com.mjstone.meteor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.mjstone.meteor.CallController.CallListener;
import com.mjstone.meteor.SubsController.SubsListener;
import com.mjstone.meteor.general.Config;
import com.mjstone.meteor.general.DDPMessage;
import com.mjstone.meteor.general.DDPMessage.DdpMessageField;
import com.mjstone.meteor.general.DDPMessage.DdpMessageType;
import com.mjstone.meteor.general.WebSocketClient;

public class Controller {
	private static int unique_id = 0;
	private final Gson gson = new Gson();
	private CallListener call_listener;
	private SubsListener subs_listener;
	private WebSocketClient client;
	private int counter = 0;
	private boolean connected = false;
	private boolean auto_reconnect = false;
	private String saved_json = "";
	private String saved_subs_id = "";
	private boolean saved = false;
	public Controller() {}
	public Controller(final String subs_id) {
		this.saved_subs_id = subs_id;
	}
	public void init(CallListener call_listener) {
		this.call_listener = call_listener;
		auto_reconnect = false;
		saved = false;
		connect();
	}
	public void init(SubsListener subs_listener, final boolean auto_reconnect) {
		this.subs_listener = subs_listener;
		this.auto_reconnect = auto_reconnect;
		saved = false;
		connect();
	}
	public void setAutoReconnect(final boolean auto_reconnect) {
		this.auto_reconnect = auto_reconnect;
	}
	public WebSocketClient SocketClient() {
		return client;
	}
	public boolean isConnected() {
		return connected;
	}
	public int getCounter() {
		return counter;
	}
	public void subs(final String name) {
		Map<String, Object> message = new HashMap<String, Object>();
		message.put(DdpMessageField.MSG, DdpMessageType.SUB);
		message.put(DdpMessageField.NAME, name);
		message.put(DdpMessageField.PARAMS, null);
		message.put(DdpMessageField.ID, String.valueOf(next_id()));
		saved_json = gson.toJson(message);
		if(!isConnected()) {
			saved = true;
			return;
		}
		saved = false;
		client.send(saved_json);
	}
	public void subs(final String name, final Object params[]) {
		Map<String, Object> message = new HashMap<String, Object>();
		message.put(DdpMessageField.MSG, DdpMessageType.SUB);
		message.put(DdpMessageField.NAME, name);
		message.put(DdpMessageField.PARAMS, params);
		message.put(DdpMessageField.ID, String.valueOf(next_id()));
		saved_json = gson.toJson(message);
		if(!isConnected()) {
			saved = true;
			return;
		}
		saved = true;
		client.send(saved_json);
	}
	public void call(final String name, final Object[] params) {
		Map<String, Object> message = new HashMap<String, Object>();
		message.put(DdpMessageField.MSG, DdpMessageType.METHOD);
		message.put(DdpMessageField.METHOD, name);
		message.put(DdpMessageField.PARAMS, params);
		message.put(DdpMessageField.ID, String.valueOf(next_id()));
		saved_json = gson.toJson(message);
		if(!isConnected()) {
			saved = true;
			return;
		}
		saved = false;
		client.send(saved_json);
	}
	public void connect() {
		List<BasicNameValuePair> extra_headers = Arrays.asList(new BasicNameValuePair("Cookie", "session=dealoka"));
		try {
			client = new WebSocketClient(new URI(Config.server), listener, extra_headers);
			client.connect();
		}catch(URISyntaxException ex) {
			ex.printStackTrace();
		}
	}
	public void disconnect() {
		client.disconnect();
		connected = false;
	}
	private int next_id() {
		Controller.unique_id++;
		return Controller.unique_id;
	}
	private WebSocketClient.Listener listener = new WebSocketClient.Listener() {
		@Override
		public void onConnect() {
			counter = 0;
			connected = true;
			Map<String, Object> connect_message = new HashMap<String, Object>();
			connect_message.put(DdpMessageField.MSG, DdpMessageType.CONNECT);
			connect_message.put(DdpMessageField.VERSION, DDPMessage.DDP_PROTOCOL_VERSION);
			connect_message.put(DdpMessageField.SUPPORT, new String[] { DDPMessage.DDP_PROTOCOL_VERSION });
	        if(!Config.session.equals("")) {
	        	connect_message.put(DdpMessageField.SESSION, Config.session);
	        }
	        String json = gson.toJson(connect_message);
	        client.send(json);
	        if(saved) {
	        	saved = false;
	        	client.send(saved_json);
	        }
		}
		@Override
		public void onMessage(String message) {
			parseMessage(message);
		}
		@Override
		public void onMessage(byte[] data) {}
		@Override
		public void onDisconnect(int code, String reason) {
			connected = false;
			if(code == 1) {
				if(subs_listener != null) {
					subs_listener.onSubEOF();
				}
			}
			if(auto_reconnect) {
				new setAutoConnectTimer().execute();
			}else {
				counter = 0;
			}
		}
		@Override
		public void onSocketError() {
			connected = false;
			if(call_listener != null) {
				call_listener.onCallFailed();
			}
			if(subs_listener != null) {
				subs_listener.onSubFailed();
			}
			new setAutoConnectTimer().execute();
		}
		@Override
		public void onUnknownHost() {
			connected = false;
			if(call_listener != null) {
				call_listener.onCallFailed();
			}
			if(subs_listener != null) {
				subs_listener.onSubFailed();
			}
			new setAutoConnectTimer().execute();
		}
		@Override
		public void onError(Exception error) {
			if(call_listener != null) {
				call_listener.onCallFailed();
			}
			if(subs_listener != null) {
				subs_listener.onSubFailed();
			}
		}
	};
	private class setAutoConnectTimer extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPostExecute(Void result) {
			if(counter < Config.connection_timer) {
				if(auto_reconnect) {
					connect();
				}else {
					new setAutoConnectTimer().execute();
				}
			}else {
				counter = 0;
			}
		}
		@Override
		protected void onPreExecute() {
		}
		@Override
		protected Void doInBackground(Void... params) {
			try {
				counter++;
				Thread.sleep(Config.idle_timer);
			}catch(InterruptedException ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}
	private void parseMessage(final String message) {
		try {
			JSONObject json = new JSONObject(message);
			if(json.get(DdpMessageField.MSG) == JSONObject.NULL) {
				return;
			}
			if(json.getString(DdpMessageField.MSG).equals(DdpMessageType.CONNECTED)) {
				if(json.get(DdpMessageField.SESSION) != JSONObject.NULL) {
					Config.session = json.getString(DdpMessageField.SESSION);
					if(call_listener != null) {
						call_listener.onCallConnected();
					}
					if(subs_listener != null) {
						subs_listener.onSubConnected(String.valueOf(unique_id), saved_subs_id);
					}
				}
			}else if(json.getString(DdpMessageField.MSG).equals(DdpMessageType.ERROR)) {
			}else if(json.getString(DdpMessageField.MSG).equals(DdpMessageType.ADDED)) {
				parseAddSubscribe(message);
			}else if(json.getString(DdpMessageField.MSG).equals(DdpMessageType.CHANGED)) {
				parseChangeSubscribe(message);
			}else if(json.getString(DdpMessageField.MSG).equals(DdpMessageType.REMOVED)) {
				parseRemoveSubscribe(message);
			}else if(json.getString(DdpMessageField.MSG).equals(DdpMessageType.RESULT)) {
				if(call_listener != null) {
					if(json.isNull(DdpMessageField.ERROR)) {
						call_listener.onCallReturned(json.getString(DdpMessageField.RESULT));
					}else {
						call_listener.onCallFailed();
					}
				}
			}
		}catch(JSONException ex) {
			ex.printStackTrace();
		}
	}
	private void parseAddSubscribe(final String message) {
		try {
			JSONObject json = new JSONObject(message);
			if(subs_listener != null) {
				subs_listener.onSubAdded(json.getString(DdpMessageField.ID), json.getString(DdpMessageField.FIELDS));
			}
		}catch(JSONException ex) {
			ex.printStackTrace();
		}
	}
	private void parseChangeSubscribe(final String message) {
		try {
			JSONObject json = new JSONObject(message);
			if(subs_listener != null) {
				subs_listener.onSubChanged(json.getString(DdpMessageField.ID), json.getString(DdpMessageField.FIELDS));
			}
		}catch(JSONException ex) {
			ex.printStackTrace();
		}
	}
	private void parseRemoveSubscribe(final String message) {
		try {
			JSONObject json = new JSONObject(message);
			if(subs_listener != null) {
				subs_listener.onSubRemoved(json.getString(DdpMessageField.ID));
			}
		}catch(JSONException ex) {
			ex.printStackTrace();
		}
	}
}