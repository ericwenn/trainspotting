import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

/**
 * Class Lab1
 * Constructor takes two Integer objects; speed1 and speed2 are the initialSpeed for each Train.
 * Starts two threads. Each use the Train Class which implements the Runnable interface.
 * Each Train takes their id and speed as well as an instance of the Track Class.
 */

public class Lab1 {

	Lab1(Integer speed1, Integer speed2) {
		Track track = new Track();

		Train train_1 = new Train(1, speed1, track);
		Train train_2 = new Train(2, speed2, track);

		Thread train_1_thread = new Thread(train_1);
		Thread train_2_thread = new Thread(train_2);

		train_1_thread.start();
		train_2_thread.start();
	}

	/*
	 * Model representing track given in the problem statement.
	 * / S1     North station semaphore
	 * ----/-----    North station cross semaphore
	 * \  /
	 * v
	 * |
	 * |       East critical section semaphore
	 * |
	 * --------
	 * |      |
	 * |      |   Overtake semaphore
	 * |      |
	 * --------
	 * |
	 * |       West critical section semaphore
	 * |
	 * ^
	 * / \
	 * /   \     South station semaphore
	 * /  S2 \
	 */

	/**
	 * The Static inner class Track contains the positions of each Switch and Sensor as well as all 6 semaphores the
	 * solution needs.
	 * Each semaphore has neighbouring sensors saved in a matrix of Sensors.
	 * Track also contains the inner class Sensor. The Class Sensor describes the position of the sensor.
	 * SensorDirection is a enum which is used to describe the sensors position in relation to their neighbouring Semaphore.
	 */

	static class Track {

		static final int[] NORTH_STATION_SWITCH_POSITION = {17, 7};
		static final int[] OVERTAKE_EAST_SWITCH_POSITION = {15, 9};
		static final int[] OVERTAKE_WEST_SWITCH_POSITION = {4, 9};
		static final int[] SOUTH_STATION_SWITCH_POSITION = {3, 11};
		final Semaphore northStationSemaphore = new Semaphore(1, true);
		final Semaphore southStationSemaphore = new Semaphore(1, true);
		final Semaphore eastCriticalSectionSemaphore = new Semaphore(1, true);
		final Semaphore westCriticalSectionSemaphore = new Semaphore(1, true);
		final Semaphore overtakeSemaphore = new Semaphore(1, true);
		final Semaphore northStationCrossSemaphore = new Semaphore(1, true);

		Track() {}

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

		static class Sensor {
			final int x;
			final int y;

			Sensor(int x, int y) {
				this.x = x;
				this.y = y;
			}
		}

		final Sensor[][] northStationCrossSensors = new Sensor[][]{
				{
					new Sensor(8, 6),
					new Sensor(8, 5),
					new Sensor(9, 5),
					new Sensor(11, 5)
				},
				{
					new Sensor(9, 7),
					new Sensor(10, 7),
					new Sensor(11, 7),
					new Sensor(13, 7),
				},
				{
					new Sensor(8, 8),
					new Sensor(9, 8),
					new Sensor(10, 8),
					new Sensor(12, 8),
				},
				{
					new Sensor(7, 7),
					new Sensor(6, 7),
					new Sensor(6, 6),
					new Sensor(6, 4),
				}
		};
		final Sensor[][] northStationSwitchSensors = new Sensor[][]{
				{},
				{
					new Sensor(18, 7),
					new Sensor(19, 9)
				},
				{
					new Sensor(17, 8),
					new Sensor(16, 8),
					new Sensor(15, 8),
					new Sensor(13, 8),
				},
				{
					new Sensor(16, 7),
					new Sensor(15, 7),
					new Sensor(14, 7),
					new Sensor(12, 7),
				}
		};
		final Sensor[][] overtakeEastSwitchSensors = new Sensor[][]{
				{},
				{
					new Sensor(16, 9),
					new Sensor(19, 9),
				},
				{
					new Sensor(15, 10),
					new Sensor(14, 10),
					new Sensor(13, 10),
					new Sensor(11, 10),
				},
				{
					new Sensor(14, 9),
					new Sensor(13, 9),
					new Sensor(12, 9),
					new Sensor(10, 9),
				}
		};
		final Sensor[][] overtakeWestSwitchSensors = new Sensor[][]{
				{},
				{
					new Sensor(5, 9),
					new Sensor(6, 9),
					new Sensor(7, 9),
					new Sensor(9, 9),
				},
				{
					new Sensor(4, 10),
					new Sensor(5, 10),
					new Sensor(6, 10),
					new Sensor(8, 10),
				},
				{
					new Sensor(3, 9),
					new Sensor(1, 9),
				}
		};
		final Sensor[][] southStationSwitchSensors = new Sensor[][]{
				{},
				{
					new Sensor(4, 11),
					new Sensor(5, 11),
					new Sensor(6, 11),
					new Sensor(8, 11),
				},
				{
					new Sensor(3, 12),
					new Sensor(3, 13),
					new Sensor(4, 13),
					new Sensor(6, 13),
				},
				{
					new Sensor(2, 11),
					new Sensor(1, 10)
				}
		};
		final Sensor[][] northStationPreferredStopSensors = new Sensor[][]{
				{}, {}, {},
				// West
				{
					new Sensor(17, 3),
					new Sensor(16, 3),
					new Sensor(15, 3),
					new Sensor(13, 3)
				}
		};
		final Sensor[][] northStationStopSensors = new Sensor[][]{
				{}, {}, {},
				// West
				{
					new Sensor(17, 5),
					new Sensor(16, 5),
					new Sensor(15, 5),
					new Sensor(13, 5)
				}
		};
		final Sensor[][] southStationPreferredStopSensors = new Sensor[][]{
				{}, {}, {},
				// West
				{
					new Sensor(16, 11),
					new Sensor(17, 11),
					new Sensor(15, 11),
					new Sensor(13, 11)
				}
		};
		final Sensor[][] southStationStopSensors = new Sensor[][]{
				{}, {}, {},
				// West
				{
					new Sensor(17, 13),
					new Sensor(16, 13),
					new Sensor(15, 13),
					new Sensor(13, 13)
				}
		};
	}

	/**
	 * The Train class represents a train as a thread. Each thread has a trainId, initialSpeed when they are initiated from Lab1.java
	 * Each train use the same track and the same tSimInterface.
	 * The train class has instances of the critical semaphores and the sensors as well as some flags to help navigate
	 * such as isOnPreferredTrack and isOnPreferredOvertake which are used when a train is on a parallel track in a critical section.
	 * stopSensor and releaseSensor are used to help smooth the acquiring and releasing of critical sections.
	 */

	class Train implements Runnable {

		private final int trainId;
		private final int initialSpeed;
		private final Track track;
		private final TSimInterface tSimInterface;
		private int direction;

		int[] switchPos;
		int switchDir, stoppingDistance;
		Semaphore criticalStationSem, criticalSectionSem, overtakeSem;
		Track.Sensor stopSensor, releaseSensor;
		Track.SensorDirection stopDirection, releaseDirection;
		boolean isOnPreferredTrack = true, isOnPreferredOvertake;

		Train(int trainId, int initialSpeed, Track track) {
			this.trainId = trainId;
			this.initialSpeed = initialSpeed;
			this.track = track;
			this.tSimInterface = TSimInterface.getInstance();
			this.direction = 1;
		}

		/**
		 * handleBrake brakes the train in case the upcoming critical section is blocked. It aquires the semaphore or waits for it to be available.
		 * @param sem Is the semaphore for the upcoming critical section.
		 * @throws CommandException Exception from TSim
		 * @throws InterruptedException If the thread is interrupted it will fire an exception.
		 */

		void handleBrake(Semaphore sem) throws CommandException, InterruptedException {
			skipSensorsUntil(stopSensor.x, stopSensor.y, false);
			tSimInterface.setSpeed(trainId, 0);
			sem.acquire();
		}

		/**
		 * handelRestart sets the train to its fullspeed and releases the semaphore if it should be released.
		 * @param sem The semaphore that should be released
		 * @param shouldRelease If the train hasn't been on a parallel track then it should release the critical semaphore. Otherwise not.
		 * @throws CommandException Exception from TSim
		 * @throws InterruptedException If the thread is interrupted it will fire an exception.
		 */


		void handleRestart(Semaphore sem, boolean shouldRelease) throws CommandException, InterruptedException {
			tSimInterface.setSpeed(trainId, fullSpeed());
			skipSensorsUntil(releaseSensor.x, releaseSensor.y, true);
			if (shouldRelease)
				sem.release();
		}

		/**
		 * run is the method that the train will run once Lab1.java have started train1.start()
		 * run consists of a infinite while loop that iterates through each section of the track.
		 */

		@Override
		public void run() {
			try {
				criticalStationSem = isGoingToSouthStation() ? track.northStationSemaphore : track.southStationSemaphore;
				criticalStationSem.acquire();
				stoppingDistance = getStoppingDistance();
				while (true) {
					tSimInterface.setSpeed(trainId, fullSpeed());
					goToNorthSectionCross();
					goToEntranceCriticalSection();
					goToOvertakeSection();
					goToOvertakeExit();
					goToStationEntrance();
					goToNorthStationCross();
					stopAtStation();
				}
			} catch (InterruptedException | CommandException err) {
				err.printStackTrace();
			}
		}

		/**
		 * goToNorthSectionCross describes the idea behind the first critical section in the track. Two trains can collide
		 * at the north station crossing. When a train passes stopSensor it will try to acquire the semaphore at north station crossing.
		 * If it can't acquire the semaphore it will stop until the other train has passed.
		 * @throws CommandException Exception from TSim
		 * @throws InterruptedException If the thread is interrupted it will fire an exception.
		 */

		void goToNorthSectionCross() throws CommandException, InterruptedException {
			// North station cross
			if (isGoingToSouthStation()) {
				stopDirection = isOnPreferredTrack ? Track.SensorDirection.WEST : Track.SensorDirection.NORTH;
				releaseDirection = isOnPreferredTrack ? Track.SensorDirection.EAST : Track.SensorDirection.SOUTH;

				stopSensor = track.northStationCrossSensors[stopDirection.v()][stoppingDistance];
				releaseSensor = track.northStationCrossSensors[releaseDirection.v()][0];

				handleBrake(track.northStationCrossSemaphore);
				handleRestart(track.northStationCrossSemaphore, true);

			}
		}


		/**
		 * goToEntranceCriticalSection describes the second critical section which is the entrance of the critical section after the station.
		 * The train collects all information about sensors, directions and switch positions. The train checks if it can acquire
		 * the semaphore representing the critical section otherwise it will wait until it can. It only releases the station
		 * semaphore if it's on the preferredTrack (not the parallel track).
		 * @throws CommandException Exception from TSim
		 * @throws InterruptedException If the thread is interrupted it will fire an exception.
		 */

		void goToEntranceCriticalSection() throws InterruptedException, CommandException {
			// Entrance first critical section
			criticalSectionSem = isGoingToSouthStation() ? track.eastCriticalSectionSemaphore : track.westCriticalSectionSemaphore;
			switchPos = isGoingToSouthStation() ? Track.NORTH_STATION_SWITCH_POSITION : Track.SOUTH_STATION_SWITCH_POSITION;


			if (isGoingToSouthStation()) {
				stopDirection = isOnPreferredTrack ? Track.SensorDirection.WEST : Track.SensorDirection.SOUTH;
				switchDir = isOnPreferredTrack ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
				releaseDirection = Track.SensorDirection.EAST;
			} else {
				stopDirection = isOnPreferredTrack ? Track.SensorDirection.EAST : Track.SensorDirection.SOUTH;
				switchDir = isOnPreferredTrack ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
				releaseDirection = Track.SensorDirection.WEST;
			}

			stopSensor = (isGoingToSouthStation() ? track.northStationSwitchSensors : track.southStationSwitchSensors)[stopDirection.v()][stoppingDistance];
			releaseSensor = (isGoingToSouthStation() ? track.northStationSwitchSensors : track.southStationSwitchSensors)[releaseDirection.v()][0];

			handleBrake(criticalSectionSem);
			tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
			handleRestart(criticalStationSem, isGoingToSouthStation() && isOnPreferredTrack || !isGoingToSouthStation() && isOnPreferredTrack);
		}

		/**
		 * goToOvertakeSection describes the third critical section. The train collects all information needed and try's to
		 * acquire the semaphore representing the preferred track. If it can then it pass through the entrance and release the
		 * first critical section semaphore. If it's on the preferred track it sets a boolean flag so that the other train
		 * chooses the parallel track.
		 * @throws CommandException Exception from TSim
		 * @throws InterruptedException If the thread is interrupted it will fire an exception.
		 */

		void goToOvertakeSection() throws CommandException, InterruptedException {
			// Overtake entrance
			overtakeSem = track.overtakeSemaphore;
			switchPos = isGoingToSouthStation() ? Track.OVERTAKE_EAST_SWITCH_POSITION : Track.OVERTAKE_WEST_SWITCH_POSITION;
			stopDirection = isGoingToSouthStation() ? Track.SensorDirection.EAST : Track.SensorDirection.WEST;

			stopSensor = (isGoingToSouthStation() ? track.overtakeEastSwitchSensors : track.overtakeWestSwitchSensors)[stopDirection.v()][1];
			skipSensorsUntil(stopSensor.x, stopSensor.y, false);
			if (overtakeSem.tryAcquire()) {
				switchDir = isGoingToSouthStation() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
				isOnPreferredOvertake = true;
				releaseDirection = isGoingToSouthStation() ? Track.SensorDirection.WEST : Track.SensorDirection.EAST;
			} else {
				switchDir = isGoingToSouthStation() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
				isOnPreferredOvertake = false;
				releaseDirection = Track.SensorDirection.SOUTH;
			}
			releaseSensor = (isGoingToSouthStation() ? track.overtakeEastSwitchSensors : track.overtakeWestSwitchSensors)[releaseDirection.v()][0];

			tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
			handleRestart(criticalSectionSem, true);
		}

		/**
		 * goToOvertakeExit describes the third critical section. If it can acquire the critical section then it will pass trough otherwise it will
		 * wait for the other train to choose the other available track in the overtake.
		 * @throws CommandException Exception from TSim
		 * @throws InterruptedException If the thread is interrupted it will fire an exception.
		 */

		void goToOvertakeExit() throws CommandException, InterruptedException {
			// Overtake exit
			criticalSectionSem = isGoingToSouthStation() ? track.westCriticalSectionSemaphore : track.eastCriticalSectionSemaphore;
			switchPos = isGoingToSouthStation() ? Track.OVERTAKE_WEST_SWITCH_POSITION : Track.OVERTAKE_EAST_SWITCH_POSITION;
			if (isGoingToSouthStation()) {
				stopDirection = isOnPreferredOvertake ? Track.SensorDirection.EAST : Track.SensorDirection.SOUTH;
				releaseDirection = Track.SensorDirection.WEST;
				switchDir = isOnPreferredOvertake ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
			} else {
				stopDirection = isOnPreferredOvertake ? Track.SensorDirection.WEST : Track.SensorDirection.SOUTH;
				releaseDirection = Track.SensorDirection.EAST;
				switchDir = isOnPreferredOvertake ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
			}
			stopSensor = (isGoingToSouthStation() ? track.overtakeWestSwitchSensors : track.overtakeEastSwitchSensors)[stopDirection.v()][stoppingDistance];
			releaseSensor = (isGoingToSouthStation() ? track.overtakeWestSwitchSensors : track.overtakeEastSwitchSensors)[releaseDirection.v()][0];

			handleBrake(criticalSectionSem);
			tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
			handleRestart(overtakeSem, isOnPreferredOvertake);
		}

		/**
		 *
		 * @throws CommandException Exception from TSim
		 * @throws InterruptedException If the thread is interrupted it will fire an exception.
		 */

		void goToStationEntrance() throws CommandException, InterruptedException {
			// Station entrance
			criticalStationSem = isGoingToSouthStation() ? track.southStationSemaphore : track.northStationSemaphore;
			switchPos = isGoingToSouthStation() ? Track.SOUTH_STATION_SWITCH_POSITION : Track.NORTH_STATION_SWITCH_POSITION;

			stopDirection = isGoingToSouthStation() ? Track.SensorDirection.WEST : Track.SensorDirection.EAST;

			stopSensor = (isGoingToSouthStation() ? track.southStationSwitchSensors : track.northStationSwitchSensors)[stopDirection.v()][1];
			skipSensorsUntil(stopSensor.x, stopSensor.y, false);

			if (criticalStationSem.tryAcquire()) {
				switchDir = isGoingToSouthStation() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
				isOnPreferredTrack = true;

				releaseDirection = isGoingToSouthStation() ? Track.SensorDirection.EAST : Track.SensorDirection.WEST;
			} else {
				switchDir = isGoingToSouthStation() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
				releaseDirection = Track.SensorDirection.SOUTH;
				isOnPreferredTrack = false;
			}
			releaseSensor = (isGoingToSouthStation() ? track.southStationSwitchSensors : track.northStationSwitchSensors)[releaseDirection.v()][0];

			tSimInterface.setSwitch(switchPos[0], switchPos[1], switchDir);
			handleRestart(criticalSectionSem, true);
		}

		/**
		 *
		 * @throws CommandException Exception from TSim
		 * @throws InterruptedException If the thread is interrupted it will fire an exception.
		 */
		

		void goToNorthStationCross() throws CommandException, InterruptedException {
			// North station cross
			if (!isGoingToSouthStation()) {

				stopDirection = isOnPreferredTrack ? Track.SensorDirection.EAST : Track.SensorDirection.SOUTH;
				releaseDirection = isOnPreferredTrack ? Track.SensorDirection.WEST : Track.SensorDirection.NORTH;

				stopSensor = track.northStationCrossSensors[stopDirection.v()][stoppingDistance];
				releaseSensor = track.northStationCrossSensors[releaseDirection.v()][0];

				handleBrake(track.northStationCrossSemaphore);
				handleRestart(track.northStationCrossSemaphore, true);
			}
		}

		/**
		 *
		 * @throws CommandException Exception from TSim
		 * @throws InterruptedException If the thread is interrupted it will fire an exception.
		 */

		void stopAtStation() throws CommandException, InterruptedException {
			// Station stop
			stopDirection = Track.SensorDirection.WEST;

			if (isGoingToSouthStation()) {
				stopSensor = (isOnPreferredTrack ? track.southStationPreferredStopSensors : track.southStationStopSensors)[stopDirection.v()][stoppingDistance];
			} else {
				stopSensor = (isOnPreferredTrack ? track.northStationPreferredStopSensors : track.northStationStopSensors)[stopDirection.v()][stoppingDistance];
			}

			skipSensorsUntil(stopSensor.x, stopSensor.y, false);
			tSimInterface.setSpeed(trainId, 0);

			sleep(breakTime()); // TODO might not work.
			sleep(waitingTime());

			direction *= -1; // change direction
		}


		/**
		 * skipSensorsUntil skips all sensors till given position. It takes a boolean if it should pass the selected sensor or just activate it.
		 * @param posX describes the selected sensors x position.
		 * @param posY describes the selected sensors y position.
		 * @param untilPass describes if the train should pass the sensor or if it should activate it.
		 * @throws CommandException A TSim exception that could be thrown.
		 * @throws InterruptedException Thread interruption exception.
		 */

		private void skipSensorsUntil(int posX, int posY, boolean untilPass) throws CommandException, InterruptedException {
			int status = untilPass ? SensorEvent.ACTIVE : SensorEvent.INACTIVE;
			SensorEvent se;
			do {
				se = this.tSimInterface.getSensor(this.trainId);
			} while ((se.getXpos() != posX || se.getYpos() != posY) || se.getStatus() == status);
		}

		/**
		 * isGoingToSouthStation describes to which station the train is currently heading to. Assumes that train 1  starts at
		 * the north station and that train 2 starts at the south station.
		 * @return true if the selected train is going to the south station.
		 */

		private boolean isGoingToSouthStation() {
			return this.trainId == 1 && this.direction == 1 || this.trainId == 2 && this.direction == -1;
		}

		/**
		 * fullSpeed describes the speed according to the system which the train is travelling with.
		 * @return a positive speed as int if the train is travelling forward or negative if the train is travelling backwards.
		 */

		private int fullSpeed() {
			return this.direction * this.initialSpeed;
		}

		/**
		 * Calculates some extra time so that the train has time to stop before changing direction.
		 * @return time in millis
		 */

		private long breakTime() {
			return 400 + this.initialSpeed * 32;
		}

		/**
		 * waitingTime calculates how much time the train should wait a station.
		 * @return the waiting time in millis.
		 */

		private long waitingTime() {
			return 1000 + 20 * this.initialSpeed;
		}

		/**
		 * getStoppingDistance returns a estimation on how many units the train's stopping distance is.
		 * @return the stopping distance.
		 * Can throw IllegalArgumentException if the speed is set too high for this solution to manage. Currently it can
		 * manage a speed up to 29 units. 
		 */

		private int getStoppingDistance() {
			if (this.initialSpeed < 11) {
				return 1;
			} else if (this.initialSpeed < 17) {
				return 2;
			} else if (this.initialSpeed < 30) {
				return 3;
			} else {
				throw new IllegalArgumentException("Maximum speed is 30");
			}
		}
	}
}