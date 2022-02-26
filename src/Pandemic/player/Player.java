package Pandemic.player;

import java.util.Random;
import java.util.Scanner;

import Pandemic.Gameboard.GameBoard;
import Pandemic.Gameboard.SimulatePandemic;
import Pandemic.cities.City;
import Pandemic.variables.Disease;
import Pandemic.variables.Piece;
import Pandemic.variables.Variables;
import Pandemic.actions.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class Player implements Cloneable {

	String playerName;
	int tactic;
	String playerRole;
	GameBoard pandemicBoard;
	Piece playerPiece;
	int playerAction; // number of action per turn for each player
	boolean activePlayer;
	ArrayList<City> hand; // hand_cards maybe from action
	String[] possibleColour = { "Red", "Blue", "Yellow", "Black" };
	ArrayList<Action> suggestions = new ArrayList<Action>();
	String[][] playerSuggestions = new String[4][4];
	ArrayList<Boolean> travelWithResearch = new ArrayList<Boolean>();
	int[][] profileTable = new int[4][4];
	int[] evaluationTable = new int[4];
	int previousMoveIndex = -1;

	/*
	 * Constructor for objects of class Player
	 */
	public Player(String pName, String pRole) {
		playerName = pName;
		hand = new ArrayList<City>();
		playerAction = 4;
		tactic = 50;
		playerRole = pRole;
		// Initialize some variables
		initializeTables();
	}

	// Initialize tables
	public void initializeTables() {
		int i, j = 0;
		for (i = 0; i < 4; i++) {
			evaluationTable[i] = 0;
			for (j = 0; j < 4; j++) {
				profileTable[i][j] = 0;
				playerSuggestions[i][j] = "rollDice:null";
			}
		}
	}

	public void setGameBoard(GameBoard currentBoard) {
		this.pandemicBoard = currentBoard;
	}

	public void setPlayerPiece(Piece newPiece) {
		playerPiece = newPiece;
	}

	public ArrayList<Action> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(ArrayList<Action> suggestions) {
		this.suggestions = suggestions;
	}

	/*
	 * draw @param numberOfCards from playerPile and check if it is epidemic and
	 * call @resolveEpidemic() or draw to @hand()
	 */
	public void drawCard(int numberOfCards, boolean test1) {
		// draws a card for the player from the board
		for (int i = 0; i < numberOfCards; i++) {
			if (test1 == true) {
				// create instance of Random class
				Random rand = new Random();
				int rand_int1 = rand.nextInt(pandemicBoard.playerPile.size());
				while (pandemicBoard.playerPile.get(rand_int1).equals(Variables.isEpidemic)) {
					rand_int1 = rand.nextInt(pandemicBoard.playerPile.size());
					Collections.shuffle(pandemicBoard.playerPile);
				}
				// adds a new card to the players hand.
				hand.add((City) pandemicBoard.playerPile.get(rand_int1));
				pandemicBoard.playerPile.remove(rand_int1);// remove the card from PlayerDeck
				System.out.println(this.getPlayerName() + " draws a card");
			} else {
				if (pandemicBoard.playerPile.size() != 0) {
					// first element from array list PlayerPile
					if (pandemicBoard.playerPile.get(0).equals(Variables.isEpidemic)) {
						System.out.println("-----EPIDEMIC DRAWN!-----");
						pandemicBoard.resolveEpidemic();// follow the steps for epidemic event
						pandemicBoard.playerPile.remove(0);
						break;
					} else {
						// adds a new card to the players hand.
						hand.add((City) pandemicBoard.playerPile.get(0));
					}
					pandemicBoard.playerPile.remove(0);// remove the card from PlayerDeck
					System.out.println(this.getPlayerName() + " draws a card");
				} else {
					System.out.println("no more cards left");
				}
			}
		}
	}

	/*
	 * Count how many cards with a specific colour player has in his hands
	 * 
	 * @param colour
	 */
	public int getCountXCards(String colour) {
		int toReturn = 0;
		for (int i = 0; i < hand.size(); i++) {
			if (hand.get(i).getColour().equals(colour)) {
				toReturn++;
			}
		}
		return toReturn;
	}

	/*
	 * remove a card from hand , then calls methods to put it in the discard pile()
	 * and remove the card from the hand.
	 */
	public void discardCard(String cardName) {
		int toDiscard = 0;
		for (int i = 0; i < getHand().size(); i++) {
			if (cardName.equals(getHand().get(i).getName())) {
				// System.out.println("found matching card in hand");
				toDiscard = i;
			}
		}
		pandemicBoard.addPlayerCardDiscard(hand.get(toDiscard));// remove from playerDeck to put in PlayerDiscardDeck
		hand.remove(toDiscard);
	}

	// discard all the cards needed for cure
	public void discardCureCards(String colour, int numberToDiscard) {
		for (int i = 0; i < numberToDiscard; i++) {
			for (int x = 0; x < hand.size(); x++) {
				if (hand.get(x).getColour().equals(colour)) {
					discardCard(hand.get(x).getName());
					break;
				}
			}
		}
	}

	// get PlayerAction
	public int getPlayerAction() {
		return playerAction;
	}

	// decrease the playerAction (Max=4)
	public void decreasePlayerAction() {
		playerAction--;
	}

	// sets a players action back to 4
	public void resetPlayerAction() {
		playerAction = 4;
	}

	// return the name of player
	public String getPlayerName() {
		return playerName;
	}

	// Returns an array with the players cards in hand
	public ArrayList<City> getHand() {
		return hand;
	}

	public String getPlayerRole() {
		return playerRole;
	}

	public void setPlayerRole(String playerRole) {
		this.playerRole = playerRole;
	}

	public Piece getPlayerPiece() {
		return playerPiece;
	}

	/**********************************************************************************
	 ******* These are the main (7+4) methods used to control the players actions********
	 **********************************************************************************/

//Build research station as OPERATIONS_EXPERT

	// Build research station
	public boolean buildResearchStation(Player curr_player) {
		if (playerAction > 0) {
			buildResearchStation tmp = new buildResearchStation(curr_player.playerPiece.getLocation(), getHand());
			if (curr_player.playerRole.equals("OPERATIONS_EXPERT")
					&& !Variables.CITY_WITH_RESEARCH_STATION.contains(curr_player.playerPiece.getLocation())) {
				Variables.ADD_CITY_WITH_RESEARCH_STATION(curr_player.playerPiece.getLocation());
				decreasePlayerAction();
				System.out.println("building a research station in " + curr_player.playerPiece.getLocation().getName());
				suggestions.add(tmp);
				return true;
			} else {
				if (tmp.isaLegalMove()) {
					discardCard(curr_player.playerPiece.getLocation().getName());
					Variables.ADD_CITY_WITH_RESEARCH_STATION(playerPiece.getLocation());
					decreasePlayerAction();
					// System.out.println("building a research station in " +
					// playerPiece.getLocation().getName());
					suggestions.add(tmp);
					return true;
				}
			}
		}

		return false;
	}

//Treat disease action
	public boolean treatDisease(City location, String colour, Player curr_player) {
		if (playerAction > 0) {
			treatDisease tmp = new treatDisease(location, colour);
			if (curr_player.playerRole.equals("MEDIC")) {
				if (tmp.isaLegalMove() == true && location == curr_player.playerPiece.getLocation()) {
					System.out.println("Removing all " + colour + " cube from " + location.getName());
					while (location.getCubeColour(colour) != 0) {
						location.removeCube(colour);
						pandemicBoard.addCube(colour);// add in pool of cube
					}
					decreasePlayerAction();
					suggestions.add(tmp);
					return true;
				}
			} else {
				if (tmp.isaLegalMove() == true && location == curr_player.playerPiece.getLocation()) {
					System.out.println("Removing a " + colour + " cube from " + location.getName());
					location.removeCube(colour);
					pandemicBoard.addCube(colour);// add in pool of cube
					decreasePlayerAction();
					suggestions.add(tmp);
					return true;
				}
			}
		}
		return false;
	}

	// Drive action
	public boolean driveCity(City location, City destination, Player curr_player) {
		if (playerAction > 0) {
			System.out.println(getPlayerName() + " current location is in " + location);
			System.out.println("and he wants to go in " + destination);
			// System.out.println(location.getMaxCube());
			// System.out.println(destination.getMaxCube());
			// System.out.println("attempting to move " + getPlayerName() + " to " +
			// destination.getName() + " from "+ location.getName());
			driveCity tmp = new driveCity(location, destination);
			if (tmp.isaLegalMove()) {
				System.out.println(
						getPlayerName() + " drives from " + location.getName() + " to " + destination.getName() + ".");
				curr_player.playerPiece.setLocation(destination);
				System.out.println("NEXT LOCATION: " + curr_player.playerPiece.getLocation());
				// MEDIC REMOVING ALL CUBES
				if (curr_player.playerRole.equals("MEDIC")
						&& pandemicBoard.getDisease(destination.getColour()).getCured()) {
					while (destination.getMaxCube() > 0) {
						destination.removeCube(destination.getColour());
						pandemicBoard.addCube(destination.getColour());
					}
					System.out.println("I REMOVED ALL THE CUBES");
				}
				decreasePlayerAction();
				suggestions.add((driveCity) tmp);
				return true;
			} else {
				System.out.println("the location isn't connected");
			}
		}
		return false;
	}

	// Charter Flight action
	public boolean charterFlight(City location, City destination, Player curr_player) {
		if (playerAction > 0) {
			// System.out.println(getPlayerName() + " wants to flying from " +
			// location.getName() + " to "+ destination.getName() +
			// " on a charter flight");
			charterFlight tmp = new charterFlight(location, curr_player.getHand(), destination);
			if (tmp.isaLegalMove() && playerPiece.getLocation().equals(location)) {
				System.out.println(getPlayerName() + " takes a charter flight to " + destination.getName() + " from "
						+ location.getName());
				curr_player.discardCard(location.getName());
				curr_player.playerPiece.setLocation(destination);
				if (curr_player.playerRole.equals("MEDIC")
						&& pandemicBoard.getDisease(destination.getColour()).getCured()) {
					while (destination.getMaxCube() > 0) {
						destination.removeCube(destination.getColour());
						pandemicBoard.addCube(destination.getColour());
					}
					System.out.println("I REMOVED ALL THE CUBES");
				}
				decreasePlayerAction();
				suggestions.add(tmp);
				return true;
			}
		}
		return false;
	}

	// Direct flight
	public boolean directFlight(City location, City destination, Player curr_player) {
		if (playerAction > 0) {
			// System.out.println(getPlayerName() + " wants to flying from " +
			// location.getName() + " to "+ destination.getName() +
			// " on a direct flight");
			directFlight tmp = new directFlight(destination, curr_player.getHand());
			if (tmp.isaLegalMove()) {
				System.out.println(getPlayerName() + " takes a direct flight to " + destination.getName() + " from "
						+ location.getName());
				curr_player.discardCard(destination.getName());
				curr_player.playerPiece.setLocation(destination);
				if (curr_player.playerRole.equals("MEDIC")
						&& pandemicBoard.getDisease(destination.getColour()).getCured()) {
					while (destination.getMaxCube() > 0) {
						destination.removeCube(destination.getColour());
						pandemicBoard.addCube(destination.getColour());
					}
					System.out.println("I REMOVED ALL THE CUBES");
				}
				decreasePlayerAction();
				suggestions.add(tmp);
				return true;
			}
		}
		return false;
	}

	// ShuttleFlight
	public boolean shuttleFlight(City location, City destination, Player curr_player) {
		if (playerAction > 0) {
			// System.out.println(getPlayerName() + " wants to flying from " +
			// location.getName() + " to "+ destination.getName() +
			// " on a shuttle flight");
			shuttleFlight tmp = new shuttleFlight(location, destination);
			if (tmp.isaLegalMove()) {
				System.out.println(getPlayerName() + " takes a shuttle flight to " + destination.getName() + " from "
						+ location.getName());
				curr_player.playerPiece.setLocation(destination);
				if (curr_player.playerRole.equals("MEDIC")
						&& pandemicBoard.getDisease(destination.getColour()).getCured()) {
					while (destination.getMaxCube() > 0) {
						destination.removeCube(destination.getColour());
						pandemicBoard.addCube(destination.getColour());
					}
					System.out.println("I REMOVED ALL THE CUBES");
				}
				decreasePlayerAction();
				suggestions.add(tmp);
				return true;
			}
		}
		return false;
	}

	// Discover Cure action
	public boolean discoverCure(City location, String colour, Player curr_player) {
		if (playerAction > 0) {
			System.out.println("CITIES WITH RES: " + pandemicBoard.researchCentres);
			System.out.println("COLOR TO CURE: " +colour);
			for(int i=0;i<curr_player.getHand().size();i++) {
				System.out.println("Card: " +i+" Color: " +curr_player.getHand().get(i).getColour());
			}
			discoverCure tmp = new discoverCure(location, curr_player.getHand(), colour);
			if (curr_player.playerRole.equals("SCIENTIST")) {
				if (tmp.isaLegalMove(1)) {
					System.out.println("Its possible to discover a cure");
					curr_player.discardCureCards(colour,
							(pandemicBoard.getNeededForCure(curr_player.getPlayerRole()) - 1));
					pandemicBoard.cureDisease(colour);
					decreasePlayerAction();
					suggestions.add(tmp);
					return true;
				}
			} else {
				if (tmp.isaLegalMove(0)) {
					System.out.println("Its possible to discover a cure");
					curr_player.discardCureCards(colour, pandemicBoard.getNeededForCure(curr_player.getPlayerRole()));
					pandemicBoard.cureDisease(colour);
					decreasePlayerAction();
					suggestions.add(tmp);
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * Below are implemented 1) --> OPERATIONS_EXPERT special ability (build
	 * research station without to discard a card) is implemented as if in
	 * the @BuildResearchStaion method 2) --> MEDIC special ability (treat all the
	 * cubes of a specific colour) implemented in @treatDisease as if statement 3)
	 * --> SCIENTIST special ability is implemented as if in the classic cure
	 * disease 4) --> QUARANTINE_SPECIALIST special ability is implemented
	 * in @infectCityPhase in @GameBoard class
	 */

	// ---------------------------------------------------------------------------------
	/**********************************************************************************
	 ***************** These methods are used for AI controlled players.****************
	 **********************************************************************************/
	// ---------------------------------------------------------------------------------

	// Check if a move given from a suggestion is legal
	public boolean checkLegalMove(String action) {
		// Initialize
		String[] takeAction = action.split(":"); // Split in delimiter ":"
		String tempAction = takeAction[0]; // final action
		City targetCity = stringToCity(takeAction[1]); // City to take the action

		// Iterate through all tempActions and check for legal Moves
		if (tempAction.equals("driveCity")) {
			driveCity tmp = new driveCity(getPlayerPiece().getLocation(), targetCity);
			if (tmp.isaLegalMove()) {
				return true;
			} else {
				return false;
			}
		} else if (tempAction.equals("treatDisease")) {
			treatDisease tmp = new treatDisease(getPlayerPiece().getLocation(), targetCity.getColour());
			if (playerRole.equals("MEDIC")) {
				if (tmp.isaLegalMove() == true && getPlayerPiece().getLocation() == playerPiece.getLocation()) {
					return true;
				} else {
					return false;
				}
			} else {
				if (tmp.isaLegalMove() == true && getPlayerPiece().getLocation() == playerPiece.getLocation()) {
					return true;
				} else {
					return false;
				}
			}
		} else if (tempAction.equals("discoverCure")) {
			discoverCure tmp = new discoverCure(getPlayerPiece().getLocation(), getHand(), targetCity.getColour());
			if (playerRole.equals("SCIENTIST")) {
				if (tmp.isaLegalMove(1)) {
					return true;
				} else {
					return false;
				}
			} else {
				if (tmp.isaLegalMove(0)) {
					return true;
				} else {
					return false;
				}
			}
		} else if (tempAction.equals("shuttleFlight")) {
			shuttleFlight tmp = new shuttleFlight(getPlayerPiece().getLocation(), targetCity);
			if (tmp.isaLegalMove()) {
				return true;
			} else {
				return false;
			}
		} else if (tempAction.equals("charterFlight")) {
			charterFlight tmp = new charterFlight(getPlayerPiece().getLocation(), getHand(), targetCity);
			if (tmp.isaLegalMove()) {
				return true;
			} else {
				return false;
			}
		} else if (tempAction.equals("directFlight")) {
			directFlight tmp = new directFlight(targetCity, getHand());
			if (tmp.isaLegalMove()) {
				return true;
			} else {
				return false;
			}
		} else if (tempAction.equals("buildResearchStation")) {
			buildResearchStation tmp = new buildResearchStation(playerPiece.getLocation(), getHand());
			if (playerRole.equals("OPERATIONS_EXPERT")
					&& !Variables.CITY_WITH_RESEARCH_STATION.contains(playerPiece.getLocation())) {
				return true;
			} else {
				if (tmp.isaLegalMove()) {
					return true;
				} else {
					return false;
				}
			}
		} else {
			// Wrong Input return false
			return false;
		}
	}

	// Return a City target from String
	public City stringToCity(String cityName) {
		City tempCity = new City();
		int i = 0;
		for (i = 0; i < pandemicBoard.getCities().size(); i++) {
			if (pandemicBoard.getCities().get(i).getName().equals(cityName)) {
				tempCity = pandemicBoard.getCities().get(i);
				break;
			}
		}
		return tempCity;
	}

//take 1 step distance Cities from target and check if hand contains some City
	public ArrayList<City> oneStepCards(ArrayList<City> player_hand, City target) {
		ArrayList<City> cardsInHand = new ArrayList<>();

		// Iterate
		for (int i = 0; i < target.getNeighbors().size(); i++) {
			if (player_hand.contains(target.getNeighbors().get(i))
					&& !cardsInHand.contains(target.getNeighbors().get(i))) {
				cardsInHand.add(target.getNeighbors().get(i));
			}
		}

		return cardsInHand;
	}

	// take 1 and 2 step distance Cities from target and check if hand contains some
	// City
	public ArrayList<City> twoStepCards(ArrayList<City> player_hand, City target) {
		ArrayList<City> cardsInHand = new ArrayList<>();
		cardsInHand = oneStepCards(player_hand, target); // One distance cities in hand
		// Iterate
		for (int i = 0; i < target.getNeighbors().size(); i++) { // target neighbor
			City targetNeighbor = target.getNeighbors().get(i);
			for (int j = 0; j < targetNeighbor.getNeighbors().size(); j++) // neighbor of neighbor
				if (player_hand.contains(targetNeighbor.getNeighbors().get(j))
						&& !cardsInHand.contains(targetNeighbor.getNeighbors().get(j))) {
					cardsInHand.add(targetNeighbor.getNeighbors().get(j));
				}
		}

		return cardsInHand;
	}

	// Find cities in hand with max distance 2 from target and take the one that is
	// farthest from research
	public City cityTargetForResearchStation(Player curr_player, City target) {
		ArrayList<City> cardsInHand = twoStepCards(curr_player.hand, target);
		int size = cardsInHand.size();

		if (size == 0) { // Empty array list
			return null;
		} else if (size == 1) {
			return cardsInHand.get(0);
		} else {
			// Iterate through all cities In Hand
			City tempCity = new City();
			int tempDistance = -1;
			int distance = -1;

			for (int i = 0; i < size; i++) {
				distance = curr_player.getDistanceResearchFromDestination(cardsInHand.get(i));
				// check the distance
				if (distance > tempDistance) {
					tempDistance = distance;
					tempCity = cardsInHand.get(i);
				}
			}
			return tempCity;
		}
	}

	// get distance (closest to the destination)
	public int getDistanceResearchFromDestination(City destination) {
		City closestCity = pandemicBoard.getResearchCentres().get(0);
		int closestDistance = calcDistanceBFS(destination, closestCity);
		int tempDist = 0;

		for (int i = 1; i < pandemicBoard.getResearchCentres().size(); i++) {
			tempDist = calcDistanceBFS(destination, pandemicBoard.getResearchCentres().get(i));
			if (tempDist < closestDistance) {
				closestDistance = tempDist;
			}
		}
		return closestDistance;
	}

	// Evaluation of Board(true-false) for knowing if we discard cards
	public Boolean evaluateBoard(Player currPlayer, City target) {
		// Initialize variables
		int i = 0;
		int rounds = pandemicBoard.getRound();
		double evaluation = .0;
		double cubesWeight = 0.2, outbreakWeight = 0.4, cardsWeight = 0.3, cureWeight = 0.1;
		int outbreakValue, cureValue, cardsValue, cubesValue = 0;
		String color = target.getColour();
		// Process the evaluation
		// ---------------------------------------------------------------------------------
		// Process rounds
		if (rounds == 1) {
			return false; // too soon to throw a card
		}
		// Process outbreak marker
		if (pandemicBoard.getOutbreakCount() < 2) {
			outbreakValue = 0; // NO THREAT
		} else if (pandemicBoard.getOutbreakCount() < 4) {
			outbreakValue = pandemicBoard.getOutbreakCount() - 1; // LOW THREAT
		} else if (pandemicBoard.getOutbreakCount() < 6) {
			outbreakValue = pandemicBoard.getOutbreakCount(); // MEDIUM THREAT
		} else {
			outbreakValue = 8; // DANGER
		}

		// Process Cubes
		int numberOfCubes = 3;
		int sizeOfCluster = target.getNeighbors().size() + 1;

		for (i = 0; i < target.getNeighbors().size(); i++) {
			numberOfCubes += target.getNeighbors().get(i).getMaxCube();
		}

		if (numberOfCubes / sizeOfCluster <= 1.0) {
			cubesValue = 2;
		} else if (numberOfCubes <= 2.0) {
			cubesValue = 5;
		} else { // <= 3
			cubesValue = 8;
		}

		// Process Cards from Player Discard Pile
		if (pandemicBoard.playerDiscardPile.size() < 12) {
			cardsValue = 1;
		} else if (pandemicBoard.playerDiscardPile.size() < 24) {
			cardsValue = 3;
		} else if (pandemicBoard.playerDiscardPile.size() < 36) {
			cardsValue = 5;
		} else {
			cardsValue = 7;
		}

		// Process Cures
		if (pandemicBoard.getDisease(color).cured) { // Check if cured
			return true; // disease is cures so it doesnt matter.throw cards
		} else {
			if (currPlayer.getPlayerRole() == "SCIENTIST") {
				if (getCountXCards(color) < 2) {
					cureValue = 5;
				} else {
					return false;
				}
			} else {
				if (getCountXCards(color) < 3) {
					cureValue = 5;
				} else {
					cureValue = 10;
				}
			}
		}

		// Total Evaluation
		evaluation = outbreakValue * outbreakWeight + cardsWeight * cardsValue + cubesValue * cubesWeight
				- cureValue * cureWeight;

		if (evaluation < 3.6) { // through experimental
			return true;
		} else {
			return false;
		}
	}

	// Chain Reaction Cities
	public ArrayList<City> chainReactionCities() {
		// Initialize variables
		int i, j = 0;
		ArrayList<City> CitiesWith3Cubes = pandemicBoard.get3CubeCities();
		ArrayList<City> chainReactionCities = new ArrayList<City>();
		City checkCity = new City();

		// Find For all the 3 cubes cities those who are at immediate risk of chain
		// reaction
		for (i = 0; i < CitiesWith3Cubes.size(); i++) {
			checkCity = CitiesWith3Cubes.get(i);
			for (j = 0; j < checkCity.getNeighbors().size(); j++) {
				if (CitiesWith3Cubes.contains(checkCity.getNeighbors().get(j))) { // check neighbor
					if (!chainReactionCities.contains(checkCity))// add check City
						chainReactionCities.add(checkCity);
					if (!chainReactionCities.contains(checkCity.getNeighbors().get(j))) // add Check neighbor
						chainReactionCities.add(checkCity.getNeighbors().get(j));
				}
			}
		}

		return chainReactionCities;
	}

	// BFS to find closest distance
	public int calcDistanceBFS(City startCity, City target) {
		City checkCity = new City();
		Queue<City> myCityList = new LinkedList<City>();
		Queue<City> visitedList = new LinkedList<City>();
		Queue<Integer> distanceList = new LinkedList<Integer>();
		int checkDistance = 0;

		myCityList.add(startCity);
		distanceList.add(0);
		while (!myCityList.isEmpty()) {

			// REMOVE FIRST ELEMENT FROM QUEUE
			checkCity = myCityList.remove();
			visitedList.add(checkCity); // Add to visited List

			checkDistance = distanceList.remove();
			// CHECK IF City IS GOAL TARGET
			if (checkCity.equals(target)) {
				break;
			} else {
				checkDistance++;
				for (int i = 0; i < checkCity.getNeighbors().size(); i++) {
					if (!visitedList.contains(checkCity.getNeighbors().get(i))) {
						myCityList.add(checkCity.getNeighbors().get(i));
						distanceList.add(checkDistance);
					}
				}
			}
		}
		return checkDistance;
	}

	// Find Heuristic Distances for All Cities from Current Location
	public ArrayList<Integer> citiesHeuristicDistances(City location, City target) {
		ArrayList<Integer> cityDistances = new ArrayList<Integer>();
		int distanceToResearch = getDistanceResearch(); // Distance from our location
		int distanceTargetsResearch, travelWithRes, travelOnFoot = 0;
		// Search shortest routes for All Cities
		for (int i = 0; i < pandemicBoard.cities.size(); i++) {
			// Check if we try calculate the city we are in
			if (pandemicBoard.cities.get(i) == location) {
				cityDistances.add(i, 0);
				travelWithResearch.add(i, false);
			} else {
				// calculate distances
				distanceTargetsResearch = getDistanceResearchFromDestination(pandemicBoard.cities.get(i));
				travelWithRes = distanceTargetsResearch + distanceToResearch + 1; // Distance to go to nearest research
																					// stations + 1 for the action

				travelOnFoot = calcDistanceBFS(pandemicBoard.cities.get(i), location);

				if (travelOnFoot >= travelWithRes) { // Check if it is worth to travel on foot or by Research station
					cityDistances.add(i, travelWithRes);
					travelWithResearch.add(i, true);
				} else {
					cityDistances.add(i, travelOnFoot);
					travelWithResearch.add(i, false);
				}
			}
		}
		return cityDistances;
	}

	// Find nearest research from location
	public City nearestResearch(City location) {
		getDistances(Variables.GET_CITIES_WITH_RESEARCH_STATION());
		// System.out.println("Calculating destination");
		City toDriveTo = calculateDestinationWithParameter(location);
		return toDriveTo;
	}

	// Calculate distance from location to target
	public Integer calcDistanceToTarget(City location, City target) {
		ArrayList<Integer> distanceToCities = citiesHeuristicDistances(location, target);
		// Search city
		int distance = 0;
		for (int i = 0; i < pandemicBoard.cities.size(); i++) {
			if (pandemicBoard.cities.get(i).equals(target)) {
				distance = distanceToCities.get(i);
				break;
			}
		}
		return distance;
	}

	// Calculate way to TravcheckWayel
	public Boolean checkTravelWithRes(City target) {
		ArrayList<Boolean> wayToTravel = travelWithResearch;
		// Search city
		boolean checkWay = false;
		for (int i = 0; i < pandemicBoard.cities.size(); i++) {
			if (pandemicBoard.cities.get(i) == target) {
				if (wayToTravel.get(i) == true) {
					checkWay = true;
				}
			}
		}
		return checkWay;
	}

	// Find next destination point between location city and target city
	public City findNextLocation(City location, City target) {
		int index = -1;
		int i, distanceLoc, distanceNeighbor = 0;
		City nearestCity = new City();

		// Find target's index
		for (i = 0; i < pandemicBoard.cities.size(); i++) {
			if (target == pandemicBoard.cities.get(i)) {
				index = i;
			}
		}
		// Find city with minimum distance to target from location
		distanceLoc = citiesHeuristicDistances(location, target).get(index);
		System.out.println("DISTANCE FINAL TO TARGET: " + distanceLoc);
		nearestCity = location.getNeighbors().get(0);
		for (i = 1; i < location.getNeighbors().size(); i++) {
			distanceNeighbor = citiesHeuristicDistances(location.getNeighbors().get(i), target).get(index);
			// Find nearest City
			if (distanceNeighbor < distanceLoc) {
				nearestCity = location.getNeighbors().get(i);
				break;
			}
		}
		return nearestCity;
	}

	public String evaluateSuggestions(int action, int playerIndex) { // Evaluate Suggestions,Profile players and return
																		// the final action
		int[][] profileTable = new int[4][4];
		int[] evaluationTable = new int[4];
		int[] diffTable = new int[4];
		int[] currentEvaluation = new int[4];
		int i, j = 0;
		String tempAction, finalAction = "";
		String[] takeAction = {};
		City targetCity = new City();

		// Initialize tables
		for (i = 0; i < 4; i++) {
			evaluationTable[i] = 0;
			diffTable[i] = 0;
			currentEvaluation[i] = 0;
			for (j = 0; j < 4; j++) {
				profileTable[i][j] = 0;
			}
		}

		// Iterate through all suggestions
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				// Same player
				if (j == playerIndex && action != i) { // evaluation[playerIndex] = currentEvaluation
					continue;
				}

				takeAction = playerSuggestions[i][j].split(":"); // Split in delimiter ":"
				tempAction = takeAction[0]; // final action
				targetCity = stringToCity(takeAction[1]); // City to take the action

				// evaluate actions
				if (i < action) {
					if (tempAction.equals("driveCity")) {
						evaluationTable[j] += 1;
						diffTable[j] += 1;
					} else if (tempAction.equals("treatDisease")) {
						if (pandemicBoard.get3CubeCities().contains(targetCity)) {
							evaluationTable[j] += 4; // 3 cube city
							diffTable[j] += 4;
						} else {
							evaluationTable[j] += 1;
							diffTable[j] += 1;
						}
					} else if (tempAction.equals("discoverCure")) {
						evaluationTable[j] += 6;
						diffTable[j] += 6;
					} else if (tempAction.equals("shuttleFlight")) {
						evaluationTable[j] += 2;
						diffTable[j] += 2;
					} else if (tempAction.equals("charterFlight")) {
						evaluationTable[j] += 3;
						diffTable[j] += 3;
					} else if (tempAction.equals("directFlight")) {
						evaluationTable[j] += 2;
						diffTable[j] += 2;
					} else if (tempAction.equals("buildResearchStation")) {
						evaluationTable[j] += 3;
						diffTable[j] += 3;
					} else {
						evaluationTable[j] += 0; // wrong input
						diffTable[j] += 0;
					}
				} else if (i == action) {
					if (tempAction.equals("driveCity")) {
						evaluationTable[j] += 1;
						currentEvaluation[j] = 1;
					} else if (tempAction.equals("treatDisease")) {
						if (pandemicBoard.get3CubeCities().contains(targetCity)) {
							evaluationTable[j] += 4; // 3 cube city
							currentEvaluation[j] = 4;
						} else {
							evaluationTable[j] += 1;
							currentEvaluation[j] = 1;
						}
					} else if (tempAction.equals("discoverCure")) {
						evaluationTable[j] += 6;
						currentEvaluation[j] = 6;
					} else if (tempAction.equals("shuttleFlight")) {
						evaluationTable[j] += 2;
						currentEvaluation[j] = 2;
					} else if (tempAction.equals("charterFlight")) {
						evaluationTable[j] += 3;
						currentEvaluation[j] = 3;
					} else if (tempAction.equals("directFlight")) {
						evaluationTable[j] += 2;
						currentEvaluation[j] = 2;
					} else if (tempAction.equals("buildResearchStation")) {
						evaluationTable[j] += 3;
						currentEvaluation[j] = 3;
					} else {
						evaluationTable[j] += 0; // wrong input
						currentEvaluation[j] = 0;
					}
				} else {
					if (tempAction.equals("driveCity")) {
						evaluationTable[j] += 1;
					} else if (tempAction.equals("treatDisease")) {
						if (pandemicBoard.get3CubeCities().contains(targetCity)) {
							evaluationTable[j] += 4; // 3 cube city
						} else {
							evaluationTable[j] += 1;
						}
					} else if (tempAction.equals("discoverCure")) {
						evaluationTable[j] += 6;
					} else if (tempAction.equals("shuttleFlight")) {
						evaluationTable[j] += 2;
					} else if (tempAction.equals("charterFlight")) {
						evaluationTable[j] += 3;
					} else if (tempAction.equals("directFlight")) {
						evaluationTable[j] += 2;
					} else if (tempAction.equals("buildResearchStation")) {
						evaluationTable[j] += 3;
					} else {
						evaluationTable[j] += 0; // wrong input
					}
				}
			}
		}

		// Find the difference(except our index) and max Diff index
		int maxIndex = -1;
		int maxValue = -1;
		for (i = 0; i < 4; i++) {
			if (i != playerIndex)
				diffTable[i] = evaluationTable[i] - diffTable[i];
			if (diffTable[i] > maxValue) {
				maxValue = diffTable[i];
				maxIndex = i;
			} else if (diffTable[i] == maxValue) { // Then profile table will tell us what to do
				if (profileTable[playerIndex][i] >= (profileTable[playerIndex][maxIndex])) {
					maxValue = diffTable[i];
					maxIndex = i;
				}
			}
		}

		// Do our final move
		if (currentEvaluation[playerIndex] >= currentEvaluation[maxIndex]) { // Our value greater than the others
			// DO OUR MOVE
			finalAction = playerSuggestions[action][playerIndex];
			previousMoveIndex = playerIndex;
		} else {
			if (previousMoveIndex > 0) {// Check if move is feasible for maxIndex player
				if (checkLegalMove(playerSuggestions[action][maxIndex])) {
					// Do MaxIndex player move
					finalAction = playerSuggestions[action][maxIndex];
					previousMoveIndex = maxIndex;
				} else {// Not feasible follow the player you followed previous action
					finalAction = playerSuggestions[action][previousMoveIndex];
					// Previous index remains the same
				}
			} else {
				finalAction = playerSuggestions[action][playerIndex];
			}
		}

		// Reset index and make profiling
		if (action == 3) {
			previousMoveIndex = -1;

			// Profiling
			for (i = 0; i < 4; i++) {
				if (i == playerIndex) {
					continue;
				} else { // Profile table will be the evaluation of our player minus the total evaluation
							// of each Player separately
					profileTable[playerIndex][i] = evaluationTable[i] - evaluationTable[playerIndex];
				}
			}
		}

		// Return the final action
		return finalAction;
	}

	// Reset some variables for next action/round
	public void resetVariables() {
		travelWithResearch.clear();
	}

	public void makeDecision(ArrayList<City> friend_hand, Player currPlayer, Integer playerIndex) {

		// Variables
		currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = "rollDice:null";
		ArrayList<City> CitiesWith3Cubes = pandemicBoard.get3CubeCities();
		ArrayList<City> CitiesWith2Cubes = pandemicBoard.get2CubeCities();
		ArrayList<City> CitiesWith1Cube = pandemicBoard.get1CubeCities();
		ArrayList<City> infectPile = pandemicBoard.getInfectPile();
		ArrayList<City> infectPileBeforeEpidemic = pandemicBoard.getInfectDiscardPileBeforeEpidemic();
		ArrayList<City> chainReactionCities = chainReactionCities(); // Chain Reaction Cities
		City locationCity = currPlayer.getPlayerPiece().getLocation();
		System.out.println("CURR PLAYER LOC: " + locationCity);
		String Role = currPlayer.getPlayerRole();
		String action = "";
		City nextLocation = new City();
		City target = new City();
		int distance = 0;
		int tempDist = 1000;
		City tempCt = new City();
		ArrayList<City> highPriorityCities = new ArrayList<City>();
		ArrayList<City> mediumPriorityCities = new ArrayList<City>();
		ArrayList<City> lowPriorityCities = new ArrayList<City>();
		int distance_sugg = -1;

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				System.out.println("SUGGESTION action: " + i + " player: " + j + " Suggestion: "
						+ currPlayer.playerSuggestions[i][j]);
			}
		}

		// Find priorities in Cities with 3 cubes
		for (int i = 0; i < CitiesWith3Cubes.size(); i++) {
			// For Pile before epidemic
			for (int j = 0; j < infectPileBeforeEpidemic.size(); j++) {
				target = infectPileBeforeEpidemic.get(j);
				if (CitiesWith3Cubes.contains(target)) {
					highPriorityCities.add(target);
				}
			}
			// For infect Pile
			for (int j = 0; j < infectPile.size(); j++) {
				target = infectPile.get(j);
				if (CitiesWith3Cubes.contains(target) && !highPriorityCities.contains(target)) {
					mediumPriorityCities.add(target);
				}
			}
			// Probably in Discard Pile
			if (!highPriorityCities.contains(CitiesWith3Cubes.get(i))
					&& !mediumPriorityCities.contains(CitiesWith3Cubes.get(i))) {
				lowPriorityCities.add(CitiesWith3Cubes.get(i));
			}
		}

		if (currPlayer.getPlayerRole() == playerRole) { // Final action of player
			if (playerRole == "MEDIC") {
				// Check Cure
				if (checkCureWorthIt(currPlayer)) {
					if (currPlayer.getDistanceResearch() == 0) {
						action = "discoverCure" + ":" + locationCity.getName(); // Location research station and we can
																				// discover cure
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					} else { // go to nearest research to discover cure
						nextLocation = findNextLocation(locationCity, nearestResearch(locationCity));
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					}
					System.out.println(getPlayerName() + " wants to move as follows: "
							+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
				} else {
					// Check if our location is a city with 3 cubes.
					if (CitiesWith3Cubes.contains(locationCity)) {
						action = "treatDisease" + ":" + locationCity.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
					} else { // Check all the immediate threat cities
						if (!chainReactionCities.isEmpty()) {
							for (int i = 0; i < chainReactionCities.size(); i++) {
								target = chainReactionCities.get(i);
								distance = calcDistanceToTarget(locationCity, target);
								if (distance <= 3) {
									if (checkTravelWithRes(target)) {
										if (currPlayer.getDistanceResearch() == 0) { // Check if our current location
																						// has a
																						// research
											action = "shuttleFlight" + ":" + nearestResearch(target).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is 1
											action = "driveCity" + ":" + nearestResearch(locationCity).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else { // distance 2 or 3
											nextLocation = findNextLocation(locationCity,
													nearestResearch(locationCity));
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else { // Drive on foot
										nextLocation = findNextLocation(locationCity, target);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										break;
									}
								} else {
									System.out.println("Location City above if:" + locationCity);

									if (friend_hand.contains(locationCity)) {
										action = "charterFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else if (friend_hand.contains(target)) {
										action = "directFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else {
										target = null;
										continue;
									}
								}
							}
							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						} else {

							// Make suggestion according to priorities
							if (!highPriorityCities.isEmpty()) {
								for (int i = 0; i < highPriorityCities.size(); i++) {
									target = highPriorityCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);

									// Check if distance is less than 3 so try move immediately
									if (distance <= 3) {
										if (checkTravelWithRes(target)) {
											if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																							// location
																							// has a research
												action = "shuttleFlight" + ":" + nearestResearch(target).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is
																								// 1
												action = "driveCity" + ":" + nearestResearch(locationCity).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else { // distance 2 or 3
												nextLocation = findNextLocation(locationCity,
														nearestResearch(locationCity));
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else { // Drive on foot
											nextLocation = findNextLocation(locationCity, target);
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else {
										if (friend_hand.contains(locationCity) && evaluateBoard(currPlayer, target)) {
											action = "charterFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else if (friend_hand.contains(target) && evaluateBoard(currPlayer, target)) {
											action = "directFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else {
											target = null;
											continue;
										}
									}
								}
								// If suggestions is null

								if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
										.equals("rollDice:null")) {

									tempCt = highPriorityCities.get(0);
									tempDist = calcDistanceToTarget(locationCity, tempCt);

									for (int i = 1; i < highPriorityCities.size(); i++) {
										target = highPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										// Check if distance is nearest than temp and i dont have the city you want to
										// go
										if (distance < tempDist && !hand.contains(target)) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}

							} else if (!mediumPriorityCities.isEmpty()) {
								for (int i = 0; i < mediumPriorityCities.size(); i++) {
									target = mediumPriorityCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);

									// Check if distance is less than 3 so try move immediately
									if (distance <= 3) {
										if (checkTravelWithRes(target)) {
											if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																							// location
																							// has a research
												action = "shuttleFlight" + ":" + nearestResearch(target).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is
																								// 1
												action = "driveCity" + ":" + nearestResearch(locationCity).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else { // distance 2 or 3
												nextLocation = findNextLocation(locationCity,
														nearestResearch(locationCity));
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else { // Drive on foot
											nextLocation = findNextLocation(locationCity, target);
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else {
										if (friend_hand.contains(locationCity) && evaluateBoard(currPlayer, target)) {
											action = "charterFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else if (friend_hand.contains(target) && evaluateBoard(currPlayer, target)) {
											action = "directFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else {
											target = null;
											continue;
										}
									}
								}
								// If suggestions is null

								if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
										.equals("rollDice:null")) {
									tempCt = mediumPriorityCities.get(0);
									tempDist = calcDistanceToTarget(locationCity, tempCt);

									for (int i = 1; i < mediumPriorityCities.size(); i++) {
										target = mediumPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										// Check if distance is nearest than temp and i dont have the city you want to
										// go
										if (distance < tempDist && !hand.contains(target)) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}
							} else { // Low priorities
								if (!lowPriorityCities.isEmpty()) { // low Priority with 3 cubes
									for (int i = 0; i < lowPriorityCities.size(); i++) {
										target = lowPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								} else if (!CitiesWith2Cubes.isEmpty()) { // 2 cube cities
									for (int i = 0; i < CitiesWith2Cubes.size(); i++) {
										target = CitiesWith2Cubes.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								} else { // 1 cube
									for (int i = 0; i < CitiesWith1Cube.size(); i++) {
										target = CitiesWith1Cube.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}
							}
							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						}
					}
				}
			} else if (Role == "OPERATIONS_EXPERT") {
				if (checkCureWorthIt(currPlayer)) {
					if (currPlayer.getDistanceResearch() == 0) {
						action = "discoverCure" + ":" + locationCity.getName(); // Location research station and we can
																				// discover
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action; // cure
					} else { // go to nearest research to discover cure
						nextLocation = findNextLocation(locationCity, nearestResearch(locationCity));
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					}
					System.out.println(getPlayerName() + " wants to move as follows: "
							+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
				} else {
					// Builds a research station
					City nearestResearch = nearestResearch(locationCity);
					if (CitiesWith3Cubes.contains(locationCity)) {
						action = "treatDisease" + ":" + locationCity.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					} else {
						for (int i = 0; i < CitiesWith3Cubes.size(); i++) {
							City target_to_build = cityTargetForResearchStation(currPlayer, CitiesWith3Cubes.get(i));

							if (target_to_build == null) {
								continue;
							} else {
								if (currPlayer.getPlayerPiece().getLocation().equals(target_to_build)) {
									action = "buildResearchStation" + ":" + target_to_build.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									break;
								} else {
									action = "driveCity" + ":" + nearestResearch.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									break;
								}

							}

						}
					}
				}
				distance_sugg = 0;
				tempDist = 0;
				tempCt = null;
				if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex].equals("rollDice:null")) {

					if (!highPriorityCities.isEmpty()) {
						tempCt = highPriorityCities.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < highPriorityCities.size(); i++) {
							target = highPriorityCities.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					} else if (!mediumPriorityCities.isEmpty()) {
						tempCt = mediumPriorityCities.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < mediumPriorityCities.size(); i++) {
							target = mediumPriorityCities.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					} else if (!lowPriorityCities.isEmpty()) {
						tempCt = lowPriorityCities.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < lowPriorityCities.size(); i++) {
							target = lowPriorityCities.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					} else if (!CitiesWith2Cubes.isEmpty()) {
						tempCt = CitiesWith2Cubes.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < CitiesWith2Cubes.size(); i++) {
							target = CitiesWith2Cubes.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					} else {
						tempCt = CitiesWith1Cube.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < CitiesWith1Cube.size(); i++) {
							target = CitiesWith1Cube.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					}

				}

			} else if (playerRole == "SCIENTIST") {
				// Check Cure
				if (checkCureWorthIt(currPlayer)) {
					if (currPlayer.getDistanceResearch() == 0) {
						action = "discoverCure" + ":" + locationCity.getName(); // Location research station and we can
																				// discover
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action; // cure
					} else { // go to nearest research to discover cure
						nextLocation = findNextLocation(locationCity, nearestResearch(locationCity));
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					}
					System.out.println(getPlayerName() + " wants to move as follows: "
							+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
				} else {
					// Check if our location is a city with 3 cubes.
					if (CitiesWith3Cubes.contains(locationCity)) {
						action = "treatDisease" + ":" + locationCity.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
					} else { // Check all the immediate threat cities
						if (!chainReactionCities.isEmpty()) {
							for (int i = 0; i < chainReactionCities.size(); i++) {
								target = chainReactionCities.get(i);
								distance = calcDistanceToTarget(locationCity, target);
								if (distance <= 3) {
									if (checkTravelWithRes(target)) {
										if (currPlayer.getDistanceResearch() == 0) { // Check if our current location
																						// has a
																						// research
											action = "shuttleFlight" + ":" + nearestResearch(target).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is 1
											action = "driveCity" + ":" + nearestResearch(locationCity).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else { // distance 2 or 3
											nextLocation = findNextLocation(locationCity,
													nearestResearch(locationCity));
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else { // Drive on foot
										nextLocation = findNextLocation(locationCity, target);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										break;
									}
								} else {
									if (friend_hand.contains(locationCity)) {
										action = "charterFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else if (friend_hand.contains(target)) {
										action = "directFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else {
										target = null;
										continue;
									}
								}
							}
							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						} else {

							if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
									.equals("rollDice:null")) { // Check all
								// the 3
								// cube
								// cities
								target = null;
								// Make suggestion according to priorities
								if (!highPriorityCities.isEmpty()) {
									for (int i = 0; i < highPriorityCities.size(); i++) {
										target = highPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										// Check if distance is less than 3 so try move immediately
										if (distance <= 3) {
											if (checkTravelWithRes(target)) {
												if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																								// location
																								// has a research
													action = "shuttleFlight" + ":" + nearestResearch(target).getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance
																									// is
																									// 1
													action = "driveCity" + ":"
															+ nearestResearch(locationCity).getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												} else { // distance 2 or 3
													nextLocation = findNextLocation(locationCity,
															nearestResearch(locationCity));
													action = "driveCity" + ":" + nextLocation.getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												}
											} else { // Drive on foot
												nextLocation = findNextLocation(locationCity, target);
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else {
											if (friend_hand.contains(locationCity)
													&& evaluateBoard(currPlayer, target)) {
												action = "charterFlight" + ":" + target.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
											} else if (friend_hand.contains(target)
													&& evaluateBoard(currPlayer, target)) {
												action = "directFlight" + ":" + target.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
											} else {
												target = null;
												continue;
											}
										}
									}
									// If suggestions is null

									if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
											.equals("rollDice:null")) {

										tempCt = highPriorityCities.get(0);
										tempDist = calcDistanceToTarget(locationCity, tempCt);

										for (int i = 1; i < highPriorityCities.size(); i++) {
											target = highPriorityCities.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);

											// Check if distance is nearest than temp and i dont have the city you want
											// to
											// go
											if (distance < tempDist && !hand.contains(target)) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action
										nextLocation = findNextLocation(locationCity, tempCt);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									}

								} else if (!mediumPriorityCities.isEmpty()) {
									for (int i = 0; i < mediumPriorityCities.size(); i++) {
										target = mediumPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										// Check if distance is less than 3 so try move immediately
										if (distance <= 3) {
											if (checkTravelWithRes(target)) {
												if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																								// location
																								// has a research
													action = "shuttleFlight" + ":" + nearestResearch(target).getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance
																									// is
																									// 1
													action = "driveCity" + ":"
															+ nearestResearch(locationCity).getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												} else { // distance 2 or 3
													nextLocation = findNextLocation(locationCity,
															nearestResearch(locationCity));
													action = "driveCity" + ":" + nextLocation.getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												}
											} else { // Drive on foot
												nextLocation = findNextLocation(locationCity, target);
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else {
											target = null;
											continue;
										}
									}
									// If suggestions is null

									if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
											.equals("rollDice:null")) {
										tempCt = mediumPriorityCities.get(0);
										tempDist = calcDistanceToTarget(locationCity, tempCt);

										for (int i = 1; i < mediumPriorityCities.size(); i++) {
											target = mediumPriorityCities.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);

											// Check if distance is nearest than temp and i dont have the city you want
											// to
											// go
											if (distance < tempDist && !hand.contains(target)) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action
										nextLocation = findNextLocation(locationCity, tempCt);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									}
								} else { // Low priorities
									if (!lowPriorityCities.isEmpty()) { // low Priority with 3 cubes
										for (int i = 0; i < lowPriorityCities.size(); i++) {
											target = lowPriorityCities.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);
											if (distance < tempDist) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action
										nextLocation = findNextLocation(locationCity, tempCt);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else if (!CitiesWith2Cubes.isEmpty()) { // 2 cube cities
										for (int i = 0; i < CitiesWith2Cubes.size(); i++) {
											target = CitiesWith2Cubes.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);
											if (distance < tempDist) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action
										nextLocation = findNextLocation(locationCity, tempCt);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else { // 1 cube
										for (int i = 0; i < CitiesWith1Cube.size(); i++) {
											target = CitiesWith1Cube.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);
											if (distance < tempDist) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action
										nextLocation = findNextLocation(locationCity, tempCt);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									}
								}
							}
							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						}
					}
				}
			} else { // Quarantine
				// Check Cure

				if (checkCureWorthIt(currPlayer)) {
					if (currPlayer.getDistanceResearch() == 0) {
						action = "discoverCure" + ":" + locationCity.getName(); // Location research station and we can
																				// discover
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action; // cure
					} else { // go to nearest research to discover cure
						nextLocation = findNextLocation(locationCity, nearestResearch(locationCity));
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					}
					System.out.println(getPlayerName() + " wants to move as follows: "
							+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
				} else {
					// Check if our location is a city with 3 cubes.
					if (CitiesWith3Cubes.contains(locationCity)) {
						action = "treatDisease" + ":" + locationCity.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
					} else { // Check all the immediate threat cities
						if (!chainReactionCities.isEmpty()) {
							for (int i = 0; i < chainReactionCities.size(); i++) {
								target = chainReactionCities.get(i);
								distance = calcDistanceToTarget(locationCity, target);
								if (distance <= 3) {
									if (checkTravelWithRes(target)) {
										if (currPlayer.getDistanceResearch() == 0) { // Check if our current location
																						// has a
																						// research
											action = "shuttleFlight" + ":" + nearestResearch(target).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is 1
											action = "driveCity" + ":" + nearestResearch(locationCity).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else { // distance 2 or 3
											nextLocation = findNextLocation(locationCity,
													nearestResearch(locationCity));
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else { // Drive on foot
										nextLocation = findNextLocation(locationCity, target);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										break;
									}
								} else {
									if (friend_hand.contains(locationCity)) {
										action = "charterFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else if (friend_hand.contains(target)) {
										action = "directFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else {
										target = null;
										continue;
									}
								}
							}
							// Iterate through all cities for chain threat
							if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
									.equals("rollDice:null")) {
								tempCt = chainReactionCities.get(0);
								tempDist = calcDistanceToTarget(locationCity, tempCt);

								for (int i = 1; i < chainReactionCities.size(); i++) {
									target = chainReactionCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);

									// Check if distance is nearest than temp and i dont have the city you want to
									// go
									if (distance < tempDist && !hand.contains(target)) {
										tempDist = distance;
										tempCt = target;
									}
								}
								// Take action
								nextLocation = findNextLocation(locationCity, tempCt);
								action = "driveCity" + ":" + nextLocation.getName();
								currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
							}
							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						} else { // Act like Medic
							// Make suggestion according to priorities
							if (!highPriorityCities.isEmpty()) {
								for (int i = 0; i < highPriorityCities.size(); i++) {
									target = highPriorityCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);

									// Check if distance is less than 3 so try move immediately
									if (distance <= 3) {
										if (checkTravelWithRes(target)) {
											if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																							// location
																							// has a research
												action = "shuttleFlight" + ":" + nearestResearch(target).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is
																								// 1
												action = "driveCity" + ":" + nearestResearch(locationCity).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else { // distance 2 or 3
												nextLocation = findNextLocation(locationCity,
														nearestResearch(locationCity));
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else { // Drive on foot
											nextLocation = findNextLocation(locationCity, target);
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else {
										if (friend_hand.contains(locationCity) && evaluateBoard(currPlayer, target)) {
											action = "charterFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else if (friend_hand.contains(target) && evaluateBoard(currPlayer, target)) {
											action = "directFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else {
											target = null;
											continue;
										}
									}
								}
								// If suggestions is null

								if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
										.equals("rollDice:null")) {

									tempCt = highPriorityCities.get(0);
									tempDist = calcDistanceToTarget(locationCity, tempCt);

									for (int i = 1; i < highPriorityCities.size(); i++) {
										target = highPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										// Check if distance is nearest than temp and i dont have the city you want to
										// go
										if (distance < tempDist && !hand.contains(target)) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}

							} else if (!mediumPriorityCities.isEmpty()) {
								for (int i = 0; i < mediumPriorityCities.size(); i++) {
									target = mediumPriorityCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);

									// Check if distance is less than 3 so try move immediately
									if (distance <= 3) {
										if (checkTravelWithRes(target)) {
											if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																							// location
																							// has a research
												action = "shuttleFlight" + ":" + nearestResearch(target).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is
																								// 1
												action = "driveCity" + ":" + nearestResearch(locationCity).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else { // distance 2 or 3
												nextLocation = findNextLocation(locationCity,
														nearestResearch(locationCity));
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else { // Drive on foot
											nextLocation = findNextLocation(locationCity, target);
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else {
										if (friend_hand.contains(locationCity) && evaluateBoard(currPlayer, target)) {
											action = "charterFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else if (friend_hand.contains(target) && evaluateBoard(currPlayer, target)) {
											action = "directFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else {
											target = null;
											continue;
										}
									}
								}
								// If suggestions is null

								if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
										.equals("rollDice:null")) {
									tempCt = mediumPriorityCities.get(0);
									tempDist = calcDistanceToTarget(locationCity, tempCt);

									for (int i = 1; i < mediumPriorityCities.size(); i++) {
										target = mediumPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										// Check if distance is nearest than temp and i dont have the city you want to
										// go
										if (distance < tempDist && !hand.contains(target)) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}
							} else { // Low priorities
								if (!lowPriorityCities.isEmpty()) { // low Priority with 3 cubes
									for (int i = 0; i < lowPriorityCities.size(); i++) {
										target = lowPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								} else if (!CitiesWith2Cubes.isEmpty()) { // 2 cube cities
									for (int i = 0; i < CitiesWith2Cubes.size(); i++) {
										target = CitiesWith2Cubes.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								} else { // 1 cube
									for (int i = 0; i < CitiesWith1Cube.size(); i++) {
										target = CitiesWith1Cube.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}
							}
							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						}
					}
				}
			}

			// Take Evaluate Suggestions-Take Action-Profiling
			action = evaluateSuggestions(4 - getPlayerAction(), playerIndex); // Evaluate the suggestions,make the
																				// profiling and return the final action
			String[] takeFinalAction = action.split(":"); // Split in delimiter ":"
			String finalAction = takeFinalAction[0]; // final action
			City targetCity = stringToCity(takeFinalAction[1]); // City to take the action

			// Take the action
			if (finalAction.equals("driveCity")) {
				driveCity(locationCity, targetCity, currPlayer);
			} else if (finalAction.equals("treatDisease")) {
				treatDisease(targetCity, targetCity.getColour(), currPlayer);
			} else if (finalAction.equals("discoverCure")) {
				checkTryCure(currPlayer);
			} else if (finalAction.equals("shuttleFlight")) {
				shuttleFlight(locationCity, targetCity, currPlayer);
			} else if (finalAction.equals("charterFlight")) {
				charterFlight(locationCity, targetCity, currPlayer);
			} else if (finalAction.equals("directFlight")) {
				directFlight(locationCity, targetCity, currPlayer);
			} else if (finalAction.equals("buildResearchStation")) {
				buildResearchStation(currPlayer);
			} else {
				System.out.println("ROLLED DICE");
				rollDice(currPlayer); // Random move due to fail of the program
			}
			System.out.println("FINAL ACTION: " + finalAction);
		} else { // Others make suggestions
			if (Role == "MEDIC") {
				// Check Cure

				if (checkCureWorthIt(currPlayer)) {

					if (currPlayer.getDistanceResearch() == 0) {

						action = "discoverCure" + ":" + locationCity.getName(); // Location research station and we can
																				// discover
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action; // cure
					} else { // go to nearest research to discover cure
						nextLocation = findNextLocation(locationCity, nearestResearch(locationCity));
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					}
					System.out.println(getPlayerName() + " wants to move as follows: "
							+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
				} else {
					// Check if our location is a city with 3 cubes.
					if (CitiesWith3Cubes.contains(locationCity)) {
						action = "treatDisease" + ":" + locationCity.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
					} else { // Check all the immediate threat cities
						if (!chainReactionCities.isEmpty()) {
							for (int i = 0; i < chainReactionCities.size(); i++) {
								target = chainReactionCities.get(i);
								distance = calcDistanceToTarget(locationCity, target);
								if (distance <= 3) {
									if (checkTravelWithRes(target)) {
										if (currPlayer.getDistanceResearch() == 0) { // Check if our current location
																						// has a
																						// research
											action = "shuttleFlight" + ":" + nearestResearch(target).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is 1
											action = "driveCity" + ":" + nearestResearch(locationCity).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else { // distance 2 or 3
											nextLocation = findNextLocation(locationCity,
													nearestResearch(locationCity));
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else { // Drive on foot
										nextLocation = findNextLocation(locationCity, target);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										break;
									}
								} else {
									if (friend_hand.contains(locationCity)) {
										action = "charterFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else if (friend_hand.contains(target)) {
										action = "directFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else {
										target = null;
										continue;
									}
								}
							}
							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						} else {

							// Make suggestion according to priorities
							if (!highPriorityCities.isEmpty()) {
								for (int i = 0; i < highPriorityCities.size(); i++) {
									target = highPriorityCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);

									// Check if distance is less than 3 so try move immediately
									if (distance <= 3) {
										if (checkTravelWithRes(target)) {
											if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																							// location
																							// has a research
												action = "shuttleFlight" + ":" + nearestResearch(target).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is
																								// 1
												action = "driveCity" + ":" + nearestResearch(locationCity).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else { // distance 2 or 3
												nextLocation = findNextLocation(locationCity,
														nearestResearch(locationCity));
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else { // Drive on foot
											nextLocation = findNextLocation(locationCity, target);
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else {
										if (friend_hand.contains(locationCity) && evaluateBoard(currPlayer, target)) {
											action = "charterFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else if (friend_hand.contains(target) && evaluateBoard(currPlayer, target)) {
											action = "directFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else {
											target = null;
											continue;
										}
									}
								}
								// If suggestions is null

								if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
										.equals("rollDice:null")) {

									tempCt = highPriorityCities.get(0);
									tempDist = calcDistanceToTarget(locationCity, tempCt);

									for (int i = 1; i < highPriorityCities.size(); i++) {
										target = highPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
										// Check if distance is nearest than temp and i dont have the city you want to
										// go
										if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}

							} else if (!mediumPriorityCities.isEmpty()) {
								for (int i = 0; i < mediumPriorityCities.size(); i++) {
									target = mediumPriorityCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);
									distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);

									// Check if distance is less than 3 so try move immediately
									if (distance <= 3) {
										if (checkTravelWithRes(target)) {
											if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																							// location
																							// has a research
												action = "shuttleFlight" + ":" + nearestResearch(target).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is
																								// 1
												action = "driveCity" + ":" + nearestResearch(locationCity).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else { // distance 2 or 3
												nextLocation = findNextLocation(locationCity,
														nearestResearch(locationCity));
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else { // Drive on foot
											nextLocation = findNextLocation(locationCity, target);
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else {
										if (distance_sugg > 3) { // distance from suggestion player to the target
											if (friend_hand.contains(locationCity)
													&& evaluateBoard(currPlayer, target)) {
												action = "charterFlight" + ":" + target.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
											} else if (friend_hand.contains(target)
													&& evaluateBoard(currPlayer, target)) {
												action = "directFlight" + ":" + target.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
											} else {
												target = null;
												continue;
											}
										} else { // suggestion player is closer to the target
											target = null;
											continue;
										}
									}
								}
								// If suggestions is null

								if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
										.equals("rollDice:null")) {
									tempCt = mediumPriorityCities.get(0);
									tempDist = calcDistanceToTarget(locationCity, tempCt);

									for (int i = 1; i < mediumPriorityCities.size(); i++) {
										target = mediumPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										// Check if distance is nearest than temp and i dont have the city you want to
										// go
										if (distance < tempDist && !hand.contains(target)) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}
							} else { // Low priorities
								if (!lowPriorityCities.isEmpty()) { // low Priority with 3 cubes
									for (int i = 0; i < lowPriorityCities.size(); i++) {
										target = lowPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);

									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								} else if (!CitiesWith2Cubes.isEmpty()) { // 2 cube cities
									for (int i = 0; i < CitiesWith2Cubes.size(); i++) {
										target = CitiesWith2Cubes.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								} else { // 1 cube
									for (int i = 0; i < CitiesWith1Cube.size(); i++) {
										target = CitiesWith1Cube.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}
							}

							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						}
					}
				}
			} else if (Role == "OPERATIONS_EXPERT") {
				if (checkCureWorthIt(currPlayer)) {
					if (currPlayer.getDistanceResearch() == 0) {
						action = "discoverCure" + ":" + locationCity.getName(); // Location research station and we can
																				// discover
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action; // cure
					} else { // go to nearest research to discover cure
						nextLocation = findNextLocation(locationCity, nearestResearch(locationCity));
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					}
					System.out.println(getPlayerName() + " wants to move as follows: "
							+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
				} else {
					// Builds a research station
					City nearestResearch = nearestResearch(locationCity);
					if (CitiesWith3Cubes.contains(locationCity)) {
						action = "treatDisease" + ":" + locationCity.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					} else {
						for (int i = 0; i < CitiesWith3Cubes.size(); i++) {
							if (this.hand.contains(CitiesWith3Cubes.get(i))
									&& (!chainReactionCities.contains(CitiesWith3Cubes.get(i)))) {
								continue;
							} else {
								City target_to_build = cityTargetForResearchStation(currPlayer,
										CitiesWith3Cubes.get(i));
								System.out.println("TARGET FOR RES: " + target_to_build);
								if (target_to_build == null) {
									continue;
								} else {
									if (currPlayer.getPlayerPiece().getLocation().equals(target_to_build)) {
										action = "buildResearchStation" + ":" + target_to_build.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										break;
									} else {
										action = "driveCity" + ":" + nearestResearch.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										break;
									}

								}
							}
						}
					}
				}
				distance_sugg = 0;
				tempDist = 0;
				tempCt = null;
				if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex].equals("rollDice:null")) {

					if (!highPriorityCities.isEmpty()) {
						tempCt = highPriorityCities.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < highPriorityCities.size(); i++) {
							target = highPriorityCities.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					} else if (!mediumPriorityCities.isEmpty()) {
						tempCt = mediumPriorityCities.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < mediumPriorityCities.size(); i++) {
							target = mediumPriorityCities.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					} else if (!lowPriorityCities.isEmpty()) {
						tempCt = lowPriorityCities.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < lowPriorityCities.size(); i++) {
							target = lowPriorityCities.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					} else if (!CitiesWith2Cubes.isEmpty()) {
						tempCt = CitiesWith2Cubes.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < CitiesWith2Cubes.size(); i++) {
							target = CitiesWith2Cubes.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					} else {
						tempCt = CitiesWith1Cube.get(0);
						tempDist = calcDistanceToTarget(locationCity, tempCt);

						for (int i = 1; i < CitiesWith1Cube.size(); i++) {
							target = CitiesWith1Cube.get(i);
							// Get distance
							distance = calcDistanceToTarget(locationCity, target);
							distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
							// Check if distance is nearest than temp and i dont have the city you want to
							// go
							if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
								tempDist = distance;
								tempCt = target;
							}
						}
						// Take action
						nextLocation = findNextLocation(locationCity, tempCt);
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);

					}

				}

			} else if (Role == "SCIENTIST") {
				// Check Cure

				if (checkCureWorthIt(currPlayer)) {

					if (currPlayer.getDistanceResearch() == 0) {
						action = "discoverCure" + ":" + locationCity.getName(); // Location research station and we can
																				// discover
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action; // cure
					} else { // go to nearest research to discover cure
						nextLocation = findNextLocation(locationCity, nearestResearch(locationCity));
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					}
					System.out.println(getPlayerName() + " wants to move as follows: "
							+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
				} else {

					// Check if our location is a city with 3 cubes.
					if (CitiesWith3Cubes.contains(locationCity)) {

						action = "treatDisease" + ":" + locationCity.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
					} else { // Check all the immediate threat cities

						if (!chainReactionCities.isEmpty()) {
							for (int i = 0; i < chainReactionCities.size(); i++) {
								target = chainReactionCities.get(i);
								distance = calcDistanceToTarget(locationCity, target);
								if (distance <= 3) {
									if (checkTravelWithRes(target)) {
										if (currPlayer.getDistanceResearch() == 0) { // Check if our current location
																						// has a
																						// research
											action = "shuttleFlight" + ":" + nearestResearch(target).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is 1
											action = "driveCity" + ":" + nearestResearch(locationCity).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else { // distance 2 or 3
											nextLocation = findNextLocation(locationCity,
													nearestResearch(locationCity));
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else { // Drive on foot
										nextLocation = findNextLocation(locationCity, target);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										break;
									}
								} else {
									if (friend_hand.contains(locationCity)) {
										action = "charterFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else if (friend_hand.contains(target)) {
										action = "directFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else {
										target = null;
										continue;
									}
								}
							}
							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						} else {

							if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
									.equals("rollDice:null")) { // Check all
								// the 3
								// cube
								// cities
								target = null;
								// Make suggestion according to priorities
								if (!highPriorityCities.isEmpty()) {

									for (int i = 0; i < highPriorityCities.size(); i++) {
										target = highPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										// Check if distance is less than 3 so try move immediately
										if (distance <= 3) {
											if (checkTravelWithRes(target)) {
												if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																								// location
																								// has a research
													action = "shuttleFlight" + ":" + nearestResearch(target).getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance
																									// is
																									// 1
													action = "driveCity" + ":"
															+ nearestResearch(locationCity).getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												} else { // distance 2 or 3
													nextLocation = findNextLocation(locationCity,
															nearestResearch(locationCity));
													action = "driveCity" + ":" + nextLocation.getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												}
											} else { // Drive on foot
												nextLocation = findNextLocation(locationCity, target);
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else {
											if (friend_hand.contains(locationCity)
													&& evaluateBoard(currPlayer, target)) {
												action = "charterFlight" + ":" + target.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
											} else if (friend_hand.contains(target)
													&& evaluateBoard(currPlayer, target)) {
												action = "directFlight" + ":" + target.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
											} else {
												target = null;
												continue;
											}
										}
									}
									// If suggestions is null

									if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
											.equals("rollDice:null")) {

										tempCt = highPriorityCities.get(0);
										tempDist = calcDistanceToTarget(locationCity, tempCt);

										for (int i = 1; i < highPriorityCities.size(); i++) {
											target = highPriorityCities.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);
											distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(),
													target);
											// Check if distance is nearest than temp and i dont have the city you want
											// to
											// go
											if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action
										nextLocation = findNextLocation(locationCity, tempCt);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									}

								} else if (!mediumPriorityCities.isEmpty()) {

									for (int i = 0; i < mediumPriorityCities.size(); i++) {
										target = mediumPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);

										// Check if distance is less than 3 so try move immediately
										if (distance <= 3) {
											if (checkTravelWithRes(target)) {
												if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																								// location
																								// has a research
													action = "shuttleFlight" + ":" + nearestResearch(target).getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance
																									// is
																									// 1
													action = "driveCity" + ":"
															+ nearestResearch(locationCity).getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												} else { // distance 2 or 3
													nextLocation = findNextLocation(locationCity,
															nearestResearch(locationCity));
													action = "driveCity" + ":" + nextLocation.getName();
													currPlayer.playerSuggestions[4
															- getPlayerAction()][playerIndex] = action;
													break;
												}
											} else { // Drive on foot
												nextLocation = findNextLocation(locationCity, target);
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else {

											target = null;
											continue;

										}
									}
									// If suggestions is null

									if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
											.equals("rollDice:null")) {
										tempCt = mediumPriorityCities.get(0);
										tempDist = calcDistanceToTarget(locationCity, tempCt);

										for (int i = 1; i < mediumPriorityCities.size(); i++) {
											target = mediumPriorityCities.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);

											// Check if distance is nearest than temp and i dont have the city you want
											// to
											// go
											if (distance < tempDist && !hand.contains(target)) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action
										nextLocation = findNextLocation(locationCity, tempCt);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									}
								} else { // Low priorities
									if (!lowPriorityCities.isEmpty()) { // low Priority with 3 cubes

										for (int i = 0; i < lowPriorityCities.size(); i++) {
											target = lowPriorityCities.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);
											if (distance < tempDist) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action

										nextLocation = findNextLocation(locationCity, tempCt);

										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else if (!CitiesWith2Cubes.isEmpty()) { // 2 cube cities

										for (int i = 0; i < CitiesWith2Cubes.size(); i++) {
											target = CitiesWith2Cubes.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);
											if (distance < tempDist) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action
										nextLocation = findNextLocation(locationCity, tempCt);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else { // 1 cube

										for (int i = 0; i < CitiesWith1Cube.size(); i++) {
											target = CitiesWith1Cube.get(i);
											// Get distance
											distance = calcDistanceToTarget(locationCity, target);
											if (distance < tempDist) {
												tempDist = distance;
												tempCt = target;
											}
										}
										// Take action
										nextLocation = findNextLocation(locationCity, tempCt);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									}
								}
							}

							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						}
					}
				}
			} else { // Quarantine
				// Check Cure

				if (checkCureWorthIt(currPlayer)) {
					if (currPlayer.getDistanceResearch() == 0) {
						action = "discoverCure" + ":" + locationCity.getName(); // Location research station and we can
																				// discover
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action; // cure
					} else { // go to nearest research to discover cure
						System.out.println("GO to nearest Research station");
						nextLocation = findNextLocation(locationCity, nearestResearch(locationCity));
						action = "driveCity" + ":" + nextLocation.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
					}
					System.out.println(getPlayerName() + " wants to move as follows: "
							+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
				} else {
					// Check if our location is a city with 3 cubes.
					if (CitiesWith3Cubes.contains(locationCity)) {
						action = "treatDisease" + ":" + locationCity.getName();
						currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
						System.out.println(getPlayerName() + " wants to move as follows: "
								+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
					} else { // Check all the immediate threat cities
						if (!chainReactionCities.isEmpty()) {
							for (int i = 0; i < chainReactionCities.size(); i++) {
								target = chainReactionCities.get(i);
								distance = calcDistanceToTarget(locationCity, target);
								if (distance <= 3) {
									if (checkTravelWithRes(target)) {
										if (currPlayer.getDistanceResearch() == 0) { // Check if our current location
																						// has a
																						// research
											action = "shuttleFlight" + ":" + nearestResearch(target).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is 1
											action = "driveCity" + ":" + nearestResearch(locationCity).getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										} else { // distance 2 or 3
											nextLocation = findNextLocation(locationCity,
													nearestResearch(locationCity));
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else { // Drive on foot
										nextLocation = findNextLocation(locationCity, target);
										action = "driveCity" + ":" + nextLocation.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										break;
									}
								} else {
									if (friend_hand.contains(locationCity)) {
										action = "charterFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else if (friend_hand.contains(target)) {
										action = "directFlight" + ":" + target.getName();
										currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
									} else {
										target = null;
										continue;
									}
								}
							}
							// Iterate through all cities for chain threat
							if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
									.equals("rollDice:null")) {
								tempCt = chainReactionCities.get(0);
								tempDist = calcDistanceToTarget(locationCity, tempCt);

								for (int i = 1; i < chainReactionCities.size(); i++) {
									target = chainReactionCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);

									// Check if distance is nearest than temp and i dont have the city you want to
									// go
									if (distance < tempDist && !hand.contains(target)) {
										tempDist = distance;
										tempCt = target;
									}
								}
								// Take action
								nextLocation = findNextLocation(locationCity, tempCt);
								action = "driveCity" + ":" + nextLocation.getName();
								currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
							}
							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						} else { // Act like Medic
							// Make suggestion according to priorities
							if (!highPriorityCities.isEmpty()) {
								for (int i = 0; i < highPriorityCities.size(); i++) {
									target = highPriorityCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);

									// Check if distance is less than 3 so try move immediately
									if (distance <= 3) {
										if (checkTravelWithRes(target)) {
											if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																							// location
																							// has a research
												action = "shuttleFlight" + ":" + nearestResearch(target).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is
																								// 1
												action = "driveCity" + ":" + nearestResearch(locationCity).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else { // distance 2 or 3
												nextLocation = findNextLocation(locationCity,
														nearestResearch(locationCity));
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else { // Drive on foot
											nextLocation = findNextLocation(locationCity, target);
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else {
										if (friend_hand.contains(locationCity) && evaluateBoard(currPlayer, target)) {
											action = "charterFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else if (friend_hand.contains(target) && evaluateBoard(currPlayer, target)) {
											action = "directFlight" + ":" + target.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
										} else {
											target = null;
											continue;
										}
									}
								}
								// If suggestions is null

								if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
										.equals("rollDice:null")) {

									tempCt = highPriorityCities.get(0);
									tempDist = calcDistanceToTarget(locationCity, tempCt);

									for (int i = 1; i < highPriorityCities.size(); i++) {
										target = highPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);
										// Check if distance is nearest than temp and i dont have the city you want to
										// go
										if (distance < tempDist && !hand.contains(target) && distance_sugg > 3) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;

								}

							} else if (!mediumPriorityCities.isEmpty()) {
								for (int i = 0; i < mediumPriorityCities.size(); i++) {
									target = mediumPriorityCities.get(i);
									// Get distance
									distance = calcDistanceToTarget(locationCity, target);
									distance_sugg = calcDistanceToTarget(getPlayerPiece().getLocation(), target);

									// Check if distance is less than 3 so try move immediately
									if (distance <= 3) {
										if (checkTravelWithRes(target)) {
											if (currPlayer.getDistanceResearch() == 0) { // Check if our current
																							// location
																							// has a research
												action = "shuttleFlight" + ":" + nearestResearch(target).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else if (currPlayer.getDistanceResearch() == 1) { // Check if distance is
																								// 1
												action = "driveCity" + ":" + nearestResearch(locationCity).getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											} else { // distance 2 or 3
												nextLocation = findNextLocation(locationCity,
														nearestResearch(locationCity));
												action = "driveCity" + ":" + nextLocation.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
												break;
											}
										} else { // Drive on foot
											nextLocation = findNextLocation(locationCity, target);
											action = "driveCity" + ":" + nextLocation.getName();
											currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
											break;
										}
									} else {
										if (distance_sugg > 3) { // distance from suggestion player to the target
											if (friend_hand.contains(locationCity)
													&& evaluateBoard(currPlayer, target)) {
												action = "charterFlight" + ":" + target.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
											} else if (friend_hand.contains(target)
													&& evaluateBoard(currPlayer, target)) {
												action = "directFlight" + ":" + target.getName();
												currPlayer.playerSuggestions[4
														- getPlayerAction()][playerIndex] = action;
											} else {
												target = null;
												continue;
											}
										} else { // suggestion player is closer to the target
											target = null;
											continue;
										}
									}
								}
								// If suggestions is null

								if (currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]
										.equals("rollDice:null")) {
									tempCt = mediumPriorityCities.get(0);
									tempDist = calcDistanceToTarget(locationCity, tempCt);

									for (int i = 1; i < mediumPriorityCities.size(); i++) {
										target = mediumPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);

										// Check if distance is nearest than temp and i dont have the city you want to
										// go
										if (distance < tempDist && !hand.contains(target)) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}
							} else { // Low priorities
								if (!lowPriorityCities.isEmpty()) { // low Priority with 3 cubes
									for (int i = 0; i < lowPriorityCities.size(); i++) {
										target = lowPriorityCities.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										System.out.println("DISTANCE TO " + target + " : " + distance);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									System.out.println("Location City: " + locationCity);
									nextLocation = findNextLocation(locationCity, tempCt);
									System.out.println("Temp City: " + tempCt + " Next Loc: " + nextLocation);
									System.out.println("LOW PRIORITY");
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								} else if (!CitiesWith2Cubes.isEmpty()) { // 2 cube cities
									for (int i = 0; i < CitiesWith2Cubes.size(); i++) {
										target = CitiesWith2Cubes.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								} else { // 1 cube
									for (int i = 0; i < CitiesWith1Cube.size(); i++) {
										target = CitiesWith1Cube.get(i);
										// Get distance
										distance = calcDistanceToTarget(locationCity, target);
										if (distance < tempDist) {
											tempDist = distance;
											tempCt = target;
										}
									}
									// Take action
									nextLocation = findNextLocation(locationCity, tempCt);
									action = "driveCity" + ":" + nextLocation.getName();
									currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex] = action;
								}
							}

							System.out.println(getPlayerName() + " wants to move as follows: "
									+ currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex]);
						}
					}
				}
			}

			// Make the action for the players who make suggestions
			String[] takeFinalAction = currPlayer.playerSuggestions[4 - getPlayerAction()][playerIndex].split(":"); // Split
																													// in
			// delimiter
			// ":"
			String finalAction = takeFinalAction[0];
			City targetCity = stringToCity(takeFinalAction[1]); // City to take the action
			System.out.println("Action string: " + finalAction + "|City: " + targetCity.toString());

			// Take the action
			if (finalAction.equals("driveCity")) {
				System.out.println("TRY TO DRIVE");
				driveCity(locationCity, targetCity, currPlayer);
			} else if (finalAction.equals("treatDisease")) {
				treatDisease(targetCity, targetCity.getColour(), currPlayer);
			} else if (finalAction.equals("discoverCure")) {
				checkTryCure(currPlayer);
			} else if (finalAction.equals("shuttleFlight")) {
				shuttleFlight(locationCity, targetCity, currPlayer);
			} else if (finalAction.equals("charterFlight")) {
				charterFlight(locationCity, targetCity, currPlayer);
			} else if (finalAction.equals("directFlight")) {
				directFlight(locationCity, targetCity, currPlayer);
			} else if (finalAction.equals("buildResearchStation")) {
				buildResearchStation(currPlayer);
			} else {
				System.out.println("ROLLED DICE SUGG");
				rollDice(currPlayer); // Random move due to fail of the program
			}
		}

	}

//Player will either treat disease or go to a city with 3 cubes.
	public void rollDice(Player currPlayer) {
		System.out.print("Wants to treat disease... ");
		if (!tryTreat(3, currPlayer)) {
			if (!go3CubeCities(currPlayer)) {
				if (!tryTreat(2, currPlayer)) {
					if (!go2CubeCities(currPlayer)) {
						if (!tryTreat(1, currPlayer)) {
							if (!go1CubeCities(currPlayer)) {
								System.out.println("Going to drive randomly as can't think of anything.");
								driveRandom(currPlayer);
							}
						}
					}
				}
			}
		}
	}

	// Check to see if the disease can be treat according to the @param threshold of
	// cubes.
	public boolean tryTreat(int threshold, Player currPlayer) {
		boolean toReturn = false;
		City locationCity = playerPiece.getLocation();
		if (locationCity.getMaxCube() >= threshold) {
			System.out.println("As there are " + threshold + " cubes in " + locationCity.getName() + " "
					+ this.getPlayerName() + " will try and treat disease.");
			String locationColour = locationCity.getColour();
			treatDisease(locationCity, locationColour, currPlayer);
			toReturn = true;
		} else {
			System.out.println("Doesn't think it's worth trying to treat here.");
		}
		return toReturn;

	}

	public boolean checkTryCure(Player curr_player) {
		if (checkCureWorthIt(curr_player)) {
			if (discoverCure(curr_player.playerPiece.getLocation(), tryCureCardColour(curr_player), curr_player)) {
				System.out.println(this.getPlayerName() + " has discovered a cure!");
				System.out.println("Yeah!!");
				System.out.println("  Yeah!!");
				System.out.println("    Yeah!!");
				return true;
			} else {
				System.out.println("They need to go to a researh station.");
				// tryDriveResearchStation(curr_player);
			}
		} else {
			System.out.println("no point in trying to find a cure.Go to Research Station!");
		}
		return false;
	}

	// evaluate the chance for cure , (if is it worth)
	public boolean checkCureWorthIt(Player curr_player) {
		String toCure = tryCureCardColour(curr_player);
		if (toCure != null) {
			for (int i = 0; i < pandemicBoard.diseases.size(); i++) {

				Disease disease = pandemicBoard.diseases.get(i);
				if (toCure == disease.getColour() && !disease.getCured()) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * // an attempt to drive to nearest research station public void
	 * tryDriveResearchStation() {
	 * System.out.println("Searching cities with research stations as destinations."
	 * ); getDistances(Variables.GET_CITIES_WITH_RESEARCH_STATION()); //
	 * System.out.println("Calculating destination"); City toDriveTo =
	 * calculateDestination(); // System.out.println("I'll try to drive to " +
	 * toDriveTo.getName()); driveCity(playerPiece.getLocation(), toDriveTo); }
	 */

	/**
	 * Check to see if the count of cards in any color equal the number required for
	 * a cure
	 **/
	public String tryCureCardColour(Player curr_player) {
		for (int i = 0; i < pandemicBoard.getNumberColours(); i++) {
			if (getCountXCards(possibleColour[i]) >= pandemicBoard.getNeededForCure(curr_player.getPlayerRole())) {
				return possibleColour[i];
			}
		}
		return null;
	}

	// get distance
	public int getDistanceResearch() {
		ArrayList<City> destinations = new ArrayList<City>();
		for (City c : pandemicBoard.cities) {
			for (City dest : Variables.GET_CITIES_WITH_RESEARCH_STATION()) {
				if (c.getName().equals(dest.getName())) {
					destinations.add(c);
				}
			}
		}

		getDistances(destinations);
		return playerPiece.getLocation().getDistance();
	}

	// get distances of the cities which a look to travel
	public void getDistances(ArrayList<City> destinations) {
		pandemicBoard.resetDistances();
		setDestination(destinations);
		int distance = 1;
		int loc = -1;
		for (int i = 0; i < pandemicBoard.cities.size(); i++) {
			if (pandemicBoard.cities.get(i).getName().equals(playerPiece.getLocation().getName())) {
				loc = i;
				break;
			}
		}
		while (pandemicBoard.cities.get(loc).getDistance() == 9999) {
			// System.out.println("Looking for places distance of " + distance);
			for (int i = 0; i < pandemicBoard.cities.size(); i++) {
				for (int x = 0; x < pandemicBoard.cities.get(i).getNeighbors().size(); x++) {
					// System.out.println(pandemicBoard.cities.get(i).getNeighbors().get(x).getDistance());
					if (pandemicBoard.cities.get(i).getDistance() == (distance - 1)
							&& pandemicBoard.cities.get(i).getNeighbors().get(x).getDistance() > distance) {
						pandemicBoard.cities.get(i).getNeighbors().get(x).setDistance(distance);
					}
				}
			}
			distance++;

		}
	}

	public void setDestination(ArrayList<City> destinations) {
		for (int i = 0; i < destinations.size(); i++) {
			destinations.get(i).setDistance(0);
		}
	}

	// Try to random charter flight
	public void charterRandom(Player currPlayer) {
		Random rand = new Random();
		int n = rand.nextInt(pandemicBoard.cities.size());
		charterFlight(playerPiece.getLocation(), pandemicBoard.cities.get(n), currPlayer);
	}

	// try to drive to random city until a possible city is chosen.
	public void driveRandom(Player currPlayer) {
		Random rand1 = new Random();
		City temp = playerPiece.getLocation();
		int n = rand1.nextInt(temp.getNeighbors().size());
		driveCity(playerPiece.getLocation(), temp.getNeighbors().get(n), currPlayer);
	}

	public City calculateDestination() {
		int closestDestination = 9999;
		City toReturn = new City(0, 0, 0, 0, 0);
		for (int i = 0; i < playerPiece.getLocation().getNeighbors().size(); i++) {
			if (playerPiece.getLocation().getNeighbors().get(i).getDistance() < closestDestination) {
				// System.out.println("Will probably go to " +
				// playerPiece.getLocationConnections()[i].getName());
				toReturn = playerPiece.getLocation().getNeighbors().get(i);
				closestDestination = playerPiece.getLocation().getNeighbors().get(i).getDistance();
			}

		}
		return toReturn;
	}

	public City calculateDestinationWithParameter(City city) {
		int closestDestination = 9999999;
		City toReturn = new City(0, 0, 0, 0, 0);
		for (int i = 0; i < city.getNeighbors().size(); i++) {
			if (city.getNeighbors().get(i).getDistance() < closestDestination) {

				toReturn = city.getNeighbors().get(i);
				closestDestination = city.getNeighbors().get(i).getDistance();
			}

		}
		return toReturn;
	}

	// find all the cities with 3 cubes and measure distances to make a decision
	// in which city to drive
	public boolean go3CubeCities(Player currPlayer) {
		ArrayList<City> CitiesWith3Cubes = pandemicBoard.get3CubeCities();
		if (CitiesWith3Cubes.size() > 0) {
			// System.out.print("Setting 3 cube cities ");
//          for (int i = 0 ; i < CitiesWith3Cubes.size() ; i ++)
//          {
//              System.out.print("#:"+(i+1) + " " + CitiesWith3Cubes.get(i).getName());
//          }

			getDistances(CitiesWith3Cubes);
			// System.out.println(" as destinations.");
			City toDriveTo = calculateDestination();
			// System.out.println(this.getPlayerName() + " will go to " +
			// toDriveTo.getName());
			driveCity(playerPiece.getLocation(), toDriveTo, currPlayer);
			return true;
		} else {
			System.out.println("No 3 cube cities.");
			return false;
		}
	}

	// find all the cities with 2 cubes
	// measure distance and drive to best case (city)
	public boolean go2CubeCities(Player currPlayer) {
		ArrayList<City> CitiesWith2Cubes = pandemicBoard.get2CubeCities();
		if (CitiesWith2Cubes.size() > 0) {
			// System.out.print("Setting 2 cube cities ");
//          for (int i = 0 ; i < CitiesWith2Cubes.size() ; i ++)
//          {
//             System.out.print("#:"+(i+1) + " " + CitiesWith2Cubes.get(i).getName());
//          }
			getDistances(CitiesWith2Cubes);
			// System.out.println(" as destinations.");
			City toDriveTo = calculateDestination();
			// System.out.println(this.getPlayerName() + " will go to " +
			// toDriveTo.getName());
			driveCity(playerPiece.getLocation(), toDriveTo, currPlayer);
			return true;
		} else {
			// System.out.println("No 2 cube cities.");
			return false;
		}
	}

	// find all the cities with 1 cubes
	// measure distance and drive to best case (city)
	public boolean go1CubeCities(Player currPlayer) {
		ArrayList<City> CitiesWith1Cubes = pandemicBoard.get1CubeCities();
		if (CitiesWith1Cubes.size() > 0) {
			// System.out.print("Setting 1 cube cities ");
//          for (int i = 0 ; i < CitiesWith1Cubes.size() ; i ++)
//          {
//              System.out.print("#:"+(i+1) + " " + CitiesWith1Cubes.get(i).getName());
//          }
			getDistances(CitiesWith1Cubes);
			// System.out.println(" as destinations.");
			City toDriveTo = calculateDestination();
			// System.out.println(this.getPlayerName() + " will go to " +
			// toDriveTo.getName());
			driveCity(playerPiece.getLocation(), toDriveTo, currPlayer);
			return true;
		} else {
			// System.out.println("No 1 cube cities.");
			return false;
		}
	}

	public Object clone(GameBoard gb, Piece pc) throws CloneNotSupportedException {
		Player cloned = (Player) super.clone();
		cloned.playerName = String.valueOf(this.playerName);
		cloned.playerRole = String.valueOf(this.playerRole);
		cloned.pandemicBoard = gb;
		cloned.playerPiece = pc;
		ArrayList<City> clonedhands = new ArrayList<City>();
		for (int i = 0; i < this.hand.size(); i++) {
			clonedhands.add((City) this.hand.get(i).clone());
		}
		cloned.hand = clonedhands;
		cloned.suggestions = this.suggestions; // shallow copy
		return cloned;
	}

}
