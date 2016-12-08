import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.rmi.registry.Registry;

public class DA_Process_main {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		String ownIp = args[0];
		int registryPort = Integer.parseInt(args[1]);
		int processNumber = Integer.parseInt(args[2]);
		boolean isFaulty = args[3].equals("true");

		ArrayList<String> addresses = new ArrayList<String>();

		int processID = 1;
		for (int i = 4; i < args.length; i++) {
			if(processID==processNumber) processID++;
			addresses.add("rmi://"+args[i]+"/proc"+processID);
			processID++;
		}

		try{
			//
			//server
			//

			System.setProperty("java.rmi.server.hostname",ownIp);
			Registry reg;
			DA_Process localProcess=new DA_Process(processNumber);
			try {
				reg =java.rmi.registry.LocateRegistry.createRegistry(registryPort);
				String name = "rmi://"+ownIp+"/"+"proc"+processNumber;
				Naming.bind(name,localProcess);
				localProcess.setRegistry(reg);
				localProcess.setName(name);
			} catch (RemoteException e){
				e.printStackTrace();
			} catch (Exception e){

			}

			localProcess.isFaulty = isFaulty;

			localProcess.createProcesses(addresses);

			localProcess.broadcast("notification", localProcess.getRound(), localProcess.getV() );

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
