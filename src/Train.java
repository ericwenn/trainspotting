import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

import java.util.concurrent.Semaphore;

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
        Track.Sensor _switch;
        int switchDir;
        Semaphore criticalStationSem;
        Semaphore criticalSectionSem;
        Semaphore overtakeSem;
        int releaseX, releaseY;

        int stoppingDistance = 5;
        int i;

        Track.Node<Track.Sensor> stoppingNode;
        Track.Node<Track.Sensor> releaseNode;

        try {
            criticalStationSem = isGoingToStationTwo() ? track.northStationSemaphore : track.southStationSemaphore;
            criticalStationSem.acquire();

            boolean isOnPreferred;

            while(true) {
                tSimInterface.setSpeed(trainId, fullSpeed());



                if( isGoingToStationTwo() ) {
                    stoppingNode = track.northStationCross.parents().get( northStationPreferred ? 0 : 1);
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






                Track.Node<Track.Sensor> stationSwitch = isGoingToStationTwo() ? track.northStationSwitch : track.southStationSwitch;

                if( isGoingToStationTwo() ) {
                    stoppingNode = stationSwitch.parents().get( northStationPreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.parents().get(0);
                    }
                    releaseNode = stationSwitch.children().get(0);

                } else {
                    stoppingNode = stationSwitch.children().get( southStationPreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.children().get(0);
                    }
                    releaseNode = stationSwitch.parents().get(0);
                }
                skipUntil(stoppingNode.data(), false);
                System.out.println("Train "+trainId+" started to break...");
                tSimInterface.setSpeed(trainId, 0);

                criticalSectionSem = isGoingToStationTwo() ? track.eastCriticalSectionSemaphore : track.westCriticalSectionSemaphore;
                criticalSectionSem.acquire();
                System.out.println("Train "+trainId+" acquired critical section");

                _switch = stationSwitch.data();
                if (isGoingToStationTwo()) {
                    switchDir = northStationPreferred ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                } else {
                    switchDir = southStationPreferred ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                }

                tSimInterface.setSwitch(_switch.posX, _switch.posY, switchDir);
                tSimInterface.setSpeed(trainId, fullSpeed());

                skipUntil(releaseNode.data(), true);
                criticalStationSem.release();
                System.out.println("Train "+trainId+" releases station...");





                Track.Node<Track.Sensor> overtakeSwitch = isGoingToStationTwo() ? track.overtakeEastSwitch : track.overtakeWestSwitch;
                _switch = overtakeSwitch.data();
                if( isGoingToStationTwo() ) {
                    stoppingNode = overtakeSwitch.parents().get(0);
                    for( i = 1; i < stoppingDistance; i++ ) {
                        stoppingNode = stoppingNode.parents().get(0);
                    }
                } else {
                    stoppingNode = overtakeSwitch.children().get(0);
                    for( i = 1; i < stoppingDistance; i++ ) {
                        stoppingNode = stoppingNode.children().get(0);
                    }
                }
                System.out.println("Overtake:");
                skipUntil(stoppingNode.data(), false);
                System.out.println("Train "+1+" starts approaching overtake...");

                if( track.overtakeSemaphore.availablePermits() == 0) {
                    System.out.println("Train "+trainId+" takes parallell overtake route...");
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                } else {
                    System.out.println("Train "+trainId+" takes preferred overtake route...");
                    track.overtakeSemaphore.acquire();
                    overtakePreferred = true;
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                }

                tSimInterface.setSwitch( _switch.posX, _switch.posY, switchDir);

                if( overtakePreferred ) {
                    releaseNode = isGoingToStationTwo() ? overtakeSwitch.children().get(0) : overtakeSwitch.parents().get(0);
                } else {
                    releaseNode = isGoingToStationTwo() ? overtakeSwitch.children().get(1) : overtakeSwitch.parents().get(1);
                }
                skipUntil( releaseNode.data(), true);
                criticalSectionSem.release();





                overtakeSwitch = isGoingToStationTwo() ? track.overtakeWestSwitch : track.overtakeEastSwitch;
                if ( isGoingToStationTwo() ) {
                    stoppingNode = overtakeSwitch.parents().get( overtakePreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.parents().get(0);
                    }
                    releaseNode = overtakeSwitch.children().get(0);
                } else {
                    stoppingNode = overtakeSwitch.children().get( overtakePreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.children().get(0);
                    }
                    releaseNode = overtakeSwitch.parents().get(0);
                }

                skipUntil( stoppingNode.data(), false);
                tSimInterface.setSpeed(trainId,0);
                System.out.println("Train "+trainId+" approaching end of overtake");
                criticalSectionSem = isGoingToStationTwo() ? track.westCriticalSectionSemaphore : track.eastCriticalSectionSemaphore;

                criticalSectionSem.acquire();
                System.out.println("Train "+trainId+" acquired critical section");
                tSimInterface.setSpeed(trainId, fullSpeed());

                // Sätt switch










                /*

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
