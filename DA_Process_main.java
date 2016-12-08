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
		int startValue = Integer.parseInt(args[4]);

		ArrayList<String> addresses = new ArrayList<String>();

		int processID = 1;
		for (int i = 5; i < args.length; i++) {
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

			if(startValue<0) startValue = (Math.random()<0.5)?0:1;
			if(startValue>0) startValue = 1;
			localProcess.setIsFaulty(isFaulty);
			System.out.println("Process "+processNumber+": faulty = "+isFaulty);
			localProcess.setV(startValue);

			localProcess.createProcesses(addresses);

			localProcess.broadcast("notification", localProcess.getRound(), localProcess.getV() );

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
