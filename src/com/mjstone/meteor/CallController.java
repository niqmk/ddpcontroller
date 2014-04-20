package com.mjstone.meteor;

public class CallController {
	private Controller controller;
	public static interface CallListener {
		public void onCallConnected();
		public void onCallReturned(final String result);
		public void onCallFailed();
	}
	public CallController(final CallListener call_listener) {
		controller = new Controller();
		controller.init(call_listener);
	}
	public void call(final String name, final Object[] params) {
		controller.call(name, params);
	}
}