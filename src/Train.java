import TSim.CommandException;
import TSim.TSimInterface;

import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class Train implements Runnable {


    private final int trainId;
    private final int initialSpeed;
    private final Track track;
    private TSimInterface tSimInterface;
    private int direction;
    private boolean isOnParallelTrack = false;

    public Train(int trainId, int initialSpeed, Track track) {
        this.trainId = trainId;
        this.initialSpeed = initialSpeed;
        this.track = track;
        this.tSimInterface = TSimInterface.getInstance();
        this.direction = 1;
    }


    @Override
    public void run() {
        try {
            Semaphore criticalStationSem = isGoingToStationTwo() ? track.stationOneSemaphore() : track.stationTwoSemaphore();
            criticalStationSem.acquire();
            while(true) {
                tSimInterface.setSpeed(trainId, fullSpeed());
                tSimInterface.getSensor(trainId); // 1 activate
                tSimInterface.getSensor(trainId); // 1 deactivate

                tSimInterface.getSensor(trainId); // 2 activate
                tSimInterface.getSensor(trainId); // 2 deactivate

                Semaphore criticalSectionSem = isGoingToStationTwo() ? track.criticalSectionOneSemaphore() : track.criticalSectionTwoSemaphore();

                tSimInterface.setSpeed(trainId, 0);
                criticalSectionSem.acquire();

                int[] switchPos;
                int switchDir;

                if (isGoingToStationTwo()) {
                    switchPos = Track.STATION_ONE_SWITCH_POSITION;
                    switchDir = isOnParallelTrack ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                }
                else {
                    switchPos = Track.STATION_TWO_SWITCH_POSITION;
                    switchDir = isOnParallelTrack ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                }

                tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
                tSimInterface.setSpeed(trainId, fullSpeed());


                tSimInterface.getSensor(trainId); // 3 activate
                if( !isOnParallelTrack) {
                    criticalStationSem.release();
                }
                tSimInterface.getSensor(trainId); // 3 deactivate

                tSimInterface.getSensor(trainId); // 4 activate

                Semaphore overtakeSem = track.overtakeSemaphore();
                switchPos = isGoingToStationTwo() ? Track.OVERTAKE_ONE_SWITCH_POSITION : Track.OVERTAKE_TWO_SWITCH_POSITION;
                if (overtakeSem.availablePermits() == 0) { // A train is on overtake
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                    isOnParallelTrack = true;
                }
                else {
                    overtakeSem.acquire();
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                    isOnParallelTrack = false;
                }
                tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
                tSimInterface.getSensor(trainId); // 4 deactivate


                tSimInterface.getSensor(trainId); // 5 activate
                tSimInterface.getSensor(trainId); // 5 deactivate
                criticalSectionSem.release();

                tSimInterface.setSpeed(trainId, 0);
                criticalSectionSem = isGoingToStationTwo() ? track.criticalSectionTwoSemaphore() : track.criticalSectionOneSemaphore();
                criticalSectionSem.acquire();

                switchPos = isGoingToStationTwo() ? Track.OVERTAKE_TWO_SWITCH_POSITION : Track.OVERTAKE_ONE_SWITCH_POSITION;
                if (isGoingToStationTwo()) {
                    switchDir = isOnParallelTrack ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                }
                else {
                    switchDir = isOnParallelTrack ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                }
                tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
                tSimInterface.setSpeed(trainId, fullSpeed());


                tSimInterface.getSensor(trainId); // 6 activate
                tSimInterface.getSensor(trainId); // 6 activate

                tSimInterface.getSensor(trainId); // 6 activate
                if( !isOnParallelTrack) {
                    overtakeSem.release();
                }
                tSimInterface.getSensor(trainId); // 6 deactivate

                tSimInterface.getSensor(trainId); // 7 activate

                criticalStationSem = isGoingToStationTwo() ? track.stationTwoSemaphore() : track.stationOneSemaphore();
                switchPos = isGoingToStationTwo() ? Track.STATION_TWO_SWITCH_POSITION : Track.STATION_ONE_SWITCH_POSITION;
                if (criticalStationSem.availablePermits() == 0) { // A train is on overtake
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                    isOnParallelTrack = true;
                }
                else {
                    criticalStationSem.acquire();
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                    isOnParallelTrack = false;
                }
                tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
                tSimInterface.getSensor(trainId); // 7 deactivate
                tSimInterface.getSensor(trainId); // 8 activate
                criticalSectionSem.release();
                tSimInterface.getSensor(trainId); // 8 deactivate
                tSimInterface.getSensor(trainId); // 9 activate
                tSimInterface.getSensor(trainId); // 9 deactivate

                sleep(timeUntilBreak());
                tSimInterface.setSpeed(trainId, 0);
                sleep(breakTime());
                sleep(waitingTime());
                direction *= -1; // change direction
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isGoingToStationTwo() {
        return trainId == 1 && direction == 1 || trainId == 2 && direction == -1;
    }

    private int fullSpeed() {
        return this.direction * this.initialSpeed;
    }

    private long timeUntilBreak() {
        return (long) (47.94134 + 8896.056*Math.exp((-0.1638112*initialSpeed)));
    }

    private long breakTime() {
        return 400 + initialSpeed * 30;
    }

    private long waitingTime() {
        return 1000 + 20 * initialSpeed;
    }
}
