package be.kuleuven;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.rmi.RemoteException;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */

    @Test
    public void testScaling() throws RemoteException {
        BulletinBoardImpl bulletinBoard = new BulletinBoardImpl();
        for (int i = 0; i <= (bulletinBoard.getAmountOfMailboxes() * 0.8) + 1; i++) {
            bulletinBoard.postMessage(i, "test".getBytes(), "test".getBytes());
        }
        assertTrue(bulletinBoard.getAmountOfServers() == 2);

        for (int i = 0; i <= (bulletinBoard.getAmountOfMailboxes() * 1.6) + 2; i++) {
            bulletinBoard.postMessage(i, "test".getBytes(), "test".getBytes());
        }

        assertTrue(bulletinBoard.getAmountOfServers() == 3);
    }
}
