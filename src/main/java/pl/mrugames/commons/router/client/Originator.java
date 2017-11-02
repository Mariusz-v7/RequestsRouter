package pl.mrugames.commons.router.client;

/**
 * Use it together with session to recognize respective clients.
 */
public class Originator {
    private final long clientId;

    public Originator(long clientId) {
        this.clientId = clientId;
    }

    public long getClientId() {
        return clientId;
    }
}
