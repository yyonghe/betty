package io.betty.lifecycle;

public class LifecycleEventExt extends LifecycleEvent {

	private static final long serialVersionUID = 1L;
	
	private long threadId = Thread.currentThread().getId();
	
	private String threadName = Thread.currentThread().getName();
	
	public LifecycleEventExt() {
		this(DEFAULT_EVENT_SOURCE);
	}

	public LifecycleEventExt(Lifecycle lifecycle) {
		this(lifecycle, LifecycleState.DEFAULT_SOURCE_EVENT);
	}
	
	public LifecycleEventExt(Lifecycle lifecycle, LifecycleState state) {
		this(lifecycle, state, null);
	}
	
	public LifecycleEventExt(Lifecycle lifecycle, LifecycleState state, Object data) {
		super(lifecycle, state, data);
	}
	
	public void setValue(Lifecycle lifecycle, LifecycleState state, Object data, long threadId, String threadName) {
		this.source = lifecycle;
		this.state = state;
		this.data = data;
		this.threadId = threadId;
		this.threadName = threadName;
	}
	
	public void clear() {
		setValue(DEFAULT_EVENT_SOURCE, LifecycleState.DEFAULT_SOURCE_EVENT, 
				null, 0, "");
	}
	
	/**
	 * @return the threadId
	 */
	public long getThreadId() {
		return threadId;
	}

	/**
	 * @return the threadName
	 */
	public String getThreadName() {
		return threadName;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[threadId=");
		builder.append(threadId);
		builder.append(", threadName=");
		builder.append(threadName);
		builder.append(", source=");
		builder.append(source);
		builder.append(", state=");
		builder.append(state);
		builder.append(", data=");
		builder.append(data);
		builder.append("]");
		return builder.toString();
	}



	public static final LifecycleBase DEFAULT_EVENT_SOURCE = new LifecycleBase() {
		
		@Override
		public String getName() {
			return Lifecycle.DEFAULT_SOURCE_EVENT;
		}
		@Override
		protected void stopInternal() throws LifecycleException {
		}
		
		@Override
		protected void startInternal() throws LifecycleException {
		}
		
		@Override
		protected void initInternal() throws LifecycleException {
		}
		
		@Override
		protected void destroyInternal() throws LifecycleException {
		}
	};

}
