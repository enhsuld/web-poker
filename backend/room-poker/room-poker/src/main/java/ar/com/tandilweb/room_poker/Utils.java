package ar.com.tandilweb.room_poker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ar.com.tandilweb.exchange.gameProtocol.texasHoldem.inGame.SchemaCard;
import ar.com.tandilweb.room_int.handlers.dto.UserData;
import ar.com.tandilweb.room_poker.deck.cards.Card;
import ar.com.tandilweb.room_poker.roundGame.Pot;

public class Utils {
	
	public static int checkPlayers(UserData[] usersInTable) {
		int countUsers = 0;
		for(int i = 0; i < usersInTable.length; i++) {
			if (usersInTable[i] != null) {
				countUsers++;
			}
		}
		return countUsers;
	}
	
	public static int getRandomPositionOfTakens(UserData[] usersInTable) {
		List<Integer> takenSpaces = getPlayers(usersInTable);
		Random rand = new Random();
		return takenSpaces.get(rand.nextInt(takenSpaces.size()));
	}
	
	public static int getNextPositionOfPlayers(UserData[] usersInGame, int actualPosition) {
		// TODO: ignore players in all in
		for(int i = actualPosition+1; i < usersInGame.length; i++) {
			if(usersInGame[i] != null) {
				return i;
			}
		}
		for(int i = 0; i < actualPosition; i++) {
			if(usersInGame[i] != null) {
				return i;
			}
		}
		return actualPosition; // wtf?, this only can be if the room has configured the minimum on 1 player.
	}
	
	public static int countUsersCanPlay(UserData[] usersInTable) {
		int quantity = 0;
		for(int i = 0; i < usersInTable.length; i++) {
			if(usersInTable[i] != null && usersInTable[i].chips > 0) {	
				quantity++;
			}
		}
		return quantity;
	}
	
	public static int countUsersInGame(UserData[] usersInGame) {
		int quantity = 0;
		for(int i = 0; i < usersInGame.length; i++) {
			if(usersInGame[i] != null) {
				quantity++;
			}
		}
		return quantity;
	}
	
	public static UserData[] getNewArrayOfUsers(UserData[] usersInTable) {
		UserData[] usersInGame = new UserData[usersInTable.length]; 
		for(int i = 0; i < usersInTable.length; i++) {
			if(usersInTable[i] != null && usersInTable[i].chips > 0) {				
				usersInGame[i] = usersInTable[i];
			} else {
				usersInGame[i] = null;
			}
		}
		return usersInGame;
	}
	
	public static List<UserData> getPlayersWithoutChips(UserData[] usersInTable){
		List<UserData> out = new ArrayList<UserData>();
		for(UserData ud: usersInTable) {
			if(ud != null && ud.chips <= 0) {
				out.add(ud);
			}
		}
		return out;
	}
	
	public static List<Integer> getPlayers(UserData[] usersInTable) {
		List<Integer> players = new ArrayList<Integer>();
		for(int i = 0; i < usersInTable.length; i++) {
			if (usersInTable[i] != null) {
				players.add(i);
			}
		}
		return players;
	}
	
	public static List<Integer> getPlayersOrderedByBets(long[] bets) {
		ArrayList<Integer> out = new ArrayList<Integer>();
		for(var bet: bets) {
			int maxPos = -1;
			long maxBet = 0;
			for(int i = 0; i<bets.length; i++) {
				if(bets[i] > 0) {
					if(bets[i] > maxBet && !out.contains(i)) {
						maxPos = i;
						maxBet = bets[i];
					}
				}
			}
			if(maxPos >= 0) {
				out.add(0, maxPos);
			}
		}
		return out;
	}
	
	public static List<Integer> getPlayersFromPosition(UserData[] usersInTable, int startPosition) {
		List<Integer> players = new ArrayList<Integer>();
		for(int i = startPosition + 1; i < usersInTable.length; i++) {
			if (usersInTable[i] != null) {
				players.add(i);
			}
		}
		for(int i = 0; i <= startPosition; i++) {
			if (usersInTable[i] != null) {
				players.add(i);
			}
		}
		return players;
	}
	
	public static int getPlyerPosition(UserData[] usersInGame, UserData userSearched) {
		// TODO: only for ready users?
		for(int i = 0; i<usersInGame.length; i++) {
			if(usersInGame[i]!= null && usersInGame[i].userID == userSearched.userID) {
				return i;
			}
		}
		return -1; // TODO: throw user not found.-
	}
	
	public static SchemaCard getSchemaFromCard(Card card) {
		return new SchemaCard(card.suit.ordinal(), card.value.getNumericValue());
	}
	
	public static List<Long> getPotValues(List<Pot> pots) {
		List<Long> vPots = new ArrayList<Long>();
		pots.forEach(pot -> {
			vPots.add(pot.pot);
		});
		return vPots;
	}

}
