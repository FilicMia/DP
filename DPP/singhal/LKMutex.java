import java.util.*;
public class LKMutex extends Process implements Lock{

	int My_Sequence_Number;
	boolean[] RV;
	int Highest_Sequence_Number_Seen;
	Queue<Pair<Integer, Integer>> LRQ; 
	Queue<Pair<Integer, Integer>> pending;

	public LKMutex(Linker initComm) {
		super(initComm);
		My_Sequence_Number=0;
		RV=new boolean[N];
		for (int j = 0; j < N; j++)
            		RV[j] = false;

		LRQ = new PriorityQueue<Pair<Integer, Integer> >(N, new Comparator<Pair<Integer, Integer>>() {
			@Override public int compare(    Pair<Integer, Integer> o1,    Pair<Integer, Integer> o2){
      				if (o1.getFirst() == o2.getFirst())     
					return o1.getSecond().compareTo(o2.getSecond());
				
				if (o1.getFirst() < o2.getFirst())       
					return -1;

     				return 1;
    				}
  			}

		);

		pending = new PriorityQueue<Pair<Integer, Integer> >(N, new Comparator<Pair<Integer, Integer>>() {
			@Override public int compare(    Pair<Integer, Integer> o1,    Pair<Integer, Integer> o2){
      				if (o1.getFirst() == o2.getFirst())       
					return o1.getSecond().compareTo(o2.getSecond());
      				if (o1.getFirst() < o2.getFirst())
				        return -1;
     				return 1;
    				}
  			}

		);

        	Highest_Sequence_Number_Seen=0;	
	}


	public synchronized void requestCS() {
       		My_Sequence_Number = Highest_Sequence_Number_Seen + 1;
		while (!LRQ.isEmpty()){ 
			LRQ.poll();
		}
		
		while (!pending.isEmpty()){ 
			pending.poll();
		}
							
		Pair<Integer, Integer> p = new Pair<Integer, Integer>( My_Sequence_Number, myId);
		LRQ.add(p);
		broadcastMsg("request", My_Sequence_Number);

		for (int j = 0; j < N; j++)
            		RV[j] = false;
		RV[myId]=true;

		while (!CheckExecuteCS())
            		myWait();
		
   	}

	public synchronized void releaseCS() {
		Pair<Integer, Integer> t=new Pair<Integer, Integer>(-1,-1);
		t = LRQ.peek();
		if (t.getSecond() == myId )
			LRQ.poll();									
		if( !LRQ.isEmpty()) {								
			t=LRQ.poll();
        		sendMsg(t.getSecond(), "flush", My_Sequence_Number);                
		}

		
		while (!pending.isEmpty()){
			t=pending.poll();
			sendMsg(t.getSecond(), "okay", My_Sequence_Number);
		}
    	}

	public synchronized void handleMsg(Msg m, int src, String tag) { 
        	int SN = m.getMessageInt(); 

        	if (tag.equals("request")) {
			if (Highest_Sequence_Number_Seen < SN)
				Highest_Sequence_Number_Seen = SN;
			
			if (RV[myId] == true) {
				if (RV[src] == false) {
					RV[src] = true;
					Pair<Integer, Integer> p = new Pair<Integer, Integer>( SN, src);
					LRQ.add(p);	
				}
				else if (RV[src] == true) { 
					Pair<Integer, Integer> p = new Pair<Integer, Integer>( SN, src);
					pending.add(p);
				}
				if (CheckExecuteCS()){								
					notify(); 
				}
			}
			else if (RV[myId] == false)
				sendMsg(src, "okay", My_Sequence_Number); 
		}
		else if (tag.equals("okay")) {
			RV[src]=true;

			Pair<Integer, Integer> t=new Pair<Integer, Integer>(-1,-1);
			while (!LRQ.isEmpty() && t.getFirst() <= SN && t.getSecond() != myId){
				t=LRQ.peek();
				if (t.getFirst() <= SN && t.getSecond() != myId) LRQ.poll();
			}
			
			
			if (CheckExecuteCS()){
				notify(); 
			}
		}
		else if (tag.equals("flush")){
			RV[src]=true;

			Pair<Integer, Integer> t=new Pair<Integer, Integer>(-1,-1);
			while (!LRQ.isEmpty() && t.getFirst() <= SN && t.getSecond() != myId){
				t=LRQ.peek();
				if (t.getFirst() <= SN && t.getSecond() != myId) LRQ.poll();
			}

			if (CheckExecuteCS()){
				notify(); 
			}
			
		}
    	}

	boolean CheckExecuteCS() {
		for (int j = 0; j < N; j++)
            		if (RV[j] == false) return false;

		if (LRQ.isEmpty()){ return false;}

		Pair<Integer, Integer> p = new Pair<Integer, Integer>(-1,-1);
		p=LRQ.peek();
		if (p.getSecond() != myId ) return false;

		return true; 

	}
}
