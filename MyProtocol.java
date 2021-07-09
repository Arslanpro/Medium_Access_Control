package my_protocol;

import framework.IMACProtocol;
import framework.MediumState;
import framework.TransmissionInfo;
import framework.TransmissionType;

import java.util.Random;

/**
 * A fairly trivial Medium Access Control scheme.
 *
 * @author Jaco ter Braak, University of Twente
 * @version 05-12-2013
 *
 *          Copyright University of Twente, 2013-2019
 *
 **************************************************************************
 *          Copyright notice * * This file may ONLY be distributed UNMODIFIED. *
 *          In particular, a correct solution to the challenge must NOT be
 *          posted * in public places, to preserve the learning effect for
 *          future students. *
 **************************************************************************
 */

public class MyProtocol implements IMACProtocol {

	// Idle state: Node stop sending data, and when QueueLength != 0 has 60%
	// probability turned into Test state and send data
	// Test state: Node send data for test
	// Active state: after successfully test, Node turned into Active state and
	// continually send data
	enum NodeState {
		Idle, Test, Active
	}

	NodeState state;
	private int slotCount; // Counting the number of how many time slots used by a single Node

	private final int LIMITATION = 5; // limitation for a single Node continually using time slots, for increasing
										// fairness

	// Class Constructor, set the initial state as Idle
	public MyProtocol() {
		state = NodeState.Idle;
	}

	@Override
	public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation,
			int localQueueLength) {

		switch (state) {

		case Idle:
			if (localQueueLength == 0) {
				System.out.print("SLOT - No data to send.");
				return new TransmissionInfo(TransmissionType.Silent, 0);
			} else {
				if (controlInformation == 0 && new Random().nextInt(100) < 60) {
					state = NodeState.Test;
					slotCount = 1;
					System.out.println("SLOT - Sending data and hope for no collision.");
					return new TransmissionInfo(TransmissionType.Data, localQueueLength - 1);
				} else {
					System.out.println("SLOT - Not sending data to give room for others.");
					return new TransmissionInfo(TransmissionType.Silent, 0);
				}
			}

		case Test:
			if (previousMediumState == MediumState.Collision) {
				if (new Random().nextInt(100) < 20) {
					System.out.println("SLOT - Sending data and hope for no collision.");
					slotCount = 1;
					return new TransmissionInfo(TransmissionType.Data, localQueueLength - 1);
				} else {
					state = NodeState.Idle;
					System.out.println("SLOT - Not sending data to give room for others.");
					return new TransmissionInfo(TransmissionType.Silent, 0);
				}
			} else {
				if (localQueueLength == 0) {
					state = NodeState.Idle;
					System.out.println("SLOT - No data to send.");
					return new TransmissionInfo(TransmissionType.Silent, 0);
				} else {
					state = NodeState.Active;
					slotCount++;

					System.out.println("SLOT - Sending data and hope for no collision.");
					return new TransmissionInfo(TransmissionType.Data, localQueueLength - 1);
				}
			}

		case Active:
			if (slotCount >= LIMITATION) {
				state = NodeState.Idle;
				System.out.println("SLOT - give slots for others");
				return new TransmissionInfo(TransmissionType.Silent, 0);
			}

			if (localQueueLength - 1 == 0) {
				state = NodeState.Idle;
				System.out.println("SLOT - last data will be sent");
			} else {
				System.out.println("SLOT - Sending data");
			}

			slotCount++;

			return new TransmissionInfo(TransmissionType.Data, localQueueLength - 1);

		default:
			return null;

		}

	}
}