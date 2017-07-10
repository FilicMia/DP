import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MaekawaMutex extends Process implements Lock {
	private static final String REQUEST = "request";
	private static final String REPLY = "reply";
	private static final String RELEASE = "release";
	private static final String FAILED = "failed";
	private static final String INQUIRE = "inquire";
	private static final String YIELD = "yield";

	LamportClock c = new LamportClock();
	Map<Integer, Integer> queue;
	Set<Integer> fORy;
	int numberOfReplies = 0;
	int time;
	int grant;

	public MaekawaMutex(Linker initComm) {
		super(initComm);
		queue = new HashMap<Integer, Integer>();
		fORy = new HashSet<Integer>();
		time = Symbols.Infinity;
		grant = Symbols.Infinity;
	}

	public synchronized void requestCS() {
		c.tick();
		queue.put(myId, c.getValue());
		sendToNeighbors(REQUEST, c.getValue());
		numberOfReplies = 0;

		if (grant == Symbols.Infinity) {
			grant = myId;
			numberOfReplies++;
		}

		while (numberOfReplies < (int) Math.sqrt(N) + 1) {
			myWait();
		}
	}

	public synchronized void releaseCS() {
		queue.put(myId, Symbols.Infinity);
		sendToNeighbors(RELEASE, c.getValue());

		int minTime = Integer.MAX_VALUE;
		int pr = Integer.MAX_VALUE;

		for (Map.Entry<Integer, Integer> process : queue.entrySet()) {
			if (process.getValue() != Symbols.Infinity) {
				if (minTime > process.getValue() || minTime == process.getValue() && pr > process.getKey()) {
					pr = process.getKey();
					minTime = process.getValue();
				}
			}
		}

		if (pr != Integer.MAX_VALUE) {
			sendMsg(pr, REPLY, c.getValue());
			grant = pr;
		} else {
			grant = Symbols.Infinity;
		}
	}

	public synchronized void handleMsg(Msg m, int src, String tag) {
		int timeStamp = m.getMessageInt();
		c.receiveAction(src, timeStamp);

		switch (tag) {
		case REQUEST:
			handleRequest(src, timeStamp);
			break;
		case RELEASE:
			handleRelease(src, timeStamp);
			break;
		case REPLY:
			handleReply(src, timeStamp);
			break;
		case FAILED:
			handleFailed(src, timeStamp);
			break;
		case INQUIRE:
			handleInquire(src, timeStamp);
			break;
		case YIELD:
			handleYield(src, timeStamp);
			break;
		}

	}

	private void handleRequest(int src, int timeStamp) {
		queue.put(src, timeStamp);
		
		if (grant == Symbols.Infinity) {
			sendMsg(src, REPLY, c.getValue());
			grant = src;
		} else if (queue.get(grant) < queue.get(src) || queue.get(grant) == queue.get(src) && grant < src)
			sendMsg(src, FAILED, c.getValue());
		else if (grant != myId)
			sendMsg(grant, INQUIRE, c.getValue());
		else {
			if (!fORy.isEmpty()) {
				numberOfReplies--;
				
				int minTime = Integer.MAX_VALUE;
				int pr = Integer.MAX_VALUE;
				
				for (Map.Entry<Integer, Integer> process : queue.entrySet()) {
					if (process.getValue() != Symbols.Infinity) {
						if (minTime > process.getValue() || minTime == process.getValue() && pr > process.getKey()) {
							pr = process.getKey();
							minTime = process.getValue();
						}
					}
				}
				grant = pr;
				if (pr != myId && pr != Integer.MAX_VALUE) {
					sendMsg(pr, REPLY, c.getValue());
				}
				else if (pr == myId) {
					numberOfReplies++;
				}
			} else
				time = c.getValue();
		}
	}

	private void handleRelease(int src, int timeStamp) {
		queue.put(src, Symbols.Infinity);
		int minTime = Integer.MAX_VALUE;
		int pr = Integer.MAX_VALUE;

		for (Map.Entry<Integer, Integer> process : queue.entrySet()) {
			if (process.getValue() != Symbols.Infinity) {
				if (minTime > process.getValue() || minTime == process.getValue() && pr > process.getKey()) {
					pr = process.getKey();
					minTime = process.getValue();
				}
			}
		}
		if (minTime != Symbols.Infinity) {
			grant = pr;
			if (pr != myId && pr != Integer.MAX_VALUE) {
				sendMsg(pr, "reply", c.getValue());
			} else if (pr == myId) {
				numberOfReplies++;
			}

			if (numberOfReplies == (int) Math.sqrt(N) + 1) {
				notify();
			}
		} else {
			grant = Symbols.Infinity;
		}
	}

	private void handleReply(int src, int timeStamp) {
		if (fORy.contains(src)) {
			fORy.remove(src);
		}

		numberOfReplies++;
		if (numberOfReplies == (int) Math.sqrt(N) + 1) {
			notify();
		}
	}

	private void handleFailed(int src, int timeStamp) {
		fORy.add(src);

		if (time + 1 == c.getValue()) {
			numberOfReplies--;
			int minTime = Integer.MAX_VALUE;
			int pr = Integer.MAX_VALUE;

			for (Map.Entry<Integer, Integer> process : queue.entrySet()) {
				if (process.getValue() != Symbols.Infinity) {
					if (minTime > process.getValue() || minTime == process.getValue() && pr > process.getKey()) {
						pr = process.getKey();
						minTime = process.getValue();
					}
				}
			}

			grant = pr;
			if (pr != myId && pr != Integer.MAX_VALUE) {
				sendMsg(pr, REPLY, c.getValue());
			}
		}
	}

	private void handleInquire(int src, int timeStamp) {
		if (!fORy.isEmpty()) {
			sendMsg(src, YIELD, c.getValue());
			fORy.add(src);
			numberOfReplies--;
		}
	}

	private void handleYield(int src, int timeStamp) {
		int minTime = Integer.MAX_VALUE;
		int pr = Integer.MAX_VALUE;

		for (Map.Entry<Integer, Integer> process : queue.entrySet()) {
			if (process.getValue() != Symbols.Infinity) {
				if (minTime > process.getValue() || minTime == process.getValue() && pr > process.getKey()) {
					pr = process.getKey();
					minTime = process.getValue();
				}
			}
		}
		grant = pr;

		if (pr != myId && pr != Integer.MAX_VALUE) {
			sendMsg(pr, REPLY, c.getValue());
			return;
		}

		if (pr == myId) {
			numberOfReplies++;

			if (numberOfReplies == (int) Math.sqrt(N) + 1) {
				notify();
			}
		}
	}
}
