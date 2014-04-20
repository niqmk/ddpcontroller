package com.mjstone.meteor;

public class SubsController {
	private Controller controller;
	private String subs_id = "";
	public interface SubsListener {
		public void onSubConnected(final String unique_id, final String saved_subs_id);
		public void onSubAdded(final String id, final String result);
		public void onSubChanged(final String id, final String result);
		public void onSubRemoved(final String id);
		public void onSubFailed();
		public void onSubEOF();
	}
	public SubsController getSubController() {
		return this;
	}
	public SubsController() {}
	public SubsController(final String subs_id) {
		this.subs_id = subs_id;
	}
	public void setListener(final SubsListener subs_listener, final boolean auto_reconnect) {
		controller = new Controller(subs_id);
		controller.init(subs_listener, auto_reconnect);
	}
	public void setAutoReconnect(final boolean auto_reconnect) {
		controller.setAutoReconnect(auto_reconnect);
	}
	public void connect() {
		controller.connect();
	}
	public int getCounter() {
		return controller.getCounter();
	}
	public boolean isConnected() {
		return controller.isConnected();
	}
	public void subs(final String name, final Object[] params) {
		controller.subs(name, params);
	}
	public void subs(final String name) {
		controller.subs(name);
	}
}