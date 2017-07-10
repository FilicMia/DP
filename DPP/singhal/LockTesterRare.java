public class LockTesterRare {
	/* Rijetko potražuje CS.*/
    public static void main(String[] args) throws Exception {
        Linker comm = null;
        try {
            String baseName = args[0];
            int myId = Integer.parseInt(args[1]);
            int numProc = Integer.parseInt(args[2]);
            comm = new Linker(baseName, myId, numProc);
            Lock lock = null;
            if (args[3].equals("Lamport"))
                lock = new LamportMutex(comm);
            if (args[3].equals("RicartAgrawala"))
                lock = new RAMutex(comm);
            if (args[3].equals("DiningPhil"))
                lock = new DinMutex(comm);
	    if (args[3].equals("Singhal"))
                lock = new SinghalMutex(comm);
            if (args[3].equals("CircToken"))
                lock = new CircToken(comm,0);
            for (int i = 0; i < numProc; i++)
               if (i != myId)
                  (new ListenerThread(i, (MsgHandler)lock)).start();
            while (true) {
                System.out.println(myId + " is not in CS");
                Util.mySleep(50000);
                lock.requestCS();
                System.out.println(myId + " is in CS *****");
 		Util.mySleep(3000);
                lock.releaseCS();
            }
        }
        catch (InterruptedException e) {
            if (comm != null) comm.close();
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
