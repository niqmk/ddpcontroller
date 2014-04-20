package com.mjstone.meteor.general;

public class DDPMessage {
	public class DdpMessageField {
		public static final String MSG = "msg";
		public static final String ID = "id";
		public static final String METHOD = "method";
		public static final String METHODS = "methods";
		public static final String SUBS = "subs";
		public static final String PARAMS = "params";
		public static final String RESULT = "result";
		public static final String NAME = "name";
		public static final String SERVER_ID = "server_id";
		public static final String ERROR = "error";
		public static final String SESSION = "session";
		public static final String VERSION = "version";
		public static final String SUPPORT = "support";
		public static final String SOURCE = "source";
		public static final String ERRORMSG = "errormsg";
		public static final String CODE = "code";
		public static final String REASON = "reason";
		public static final String REMOTE = "remote";
		public static final String COLLECTION = "collection";
		public static final String FIELDS = "fields";
		public static final String CLEARED = "cleared";
	}
	public class DdpMessageType {
		// client -> server
		public static final String CONNECT = "connect";
		public static final String METHOD = "method";
		// server -> client
		public static final String CONNECTED = "connected";
		public static final String UPDATED = "updated";
		public static final String READY = "ready";
		public static final String NOSUB = "nosub";
		public static final String RESULT = "result";
		public static final String SUB = "sub";
		public static final String UNSUB = "unsub";
		public static final String ERROR = "error";
		public static final String CLOSED = "closed";
		public static final String ADDED = "added";
		public static final String REMOVED = "removed";
		public static final String CHANGED = "changed";
	}
	public final static String DDP_PROTOCOL_VERSION = "pre1";
}