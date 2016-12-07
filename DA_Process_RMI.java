import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface DA_Process_RMI extends Remote{

		public void receiveNotification(Message notification) throws RemoteException;
		public void receiveProposal(Message proposal) throws RemoteException;
  public boolean isReady() throws RemoteException;
}
