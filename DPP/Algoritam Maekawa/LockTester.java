
public class LockTester {

	public static void main(String[] args) throws Exception {


		Linker comm = null;

		try {
			String basename = args[0];
			int myId = Integer.parseInt(args[1]);
			int numProc = Integer.parseInt(args[2]);
			comm = new Linker(basename, myId, numProc);
			Lock lock = null;

            if(args[3].equals("Maekawa")) /*Poseban folder zbog topology datoteka.*/
                lock = new MaekawaMutex(comm);
            else return;

			for(int i = 0; i < numProc; ++i)
				if(i != myId)
					(new ListenerThread(i, (MsgHandler)lock)).start();

			while(true) {

				System.out.println(myId + " is not in CS!");
				Util.mySleep(6000);
				lock.requestCS();
				System.out.println(myId + " is in CS! *****");
				Util.mySleep(3000);
				lock.releaseCS();
			}

		}
		catch(Exception e) {

			if(comm != null)
				comm.close();
			System.out.println(e);
			e.printStackTrace();
		}

	}
}
