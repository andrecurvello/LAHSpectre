package lah.spectre.interfaces;

/**
 * Interface for listener awaiting for a server for service.
 * 
 * Here, server should be understood in a general sense: an object that receives
 * requests, handles them and makes responses. Example of server object is a
 * progressive result of a time-consuming computation that allows a client
 * object waiting for it to poll it for progress update.
 * 
 * @author L.A.H.
 * 
 * @param <S>
 *            Type for server
 */
public interface IClient<S> {

	/**
	 * Invoke by the server factory object when a server is ready to render
	 * service (handle requests) to notify the waiting client
	 * 
	 * @param server
	 */
	void onServerReady(S server);

}
