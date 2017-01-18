import java.util.concurrent.Semaphore;

/**
 * Model representing track given in the problem statement.
 *   \  S1  /
 *    \    /    Station one semaphore
 *     \  /
 *      v
 *      |
 *      |       Critical section one semaphore
 *      |
 *   --------
 *   |      |
 *   |      |   Overtake semaphore
 *   |      |
 *   --------
 *      |
 *      |       Critical section two semaphore
 *      |
 *     / \
 *    /   \     Station two semaphore
 *   /  S2 \
 */
public class Track {
    private Semaphore stationOneSemaphore = new Semaphore(1, true);
    private Semaphore stationTwoSemaphore = new Semaphore(1, true);
    private Semaphore criticalSectionOneSemaphore = new Semaphore(1, true);
    private Semaphore criticalSectionTwoSemaphore = new Semaphore(1, true);
    private Semaphore overtakeSemaphore = new Semaphore(1, true);


    public static final int[] STATION_ONE_SWITCH_POSITION =     {17,  7};
    public static final int[] OVERTAKE_ONE_SWITCH_POSITION =    {15,  9};
    public static final int[] OVERTAKE_TWO_SWITCH_POSITION =    { 4,  9};
    public static final int[] STATION_TWO_SWITCH_POSITION =     { 3, 11};


    public Track() {

    }

    Semaphore stationOneSemaphore() {
        return stationOneSemaphore;
    }


    public Semaphore stationTwoSemaphore() {
        return stationTwoSemaphore;
    }

    public Semaphore criticalSectionOneSemaphore() {
        return criticalSectionOneSemaphore;
    }

    public Semaphore criticalSectionTwoSemaphore() {
        return criticalSectionTwoSemaphore;
    }

    public Semaphore overtakeSemaphore() {
        return overtakeSemaphore;
    }
}
