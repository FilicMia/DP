import java.io.*;
import java.util.*;

public class SinghalMutex extends Process implements Lock {
	LamportClock c;
	int mojZig;
	boolean zahtjev, u_CS, mojPrioritet;
	LinkedList<Integer> skupR = new LinkedList<Integer>();
	LinkedList<Integer> skupI = new LinkedList<Integer>();

	public SinghalMutex(Linker initComm) {
		super(initComm);
		c = new LamportClock();
		mojZig = Symbols.Infinity; /* Žig mojeg zahtjeva. */
		zahtjev = false;
		u_CS = false;
		mojPrioritet = false;

		for (int i = 0; i < myId; ++i) /*Tako u opisu <=*/
			skupR.add(i);
	}

	public synchronized void requestCS() {
		zahtjev = true;
		c.tick();
		mojZig = c.getValue();

		for (int i = 0; i < skupR.size(); ++i) {
			int pid = skupR.get(i);
			sendMsg(pid, "request", mojZig);
		}
		while (!skupR.isEmpty())
			myWait();
		zahtjev = false;
		u_CS = true;
	}

	public synchronized void releaseCS() {
		u_CS = false;
		mojZig = Symbols.Infinity;
		while (!skupI.isEmpty()) {
			int pid = skupI.removeFirst();
			sendMsg(pid, "okay", c.getValue());
			System.out.println("Remove from R:");
			System.out.println(pid);
			skupR.add(pid);
		}
	}

	public synchronized void handleMsg(Msg m, int src, String tag) {
		int timestamp = m.getMessageInt();
		c.receiveAction(src, timestamp); /*Povećava vrijednost log. sata*/
		
		if (tag.equals("request")) {
			if (zahtjev) {
				if ((timestamp < mojZig) || 
					(mojZig == timestamp && src < myId)) {
					sendMsg(src, "okay", c.getValue());
					if (!skupR.contains( (Object) src)) {
						skupR.add(src);
						sendMsg(src, "request", c.getValue());
					}
					
				} else {/*Imam prioritet.*/
					if (!skupI.contains(src))
						skupI.add(src);
				} 
				
			} else if (u_CS) {
				if (!skupI.contains(src))
					skupI.add(src);
			} else if (!u_CS && !zahtjev){
				skupR.add(src);
				sendMsg(src, "okay", c.getValue());
			}
		} else if (tag.equals("okay")) {
			skupR.remove((Object) src);
			if (skupR.isEmpty())
				notify();
		}
	}
}
