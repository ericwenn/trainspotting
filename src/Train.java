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

    private boolean northStationPreferred = false;
    private boolean overtakePreferred = false;
    private boolean southStationPreferred = false;

    public Train(int trainId, int initialSpeed, Track track) {
        this.trainId = trainId;
        this.initialSpeed = initialSpeed;
        this.track = track;
        this.tSimInterface = TSimInterface.getInstance();
        this.direction = 1;
        if( this.trainId == 1) {
            northStationPreferred = true;
        } else {
            southStationPreferred = true;
        }
    }

    @Override
    public void run() {
        int[] switchPos;
        int switchDir;
        Semaphore criticalStationSem;
        Semaphore criticalSectionSem;
        Semaphore overtakeSem;
        int releaseX, releaseY;

        int stoppingDistance = 5;
        int i;



        try {
            criticalStationSem = isGoingToStationTwo() ? track.northStationSemaphore : track.southStationSemaphore;
            criticalStationSem.acquire();

            boolean isOnPreferred;

            while(true) {
                tSimInterface.setSpeed(trainId, fullSpeed());



                if( isGoingToStationTwo() ) {
                    Track.Node<Track.Sensor> stoppingNode = track.northStationCross.parents().get( northStationPreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.parents().get(0);
                    }

                    skipUntil(stoppingNode.data(), false);
                    System.out.println("Train "+trainId+" started to break...");
                    tSimInterface.setSpeed( trainId, 0);

                    track.northStationCrossSemaphore.acquire();
                    System.out.println("Train "+trainId+" acquired semaphore, accelerating...");
                    tSimInterface.setSpeed(trainId, fullSpeed());

                    Track.Node<Track.Sensor> releasingNode = track.northStationCross.children().get(0);
                    skipUntil(releasingNode.data(), true);
                    System.out.println("Train "+trainId+" passed semaphore, releasing...");
                    track.northStationCrossSemaphore.release();
                }





                /*
                criticalSectionSem = isGoingToStationTwo() ? track.criticalSectionOneSemaphore() : track.criticalSectionTwoSemaphore();

                if( isOnParallelTrack ) {
                    releaseX = isGoingToStationTwo() ? 11 : 12;
                    releaseY = isGoingToStationTwo() ? 5 : 13;
                } else {
                    releaseX = isGoingToStationTwo() ? 11 : 12;
                    releaseY = isGoingToStationTwo() ? 3 : 11;
                }


                skipUntil(releaseX, releaseY, true);
                skipSensor(nrOfSkips());
                System.out.println("Train "+trainId+" skipped "+nrOfSkips()+" breaking sensors");
                tSimInterface.setSpeed(trainId, 0);

                criticalSectionSem.acquire();

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

                System.out.println("Train " + trainId + " left station");

                if(!isOnParallelTrack) {
                    System.out.println("Train " + trainId + " releasing station lock");
                    criticalStationSem.release();
                }

                releaseX = isGoingToStationTwo() ? 17 : 1;
                releaseY = isGoingToStationTwo() ? 9 : 9;

                skipUntil(releaseX, releaseY, false);
                System.out.println("Train " + trainId + " is in front of overtake");
                overtakeSem = track.overtakeSemaphore();
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

                if( isOnParallelTrack ) {
                    releaseX = isGoingToStationTwo() ? 15 : 4;
                    releaseY = isGoingToStationTwo() ? 10 : 10;
                } else {
                    releaseX = isGoingToStationTwo() ? 14 : 5;
                    releaseY = isGoingToStationTwo() ? 9 : 9;
                }
                skipUntil(releaseX, releaseY, true);

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

                if(isOnParallelTrack) {
                    releaseX = isGoingToStationTwo() ? 8 : 10; // TODO
                    releaseY = isGoingToStationTwo() ? 13 : 8; // TODO
                } else {
                    releaseX = isGoingToStationTwo() ? 9 : 10;
                    releaseY = isGoingToStationTwo() ? 11 : 7;
                }

                skipUntil(releaseX, releaseY, true);
                System.out.println("Train "+trainId+" passed last sensor dont care");

                skipSensor(nrOfSkips());
                tSimInterface.setSpeed(trainId, 0);
                sleep(breakTime());
                sleep(waitingTime());
                direction *= -1; // change direction
                */
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

    private void skipUntil( Track.Sensor sensor, boolean untilPass) throws CommandException, InterruptedException {
        skipUntil(sensor.posX, sensor.posY, untilPass);
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
