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


    private boolean isOnPreferredNorthStation = false;
    private boolean isOnPreferredSouthStation = false;
    private boolean isOnPreferredOvertake = false;


    public Train(int trainId, int initialSpeed, Track track) {
        this.trainId = trainId;
        this.initialSpeed = initialSpeed;
        this.track = track;
        this.tSimInterface = TSimInterface.getInstance();
        this.direction = 1;
    }

    @Override
    public void run() {
        int[] switchPos;
        int switchDir;
        Semaphore criticalStationSem;
        Semaphore criticalSectionSem;
        Semaphore overtakeSem;
        int releaseX, releaseY;

        Track.Sensor stopSensor;
        Track.Sensor releaseSensor;
        try {

            criticalStationSem = isGoingToSouthStation() ? track.northStationSemaphore : track.southStationSemaphore;
            criticalStationSem.acquire();
            if( isGoingToSouthStation() ) {
                isOnPreferredNorthStation = true;
            } else {
                isOnPreferredSouthStation = true;
            }


            while(true) {

                tSimInterface.setSpeed(trainId, fullSpeed());




                if( isGoingToSouthStation() ) {
                    Track.SensorDirection d = isOnPreferredNorthStation ? Track.SensorDirection.WEST : Track.SensorDirection.NORTH;

                    stopSensor = track.northStationCrossSensors[d.v()][2];



                }



                criticalSectionSem = isGoingToSouthStation() ? track.criticalSectionOneSemaphore() : track.criticalSectionTwoSemaphore();

                if( isOnParallelTrack ) {
                    releaseX = isGoingToSouthStation() ? 11 : 12;
                    releaseY = isGoingToSouthStation() ? 5 : 13;
                } else {
                    releaseX = isGoingToSouthStation() ? 11 : 12;
                    releaseY = isGoingToSouthStation() ? 3 : 11;
                }


                skipUntil(releaseX, releaseY, true);
                skipSensor(nrOfSkips());
                System.out.println("Train "+trainId+" skipped "+nrOfSkips()+" breaking sensors");
                tSimInterface.setSpeed(trainId, 0);

                criticalSectionSem.acquire();

                if (isGoingToSouthStation()) {
                    switchPos = Track.STATION_ONE_SWITCH_POSITION;
                    switchDir = isOnParallelTrack ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                }
                else {
                    switchPos = Track.STATION_TWO_SWITCH_POSITION;
                    switchDir = isOnParallelTrack ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                }

                tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
                tSimInterface.setSpeed(trainId, fullSpeed());

                releaseX = isGoingToSouthStation() ? 18 : 2;
                releaseY = isGoingToSouthStation() ? 7 : 11;
                skipUntil(releaseX, releaseY, true);

                System.out.println("Train " + trainId + " left station");

                if(!isOnParallelTrack) {
                    System.out.println("Train " + trainId + " releasing station lock");
                    criticalStationSem.release();
                }

                releaseX = isGoingToSouthStation() ? 17 : 1;
                releaseY = isGoingToSouthStation() ? 9 : 9;

                skipUntil(releaseX, releaseY, false);
                System.out.println("Train " + trainId + " is in front of overtake");
                overtakeSem = track.overtakeSemaphore();
                switchPos = isGoingToSouthStation() ? Track.OVERTAKE_ONE_SWITCH_POSITION : Track.OVERTAKE_TWO_SWITCH_POSITION;
                if (overtakeSem.availablePermits() == 0) { // A train is on overtake
                    switchDir = isGoingToSouthStation() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                    isOnParallelTrack = true;
                }
                else {
                    overtakeSem.acquire();
                    switchDir = isGoingToSouthStation() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                    isOnParallelTrack = false;
                }
                tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);

                if( isOnParallelTrack ) {
                    releaseX = isGoingToSouthStation() ? 15 : 4;
                    releaseY = isGoingToSouthStation() ? 10 : 10;
                } else {
                    releaseX = isGoingToSouthStation() ? 14 : 5;
                    releaseY = isGoingToSouthStation() ? 9 : 9;
                }
                skipUntil(releaseX, releaseY, true);

                criticalSectionSem.release();
                System.out.println("Train: " + trainId + " left critical section");

                skipSensor(nrOfSkips());
                tSimInterface.setSpeed(trainId, 0);
                criticalSectionSem = isGoingToSouthStation() ? track.criticalSectionTwoSemaphore() : track.criticalSectionOneSemaphore();
                criticalSectionSem.acquire();

                switchPos = isGoingToSouthStation() ? Track.OVERTAKE_TWO_SWITCH_POSITION : Track.OVERTAKE_ONE_SWITCH_POSITION;
                if (isGoingToSouthStation()) {
                    switchDir = isOnParallelTrack ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                }
                else {
                    switchDir = isOnParallelTrack ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                }
                tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
                tSimInterface.setSpeed(trainId, fullSpeed());

                releaseX = isGoingToSouthStation() ? 3 : 16;
                releaseY = isGoingToSouthStation() ? 9 : 9;

                skipUntil(releaseX, releaseY, true);
                System.out.println("Train "+trainId+" left overtake");
                if( !isOnParallelTrack) {
                    System.out.println("Train "+trainId+" releasing overtake lock");
                    overtakeSem.release();
                }
                skipSensor(1);
                tSimInterface.getSensor(trainId);

                criticalStationSem = isGoingToSouthStation() ? track.stationTwoSemaphore() : track.stationOneSemaphore();
                switchPos = isGoingToSouthStation() ? Track.STATION_TWO_SWITCH_POSITION : Track.STATION_ONE_SWITCH_POSITION;
                if (criticalStationSem.availablePermits() == 0) { // A train is on overtake
                    switchDir = isGoingToSouthStation() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                    releaseX = isGoingToSouthStation() ? 3 : 17;
                    releaseY = isGoingToSouthStation() ? 12 : 8;
                    isOnParallelTrack = true;
                }
                else {
                    criticalStationSem.acquire();
                    switchDir = isGoingToSouthStation() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                    isOnParallelTrack = false;
                    releaseX = isGoingToSouthStation() ? 4 : 16;
                    releaseY = isGoingToSouthStation() ? 11 : 7;
                }
                tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
                skipUntil(releaseX, releaseY, true);
                criticalSectionSem.release();
                System.out.println("Train:" + trainId + " leaving critical section");

                if(isOnParallelTrack) {
                    releaseX = isGoingToSouthStation() ? 8 : 10; // TODO
                    releaseY = isGoingToSouthStation() ? 13 : 8; // TODO
                } else {
                    releaseX = isGoingToSouthStation() ? 9 : 10;
                    releaseY = isGoingToSouthStation() ? 11 : 7;
                }

                skipUntil(releaseX, releaseY, true);
                System.out.println("Train "+trainId+" passed last sensor dont care");

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
        System.out.println("Train: " + trainId + " skipped sensors until: x:" + posX + " y:" + posY);
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


    private boolean isGoingToSouthStation() {
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
