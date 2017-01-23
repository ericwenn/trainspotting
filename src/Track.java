import sun.management.Sensor;

import java.util.concurrent.Semaphore;

/**
 * Model representing track given in the problem statement.
 *          / S1     North station semaphore
 *     ----/-----    North station cross semaphore
 *     \  /
 *      v
 *      |
 *      |       East critical section semaphore
 *      |
 *   --------
 *   |      |
 *   |      |   Overtake semaphore
 *   |      |
 *   --------
 *      |
 *      |       West critical section semaphore
 *      |
 *      ^
 *     / \
 *    /   \     South station semaphore
 *   /  S2 \
 */
public class Track {

    protected Semaphore northStationSemaphore = new Semaphore(1, true);
    protected Semaphore southStationSemaphore = new Semaphore(1, true);
    protected Semaphore eastCriticalSectionSemaphore = new Semaphore(1, true);
    protected Semaphore westCriticalSectionSemaphore = new Semaphore(1, true);
    protected Semaphore overtakeSemaphore = new Semaphore(1, true);
    protected Semaphore northStationCrossSemaphore = new Semaphore(1, true);



    protected static class Sensor {
        protected final int x;
        protected final int y;

        Sensor(int x, int y ) {

            this.x = x;
            this.y = y;
        }
    }

    protected enum SensorDirection {
        NORTH(0),
        EAST(1),
        SOUTH(2),
        WEST(3);

        private final int v;
        SensorDirection(int v) {
            this.v = v;
        }
        public int v() {
            return this.v;
        }
    }

    protected Sensor[][] northStationCrossSensors = new Sensor[][] {
            {
                new Sensor(8,6),
                new Sensor(8,5),
                new Sensor(9,5),
                new Sensor(11,5)
            },
            {
                new Sensor(9,7),
                new Sensor(10,7),
                new Sensor(11,7),
                new Sensor(13,7),
            },
            {
                new Sensor(8,8),
                new Sensor(9,8),
                new Sensor(10,8),
                new Sensor(12,8),
            },
            {
                new Sensor(7,7),
                new Sensor(6,7),
                new Sensor(6,6),
                new Sensor(6,4),
            }
    };

    protected Sensor[][] northStationSwitchSensors = new Sensor[][] {
            {
            },
            {
                new Sensor(18,7),
                new Sensor(19,9)
            },
            {
                new Sensor(17,8),
                new Sensor(16,8),
                new Sensor(15,8),
                new Sensor(13,8),
            },
            {
                new Sensor(16,7),
                new Sensor(15,7),
                new Sensor(14,7),
                new Sensor(12,7),
            }
    };

    protected Sensor[][] overtakeEastSwitchSensors = new Sensor[][] {
            {
            },
            {
                    new Sensor(16,9),
                    new Sensor(19,9),
            },
            {
                    new Sensor(15,10),
                    new Sensor(14,10),
                    new Sensor(13,10),
                    new Sensor(11,10),
            },
            {
                    new Sensor(14,9),
                    new Sensor(13,9),
                    new Sensor(12,9),
                    new Sensor(10,9),
            }
    };


    protected Sensor[][] overtakeWestSwitchSensors = new Sensor[][] {
            {
            },
            {
                    new Sensor(5,9),
                    new Sensor(6,9),
                    new Sensor(7,9),
                    new Sensor(9,9),
            },
            {
                    new Sensor(4,10),
                    new Sensor(5,10),
                    new Sensor(6,10),
                    new Sensor(8,10),
            },
            {
                    new Sensor(3,9),
                    new Sensor(1,9),
            }
    };


    protected Sensor[][] southStationSwitchSensors = new Sensor[][] {
            {
            },
            {
                    new Sensor(4,11),
                    new Sensor(5,11),
                    new Sensor(6,11),
                    new Sensor(8,11),
            },
            {
                    new Sensor(3,12),
                    new Sensor(3,13),
                    new Sensor(4,13),
                    new Sensor(6,13),
            },
            {
                    new Sensor(2,11),
                    new Sensor(1,10)
            }
    };


    protected Sensor[][] northStationPreferredStopSensors = new Sensor[][] {
            {}, {}, {},
            // West
            {
                    new Sensor(17,3),
                    new Sensor(16,3),
                    new Sensor(15,3),
                    new Sensor(13,3)
            }
    };

    protected Sensor[][] northStationStopSensors = new Sensor[][] {
            {}, {}, {},
            // West
            {
                    new Sensor(17,5),
                    new Sensor(16,5),
                    new Sensor(15,5),
                    new Sensor(13,5)
            }
    };

    protected Sensor[][] southStationPreferredStopSensors = new Sensor[][] {
            {}, {}, {},
            // West
            {
                    new Sensor(16,11),
                    new Sensor(17,11),
                    new Sensor(15,11),
                    new Sensor(13,11)
            }
    };

    protected Sensor[][] southStationStopSensors = new Sensor[][] {
            {}, {}, {},
            // West
            {
                    new Sensor(17,13),
                    new Sensor(16,13),
                    new Sensor(15,13),
                    new Sensor(13,13)
            }
    };






    public static final int[] NORTH_STATION_SWITCH_POSITION =     {17,  7};
    public static final int[] OVERTAKE_EAST_SWITCH_POSITION =    {15,  9};
    public static final int[] OVERTAKE_WEST_SWITCH_POSITION =    {4,  9};
    public static final int[] SOUTH_STATION_SWITCH_POSITION =     {3, 11};


    public Track() {

    }


}
