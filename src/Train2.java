import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

import java.util.concurrent.RunnableFuture;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;


/**
 * Created by ericwenn on 1/18/17.
 */
public class Train2 implements Runnable {
    private final int trainId;
    private final int initialSpeed;
    private int parallell_track;
    private Track track;

    // direction = 1 if the train is going forward -1 otherwise
    private int direction;

    public Train2(int trainId, int initialSpeed, Track track) {

        this.trainId = trainId;
        this.initialSpeed = initialSpeed;
        this.track = track;
        this.direction = 1;

        this.parallell_track = trainId == 1 ? 1 : 0;

    }


    @Override
    public void run() {
        TSimInterface tsim = TSimInterface.getInstance();
        boolean firstRun = true;

        try {

            int[] switch_position;
            int switch_direction;
            Semaphore stationSemaphore;
            stationSemaphore = isGoingToStationTwo() ? track.stationOneSemaphore() : track.stationTwoSemaphore();
            stationSemaphore.acquire();




            while(true) {

                // Start driving
                tsim.setSpeed(trainId, fullSpeed());

                // Block until train is ahead of a switch
                tsim.getSensor(trainId);
                tsim.getSensor(trainId);
                tsim.getSensor(trainId);

                // Stop driving
                tsim.setSpeed(trainId, 0);

                System.out.println("Train " + trainId + " is at first switch");


                // Get correct Semaphore
                Semaphore criticalSectionSem = isGoingToStationTwo() ? track.criticalSectionOneSemaphore() : track.criticalSectionTwoSemaphore();

                // Block until train can pass switch
                criticalSectionSem.acquire();


                // Set correct switch
                switch_position = isGoingToStationTwo() ? Track.STATION_ONE_SWITCH_POSITION : Track.STATION_TWO_SWITCH_POSITION;
                switch_direction = parallell_track == 1 ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;


                tsim.setSwitch(switch_position[0], switch_position[1], switch_direction);
                System.out.println("Train " + trainId + " setting first switch to " + switch_direction);

                // Resume driving
                tsim.setSpeed(trainId, fullSpeed());
                System.out.println("Train " + trainId + " is passing first switch");

                // Block until switch is passed
                tsim.getSensor(trainId);
                tsim.getSensor(trainId);

                System.out.println("Train " + trainId + " passed first switch, releasing track..,");
                stationSemaphore.release();


                // Block until train is in front of 2nd switch
                tsim.getSensor(trainId);
                tsim.getSensor(trainId);
                System.out.println("Train " + trainId + " is in front of 2nd switch");


                Semaphore overtakeSem = track.overtakeSemaphore();
                switch_position = isGoingToStationTwo() ? Track.OVERTAKE_ONE_SWITCH_POSITION : Track.OVERTAKE_TWO_SWITCH_POSITION;


                if (overtakeSem.availablePermits() == 0) { // A train is already on the overtake

                    // Go to the other side of the track
                    switch_direction = isGoingToStationTwo() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                    parallell_track = 1;
                    System.out.println("Train " + trainId + " is taking slower route over overtake...");
                } else {
                    // Acquire semaphore
                    overtakeSem.acquire();
                    switch_direction = isGoingToStationTwo() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                    parallell_track = 0;
                    System.out.println("Train " + trainId + " is taking quick route over overtake...");

                }


                tsim.setSwitch(switch_position[0], switch_position[1], switch_direction);


                // Block until on overtake
                tsim.getSensor(trainId);
                tsim.getSensor(trainId);

                // Release critical section
                System.out.println("Train " + trainId + " is on overtake, releasing critical section semaphore...");
                criticalSectionSem.release();


                tsim.setSpeed(trainId, 0);
                System.out.println("Train " + trainId + " in front to switch to leave overtake");


                criticalSectionSem = isGoingToStationTwo() ? track.criticalSectionTwoSemaphore() : track.criticalSectionOneSemaphore();

                System.out.println("Train " + trainId + " waiting for critical section to be free...");
                // Block until critical section is acqquired
                criticalSectionSem.acquire();
                System.out.println("Train " + trainId + " acquired critical section");


                switch_position = isGoingToStationTwo() ? Track.OVERTAKE_TWO_SWITCH_POSITION : Track.OVERTAKE_ONE_SWITCH_POSITION;
                switch_direction = isGoingToStationTwo() && parallell_track == 0 || isGoingToStationOne() && parallell_track == 1 ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;

                tsim.setSwitch(switch_position[0], switch_position[1], switch_direction);

                tsim.setSpeed(trainId, fullSpeed());

                // Block until on critical section
                tsim.getSensor(trainId);
                tsim.getSensor(trainId);
                System.out.println("Train " + trainId + " on passed switch, releasing overtake semaphore");
                overtakeSem.release();


                tsim.getSensor(trainId);
                tsim.getSensor(trainId);


                switch_position = isGoingToStationOne() ? Track.STATION_TWO_SWITCH_POSITION : Track.STATION_ONE_SWITCH_POSITION;
                stationSemaphore = isGoingToStationTwo() ? track.stationTwoSemaphore() : track.stationOneSemaphore();
                switch_position = isGoingToStationTwo() ? Track.OVERTAKE_ONE_SWITCH_POSITION : Track.OVERTAKE_TWO_SWITCH_POSITION;


                if (stationSemaphore.availablePermits() == 0) { // A train is already on the station tracks
                    // Go to the other side of the track
                    switch_direction = isGoingToStationTwo() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                    parallell_track = 0;
                    System.out.println("Train " + trainId + " is going to second station stop");
                } else {
                    // Acquire semaphore
                    stationSemaphore.acquire();
                    switch_direction = isGoingToStationTwo() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                    parallell_track = 1;
                    System.out.println("Train " + trainId + " is going to first station stop");

                }


                tsim.setSwitch(switch_position[0], switch_position[1], switch_direction);

                // Block until switch is passed
                tsim.getSensor(trainId);
                tsim.getSensor(trainId);
                System.out.println("Train " + trainId + " passed switch, releasing critical section semaphore...");
                criticalSectionSem.release();


                // block until train is ready to stop
                tsim.getSensor(trainId);
                tsim.getSensor(trainId);
                tsim.getSensor(trainId);

                System.out.println("Train " + trainId + " breaking..");
                tsim.setSpeed(trainId, 0);


                sleep(2000);
                System.out.println("Train " + trainId + " halted.");
                System.out.println("Attempting to reverse");
                direction = -direction;
            }








        } catch (CommandException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Train ID: "+trainId);
    }

    private boolean isGoingToStationTwo() {
        return trainId == 1 && direction == 1 || trainId == 2 && direction == -1;
    }

    private boolean isGoingToStationOne() {
        return !isGoingToStationTwo();
    }

    private int fullSpeed() {
        return direction*initialSpeed;
    }


}
