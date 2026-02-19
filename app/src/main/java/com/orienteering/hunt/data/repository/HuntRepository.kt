package com.orienteering.hunt.data.repository

import com.orienteering.hunt.data.models.Clue
import com.orienteering.hunt.data.models.ClueDifficulty
import com.orienteering.hunt.data.models.GeoLocation
import com.orienteering.hunt.data.models.Hunt
import com.orienteering.hunt.data.models.HuntDifficulty
import com.orienteering.hunt.data.models.HuntLocation
import com.orienteering.hunt.data.models.LeaderboardEntry
import com.orienteering.hunt.data.models.Player
import com.orienteering.hunt.data.models.PlayerProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HuntRepository {
    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()

    private val _activeProgress = MutableStateFlow<PlayerProgress?>(null)
    val activeProgress: StateFlow<PlayerProgress?> = _activeProgress.asStateFlow()

    private val _allProgress = MutableStateFlow<List<PlayerProgress>>(emptyList())
    val allProgress: StateFlow<List<PlayerProgress>> = _allProgress.asStateFlow()

    private val mockHunts = listOf(
        createGoldenGateHunt(),
        createDowntownHunt(),
        createParkHunt(),
        createMarinaHunt()
    )

    private val mockLeaderboard = listOf(
        LeaderboardEntry(
            rank = 1,
            playerId = "player_1",
            playerName = "TrailBlazer",
            playerAvatarUrl = null,
            huntId = "hunt_golden_gate",
            huntName = "Golden Gate Adventure",
            score = 500,
            completionTime = 3200000,
            completedAt = System.currentTimeMillis() - 86400000
        ),
        LeaderboardEntry(
            rank = 2,
            playerId = "player_2",
            playerName = "Urban Explorer",
            playerAvatarUrl = null,
            huntId = "hunt_golden_gate",
            huntName = "Golden Gate Adventure",
            score = 500,
            completionTime = 3600000,
            completedAt = System.currentTimeMillis() - 72000000
        ),
        LeaderboardEntry(
            rank = 3,
            playerId = "player_3",
            playerName = "Pathfinder",
            playerAvatarUrl = null,
            huntId = "hunt_golden_gate",
            huntName = "Golden Gate Adventure",
            score = 450,
            completionTime = 2800000,
            completedAt = System.currentTimeMillis() - 172800000
        ),
        LeaderboardEntry(
            rank = 4,
            playerId = "player_4",
            playerName = "AdventureSeeker",
            playerAvatarUrl = null,
            huntId = "hunt_downtown",
            huntName = "Downtown Discovery",
            score = 350,
            completionTime = 2100000,
            completedAt = System.currentTimeMillis() - 259200000
        ),
        LeaderboardEntry(
            rank = 5,
            playerId = "player_5",
            playerName = "CityWalker",
            playerAvatarUrl = null,
            huntId = "hunt_park",
            huntName = "Park Explorer",
            score = 400,
            completionTime = 3000000,
            completedAt = System.currentTimeMillis() - 345600000
        )
    )

    fun getAllHunts(): List<Hunt> = mockHunts

    fun getHuntById(id: String): Hunt? = mockHunts.find { it.id == id }

    fun getActiveHunts(): List<Hunt> = mockHunts.filter { it.isActive }

    fun initializePlayer(username: String, displayName: String): Player {
        val player = Player(
            id = "player_${System.currentTimeMillis()}",
            username = username,
            displayName = displayName
        )
        _currentPlayer.value = player
        return player
    }

    fun setCurrentPlayer(player: Player) {
        _currentPlayer.value = player
    }

    fun startHunt(huntId: String, playerId: String): PlayerProgress {
        val hunt = getHuntById(huntId) ?: throw IllegalArgumentException("Hunt not found")
        val progress = PlayerProgress(
            id = "progress_${System.currentTimeMillis()}",
            playerId = playerId,
            huntId = huntId
        )
        _activeProgress.value = progress
        return progress
    }

    fun updateProgress(progress: PlayerProgress) {
        _activeProgress.value = progress
        val currentList = _allProgress.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == progress.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = progress
        } else {
            currentList.add(progress)
        }
        _allProgress.value = currentList
    }

    fun clearActiveProgress() {
        _activeProgress.value?.let { progress ->
            val currentList = _allProgress.value.toMutableList()
            currentList.add(progress)
            _allProgress.value = currentList
        }
        _activeProgress.value = null
    }

    fun getLeaderboard(huntId: String? = null): List<LeaderboardEntry> {
        return if (huntId != null) {
            mockLeaderboard.filter { it.huntId == huntId }
        } else {
            mockLeaderboard
        }
    }

    fun getPlayerProgress(playerId: String): List<PlayerProgress> {
        return _allProgress.value.filter { it.playerId == playerId }
    }

    private fun createGoldenGateHunt(): Hunt {
        val locations = listOf(
            HuntLocation(
                id = "loc_gg_1",
                name = "Golden Gate Bridge Vista Point",
                description = "The iconic viewing spot for San Francisco's most famous landmark",
                location = GeoLocation(37.8077, -122.4745),
                hint = "Look for the place where tourists gather to capture the perfect shot",
                points = 100,
                order = 1
            ),
            HuntLocation(
                id = "loc_gg_2",
                name = "Battery Spencer",
                description = "Historic military installation with panoramic views",
                location = GeoLocation(37.8263, -122.4798),
                hint = "An old fortification named after a Civil War general",
                points = 100,
                order = 2
            ),
            HuntLocation(
                id = "loc_gg_3",
                name = "Crissy Field",
                description = "Former airfield turned waterfront park",
                location = GeoLocation(37.8047, -122.4638),
                hint = "Where planes once landed, now families play",
                points = 100,
                order = 3
            ),
            HuntLocation(
                id = "loc_gg_4",
                name = "Palace of Fine Arts",
                description = "Stunning classical architecture in the Marina District",
                location = GeoLocation(37.8029, -122.4484),
                hint = "A classical rotunda built for a 1915 exposition",
                points = 100,
                order = 4
            ),
            HuntLocation(
                id = "loc_gg_5",
                name = "Baker Beach",
                description = "Scenic beach with Golden Gate views",
                location = GeoLocation(37.7934, -122.4825),
                hint = "Sandy shores with a famous neighbor to the north",
                points = 100,
                order = 5
            )
        )

        val clues = listOf(
            Clue(
                id = "clue_gg_1",
                huntLocationId = "loc_gg_1",
                text = "Stand where millions have stood before to witness orange steel spanning blue waters",
                hint = "The bridge's color is officially called 'International Orange'",
                difficulty = ClueDifficulty.EASY
            ),
            Clue(
                id = "clue_gg_2",
                huntLocationId = "loc_gg_2",
                text = "Find the concrete remnants that once protected the bay from invasion",
                hint = "Built in the 1890s, these guns never fired in anger",
                difficulty = ClueDifficulty.MEDIUM
            ),
            Clue(
                id = "clue_gg_3",
                huntLocationId = "loc_gg_3",
                text = "Walk where the U.S. Army's 91st Observation Squadron once operated",
                hint = "This marshland was filled in 1915 for the Panama-Pacific Expo",
                difficulty = ClueDifficulty.MEDIUM
            ),
            Clue(
                id = "clue_gg_4",
                huntLocationId = "loc_gg_4",
                text = "Seek the Greco-Roman temple reflected in tranquil waters",
                hint = "Bernard Maybeck designed this survivor of a temporary exhibition",
                difficulty = ClueDifficulty.EASY
            ),
            Clue(
                id = "clue_gg_5",
                huntLocationId = "loc_gg_5",
                text = "Discover the shoreline where the Presidio meets the Pacific",
                hint = "Part of the Golden Gate National Recreation Area since 1977",
                difficulty = ClueDifficulty.HARD
            )
        )

        return Hunt(
            id = "hunt_golden_gate",
            name = "Golden Gate Adventure",
            description = "Explore the iconic Golden Gate area and discover hidden gems",
            locations = locations,
            clues = clues,
            totalPoints = 500,
            estimatedDurationMinutes = 120,
            difficulty = HuntDifficulty.INTERMEDIATE,
            isActive = true
        )
    }

    private fun createDowntownHunt(): Hunt {
        val locations = listOf(
            HuntLocation(
                id = "loc_dt_1",
                name = "Union Square",
                description = "The heart of San Francisco shopping and culture",
                location = GeoLocation(37.7879, -122.4074),
                hint = "Where Powell Street cable cars turn around",
                points = 75,
                order = 1
            ),
            HuntLocation(
                id = "loc_dt_2",
                name = "Chinatown Dragon Gate",
                description = "The iconic entrance to America's oldest Chinatown",
                location = GeoLocation(37.7908, -122.4055),
                hint = "Gift from the Republic of China in 1970",
                points = 75,
                order = 2
            ),
            HuntLocation(
                id = "loc_dt_3",
                name = "Coit Tower",
                description = "Art Deco tower with stunning city views",
                location = GeoLocation(37.8024, -122.4058),
                hint = "Built with Lillie Hitchcock Coit's bequest to beautify the city",
                points = 100,
                order = 3
            ),
            HuntLocation(
                id = "loc_dt_4",
                name = "Ferry Building",
                description = "Historic ferry terminal turned foodie paradise",
                location = GeoLocation(37.7955, -122.3937),
                hint = "The clock tower has kept time since 1898",
                points = 75,
                order = 4
            )
        )

        val clues = listOf(
            Clue(
                id = "clue_dt_1",
                huntLocationId = "loc_dt_1",
                text = "Find the heart of the city where a monument to Admiral Dewey stands",
                hint = "The monument was erected in 1903",
                difficulty = ClueDifficulty.EASY
            ),
            Clue(
                id = "clue_dt_2",
                huntLocationId = "loc_dt_2",
                text = "Pass through the portal guarded by stone lions into another world",
                hint = "Grant Avenue begins here",
                difficulty = ClueDifficulty.EASY
            ),
            Clue(
                id = "clue_dt_3",
                huntLocationId = "loc_dt_3",
                text = "Climb Telegraph Hill to find the fluted column honoring firefighters",
                hint = "The murals inside depict California life in the 1930s",
                difficulty = ClueDifficulty.MEDIUM
            ),
            Clue(
                id = "clue_dt_4",
                huntLocationId = "loc_dt_4",
                text = "Where commuters once caught ferries, foodies now gather",
                hint = "Look for the Saturday farmers market",
                difficulty = ClueDifficulty.EASY
            )
        )

        return Hunt(
            id = "hunt_downtown",
            name = "Downtown Discovery",
            description = "Navigate the bustling streets of downtown San Francisco",
            locations = locations,
            clues = clues,
            totalPoints = 325,
            estimatedDurationMinutes = 90,
            difficulty = HuntDifficulty.BEGINNER,
            isActive = true
        )
    }

    private fun createParkHunt(): Hunt {
        val locations = listOf(
            HuntLocation(
                id = "loc_park_1",
                name = "Japanese Tea Garden",
                description = "Oldest public Japanese garden in the United States",
                location = GeoLocation(37.7700, -122.4700),
                hint = "Built for the 1894 California Midwinter International Exposition",
                points = 100,
                order = 1
            ),
            HuntLocation(
                id = "loc_park_2",
                name = "California Academy of Sciences",
                description = "Natural history museum with living roof",
                location = GeoLocation(37.7699, -122.4661),
                hint = "The roof has 7 hills covered in native plants",
                points = 100,
                order = 2
            ),
            HuntLocation(
                id = "loc_park_3",
                name = "de Young Museum",
                description = "Fine arts museum with observation tower",
                location = GeoLocation(37.7715, -122.4686),
                hint = "The copper facade will eventually turn green",
                points = 100,
                order = 3
            ),
            HuntLocation(
                id = "loc_park_4",
                name = "Strawberry Hill",
                description = "Island in Stow Lake with waterfall views",
                location = GeoLocation(37.7671, -122.4713),
                hint = "The highest point in Golden Gate Park",
                points = 100,
                order = 4
            )
        )

        val clues = listOf(
            Clue(
                id = "clue_park_1",
                huntLocationId = "loc_park_1",
                text = "Find the pagoda where tea has been served for over a century",
                hint = "Walk through the drum gate to enter",
                difficulty = ClueDifficulty.MEDIUM
            ),
            Clue(
                id = "clue_park_2",
                huntLocationId = "loc_park_2",
                text = "Discover where rainforest meets aquarium meets planetarium",
                hint = "Renzo Piano designed the current building",
                difficulty = ClueDifficulty.EASY
            ),
            Clue(
                id = "clue_park_3",
                huntLocationId = "loc_park_3",
                text = "Ascend the twisting tower for panoramic park views",
                hint = "Free admission to the observation floor",
                difficulty = ClueDifficulty.EASY
            ),
            Clue(
                id = "clue_park_4",
                huntLocationId = "loc_park_4",
                text = "Cross the bridge to find the hidden waterfall on an island",
                hint = "Rent a boat to circle the island",
                difficulty = ClueDifficulty.HARD
            )
        )

        return Hunt(
            id = "hunt_park",
            name = "Park Explorer",
            description = "Discover the wonders of Golden Gate Park",
            locations = locations,
            clues = clues,
            totalPoints = 400,
            estimatedDurationMinutes = 100,
            difficulty = HuntDifficulty.INTERMEDIATE,
            isActive = true
        )
    }

    private fun createMarinaHunt(): Hunt {
        val locations = listOf(
            HuntLocation(
                id = "loc_marina_1",
                name = "Fort Mason Center",
                description = "Historic military base turned arts and culture hub",
                location = GeoLocation(37.8044, -122.4311),
                hint = "Home to the San Francisco African American Historical & Cultural Society",
                points = 80,
                order = 1
            ),
            HuntLocation(
                id = "loc_marina_2",
                name = "Aquatic Park",
                description = "Historic maritime district with swimming cove",
                location = GeoLocation(37.8066, -122.4230),
                hint = "Home to the Dolphin Club swimmers since 1877",
                points = 80,
                order = 2
            ),
            HuntLocation(
                id = "loc_marina_3",
                name = "Ghirardelli Square",
                description = "Former chocolate factory turned shopping destination",
                location = GeoLocation(37.8057, -122.4228),
                hint = "The clock tower is a San Francisco landmark",
                points = 70,
                order = 3
            )
        )

        val clues = listOf(
            Clue(
                id = "clue_marina_1",
                huntLocationId = "loc_marina_1",
                text = "Find where the Army once shipped supplies and now artists create",
                hint = "Look for the historic piers",
                difficulty = ClueDifficulty.MEDIUM
            ),
            Clue(
                id = "clue_marina_2",
                huntLocationId = "loc_marina_2",
                text = "Where brave souls swim in the chilly bay year-round",
                hint = "The bathhouse is a WPA project",
                difficulty = ClueDifficulty.HARD
            ),
            Clue(
                id = "clue_marina_3",
                huntLocationId = "loc_marina_3",
                text = "Follow your nose to the chocolate factory square",
                hint = "Domingo Ghirardelli started making chocolate here in 1852",
                difficulty = ClueDifficulty.EASY
            )
        )

        return Hunt(
            id = "hunt_marina",
            name = "Marina Meander",
            description = "Stroll through the scenic Marina district waterfront",
            locations = locations,
            clues = clues,
            totalPoints = 230,
            estimatedDurationMinutes = 60,
            difficulty = HuntDifficulty.BEGINNER,
            isActive = true
        )
    }
}
