import TSim.CommandException;
import TSim.SensorEvent;
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
                Semaphore criticalSectionSem = isGoingToStationTwo() ? track.criticalSectionOneSemaphore() : track.criticalSectionTwoSemaphore();
                int releaseX = isGoingToStationTwo() ? 11 : 12;
                int releaseY = isGoingToStationTwo() ? 3 : 13;
                skipUntil(releaseX, releaseY, true);
                System.out.println("Train "+trainId+" passed first three sensors");


                System.out.println("Train "+trainId+" skipping "+nrOfSkips()+" breaking sensors");
                skipSensor(nrOfSkips());

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

                releaseX = isGoingToStationTwo() ? 18 : 2;
                releaseY = isGoingToStationTwo() ? 7 : 11;
                skipUntil(releaseX, releaseY, true);

                System.out.println("Train "+trainId+" left station");

                if( !isOnParallelTrack) {
                    System.out.println("Train "+trainId+" releasing station lock");
                    criticalStationSem.release();
                }
                skipSensor(1);
                releaseX = isGoingToStationTwo() ? 16 : 1;
                releaseY = isGoingToStationTwo() ? 9 : 9;

                skipUntil(releaseX, releaseY, false);
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
                tSimInterface.getSensor(trainId);
                skipSensor(1);
                criticalSectionSem.release();
                System.out.println("Train: " + trainId + " left critical section");

                skipSensor(nrOfSkips());
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

                releaseX = isGoingToStationTwo() ? 3 : 16;
                releaseY = isGoingToStationTwo() ? 9 : 9;
                skipUntil(releaseX, releaseY, true);
                System.out.println("Train "+trainId+" left overtake");
                if( !isOnParallelTrack) {
                    System.out.println("Train "+trainId+" releasing overtake lock");
                    overtakeSem.release();
                }
                skipSensor(1);
                tSimInterface.getSensor(trainId);

                criticalStationSem = isGoingToStationTwo() ? track.stationTwoSemaphore() : track.stationOneSemaphore();
                switchPos = isGoingToStationTwo() ? Track.STATION_TWO_SWITCH_POSITION : Track.STATION_ONE_SWITCH_POSITION;
                if (criticalStationSem.availablePermits() == 0) { // A train is on overtake
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                    releaseX = isGoingToStationTwo() ? 3 : 17;
                    releaseY = isGoingToStationTwo() ? 12 : 8;
                    isOnParallelTrack = true;
                }
                else {
                    criticalStationSem.acquire();
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                    isOnParallelTrack = false;
                    releaseX = isGoingToStationTwo() ? 4 : 16;
                    releaseY = isGoingToStationTwo() ? 11 : 7;
                }
                tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
                skipUntil(releaseX, releaseY, true);
                criticalSectionSem.release();
                System.out.println("Train:" + trainId + " leaving critical section");

                if( isOnParallelTrack ) {
                    releaseX = isGoingToStationTwo() ? 8 : 0; // TODO
                    releaseY = isGoingToStationTwo() ? 13 : 0; // TODO
                } else {
                    releaseX = isGoingToStationTwo() ? 0 : 10;
                    releaseY = isGoingToStationTwo() ? 0 : 7;
                }

                skipUntil(releaseX, releaseY, true);

                skipSensor(nrOfSkips());
                tSimInterface.setSpeed(trainId, 0);
                sleep(breakTime());
                sleep(waitingTime());
                direction *= -1; // change direction
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void skipSensor(int nSkips) throws CommandException, InterruptedException {
        for (int i = 0; i < nSkips; i++) {
            tSimInterface.getSensor(trainId);
            tSimInterface.getSensor(trainId);
        }
    }

    private void skipUntil(int posX, int posY, boolean untilPass) throws CommandException, InterruptedException {
        int status = untilPass ? SensorEvent.ACTIVE : SensorEvent.INACTIVE;
        SensorEvent se;
        do {
            se = tSimInterface.getSensor(trainId);
        } while( (se.getXpos() != posX || se.getYpos() != posY) || se.getStatus() == status);
    }

    private int nrOfSkips() {
        if( initialSpeed > 25 ) {
            return 1;
        } else if( initialSpeed > 10) {
            return 2;
        } else {
            return 3;
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
