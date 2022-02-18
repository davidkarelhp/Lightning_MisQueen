package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;
import java.util.stream.*;

import static java.lang.Math.*;

public class Bot {

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command DECELERATE = new DecelerateCommand();
    private final static Command DO_NOTHING = new DoNothingCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    private static final int MINIMUM_SPEED  = 0;
    private static final int SPEED_STATE_1  = 3;
    private static final int INITIAL_SPEED  = 5;
    private static final int SPEED_STATE_2  = 6;
    private static final int SPEED_STATE_3 = 8;
    private static final int MAXIMUM_SPEED  = 9;
    private static final int BOOST_SPEED  = 15;
    private static final int[] SPEED_STATE = {MINIMUM_SPEED , SPEED_STATE_1 , SPEED_STATE_2 , SPEED_STATE_3, MAXIMUM_SPEED , BOOST_SPEED};

    public Bot() {
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        // Mendapatkan blok maksimum yang dapat dicapai saat myCar (player) bergerak lurus
        List<Lane> blocksMax = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, 0);

        // Mendapatkan blok maksimum yang dapat dicapai saat myCar (player) bergerak ke lane kanan
        List<Lane> rightBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, 1);

        // Mendapatkan blok maksimum yang dapat dicapai saat myCar (player) bergerak ke lane kanan
        List<Lane> leftBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, -1); //blok maksimal yang dapat ditempuh player di lane kirinya

        List<Lane> blocks;
        if (blocksMax.size() >= myCar.speed && myCar.boostCounter == 1) {
            // Boost di turn selanjutnya habis kembali ke max speed
            blocks = blocksMax.subList(0, Bot.MAXIMUM_SPEED);
        } else if (blocksMax.size() >= myCar.speed && myCar.boostCounter == 0) {
            // Tidak sedang dalam boost
            blocks = blocksMax.subList(0, myCar.speed);
        } else {
            blocks = blocksMax;
        }

        // Memperbaiki mobil saat tidak bisa bergerak lagi
        if (myCar.damage >= 5) {// Fix if car completely broken
            return FIX;
        }

        // Mempercepat myCar (player) jika kecepatan terlalu renda, jika bisa boost secara efektif, boost dilakukan
        if (myCar.speed == MINIMUM_SPEED ) {// ACCELERATE IF SPEED 0
            if (canReachBoostSpeed(gameState)) {
                return BOOST;
            }
            return ACCELERATE;
        }

        // Mengecek bahaya yang langsung berada di depan myCar (player)
        String immediateDanger = checkImmediateDanger(blocks, gameState, 0);

        // Mekanisme menghindar (pindah jalur (lane) dan pwer up lizard) serta sedikit mekanisme menyerang
        // Jika ada bahaya di depan
        if (immediateDanger != null) {// Lizard mechanism and Avoidance mechanism
            String immediateDangerLeft;
            String immediateDangerRight;

            // Melakukan pengecekan bahaya yang akan langsung membahayakan myCar (player) jika bergerak ke kanan dan ke kiri
            if (myCar.position.lane == 1) {
                immediateDangerLeft = null;
            } else {
                immediateDangerLeft = checkImmediateDanger(leftBlocks, gameState, 1);
            }

            if (myCar.position.lane == 4) {
                immediateDangerRight = null;
            } else {
                immediateDangerRight = checkImmediateDanger(rightBlocks, gameState, 1);
            }

            // Jika dideteksi lawan ada di depan dan myCar (player) diperkirakan tidak akan menabrak lawan dan memiliki power up EMP
            if (immediateDanger == "OPPONENT" && Math.abs(myCar.position.lane - opponent.position.lane) <= 1) {
                if (countPowerUp(PowerUps.EMP, myCar.powerups) > 0 && ((myCar.position.lane != opponent.position.lane) || (myCar.position.block <= opponent.position.block - myCar.speed + 3))) {
                    return EMP;
                }
            }

            // Jika punya lizard, gunakan lizard untuk menghindari bahaya sekaligus menambah poin dan menjaga myCar (player) tetap lurus
            if (countPowerUp(PowerUps.LIZARD, myCar.powerups) > 0) {
                return LIZARD;
            }

            // menghitung damage yang dari bahaya yang akan langsung ditabrak oleh myCar (player)
            int damage = countImmediateDamage(immediateDanger);

            // Jika player berada di lajur (lane) pinggir, damage sebelahnya akan dijadikan 999 untuk mencegah keluar jalur (lane)
            int rightDamage = myCar.position.lane == 4 ? 999 : countImmediateDamage(immediateDangerRight);
            int leftDamage = myCar.position.lane == 1 ? 999 : countImmediateDamage(immediateDangerLeft);

            // Menghitung damage yang paling kecil dari 3 jalur (lane) berbeda
            int minDamage = min(damage, min(rightDamage, leftDamage));

            // Menghitung jumlah power up di jalur (lane) lurus, kiri, dan kanan
            int countPower = countPowerUpInBlocks(blocks);
            int countPowerLeft = countPowerUpInBlocks(leftBlocks);
            int countPowerRight = countPowerUpInBlocks(rightBlocks);

            // Menghitung powerup terbanyak
            int maxPower = max(countPower, max(countPowerLeft, countPowerRight));

            // Menghitung power up kedua terbanyak
            int midPower = maxPower;
            if ((countPowerLeft <= countPower && countPower <= countPowerRight) || (countPowerRight <= countPower && countPower <= countPowerLeft)) {
                midPower = countPower;
            } else if ((countPower <= countPowerLeft && countPowerLeft <= countPowerRight) || (countPowerRight <= countPowerLeft && countPowerLeft <= countPower)) {
                midPower = countPowerLeft;
            } else if ((countPower <= countPowerRight && countPowerRight <= countPowerLeft) || (countPowerLeft <= countPowerRight && countPowerRight <= countPower)) {
                midPower = countPowerRight;
            }

            // Jika saat lurus damagenya adalah yang paling kecil dan tidak ada cyber truck didepan, tidak usah lakukan apa-apa
            if (damage == minDamage && !hasCyberTruck(blocks)) {
                return DO_NOTHING;
            }

            // Jika berada di lane paling kanan dan lajur kiri tidak mempunyai cyber truck, belok kiri
            if (myCar.position.lane == 4 && !hasCyberTruck(leftBlocks)) {
                return TURN_LEFT;
            }

            // Jika berada di lane paling kiri dan lajur kanan tidak mempunyai cyber truck, belok kanan
            if (myCar.position.lane == 1 && !hasCyberTruck(rightBlocks)) {
                return TURN_RIGHT;
            }

            // Jika power paling banyak ternyata berada di tengah, ubah power paling banyak menjadi yang kedua terbanyak,
            // hal ini dilakukan karena di bagian kode di bawah, hanya ada dua pilihan, belok kiri atau kanan
            if (maxPower == countPower) {
                maxPower = midPower;
            }

            // Damage kiri dan kanan sama
            if (leftDamage == rightDamage) {
                if (!hasCyberTruck(leftBlocks) && countPowerLeft == maxPower) {
                    return TURN_LEFT;
                }
                if (!hasCyberTruck(rightBlocks) && countPowerRight == maxPower) {
                    return TURN_RIGHT;
                }
                return DO_NOTHING; // Default return value
            }

            if (!hasCyberTruck(leftBlocks) && leftDamage == minDamage) {
                return TURN_LEFT;
            }

            if (!hasCyberTruck(rightBlocks) && rightDamage == minDamage) {
                return TURN_RIGHT;
            }

            return DO_NOTHING; // Default return value
        }

        // Bagian menyerang
        // Gunakan EMP jika lawan ada di jalur pinggir dan kita ada di jalur sebelah lawan, lawan tidak mungkin menghindar dari EMP
        if (countPowerUp(PowerUps.EMP, myCar.powerups) > 0 && (myCar.position.block <= opponent.position.block - myCar.speed + 2) && (opponent.position.lane == 1 && myCar.position.lane == 2)){
            return EMP;
        }

        if (countPowerUp(PowerUps.EMP, myCar.powerups) > 0 && (myCar.position.block <= opponent.position.block - myCar.speed + 2) && (opponent.position.lane == 4 && myCar.position.lane == 3)){
            return EMP;
        }

        // Menempatkan Cyber Truck jika punya, penempatan dilakukan dengan melakukan prediksi posisi lawan
        if (countPowerUp(PowerUps.TWEET, myCar.powerups) > 0) {
            return new TweetCommand(opponent.position.lane, opponent.position.block + faster(opponent.speed) + 3);
        }

        // Gunakan power up oil jika lawan diprediksi akan melewati posisi myCar (player) berada sekarang
        if ((countPowerUp(PowerUps.OIL, myCar.powerups) > 0) && (myCar.position.block > opponent.position.block) && (myCar.position.block <= opponent.position.block + opponent.speed) && myCar.position.lane == opponent.position.lane) {
            return OIL;
        }

        // Jika mempunyai boost
        if (countPowerUp(PowerUps.BOOST, myCar.powerups) > 0) {// Boost mechanism
            // Jika bisa sampai booost speed
            if (canReachBoostSpeed(gameState)) {
                return BOOST;
            } else if (myCar.damage > 0) { // Perbaiki agar bisa mencapai kecepatan boost
                return FIX;
            }
        }

        // Akselerasi
        if (canAccelerate(gameState)) {
            return ACCELERATE;
        }

        // Perbaiki karena tidak mungkin akselerasi lagi
        if (myCar.damage == 4 && myCar.speed == SPEED_STATE_1 ) {
            return FIX;
        }

        // Perbaiki karena tidak mungkin akselerasi lagi
        if (myCar.damage == 3 && myCar.speed == SPEED_STATE_2 ) {
            return FIX;
        }

        // Perbaiki karena tidak mungkin akselerasi lagi
        if (myCar.damage == 2 && myCar.speed == SPEED_STATE_3) {
            return FIX;
        }

        // Mekanisme pengambilan bonus untuk menambah skor sekaligus menghindari damage
        String immediateDangerLeft;
        String immediateDangerRight;

        if (myCar.position.lane == 1) {
            immediateDangerLeft = null;
        } else {
            immediateDangerLeft = checkImmediateDanger(leftBlocks, gameState, 1);
        }

        if (myCar.position.lane == 4) {
            immediateDangerRight = null;
        } else {
            immediateDangerRight = checkImmediateDanger(rightBlocks, gameState, 1);
        }

        int damage = countImmediateDamage(immediateDanger);
        int rightDamage = myCar.position.lane == 4 ? 999 : countImmediateDamage(immediateDangerRight);
        int leftDamage = myCar.position.lane == 1 ? 999 : countImmediateDamage(immediateDangerLeft);

        int countPower = countPowerUpInBlocks(blocks);
        int countPowerLeft = countPowerUpInBlocks(leftBlocks);
        int countPowerRight = countPowerUpInBlocks(rightBlocks);
        int maxPower = max(countPower, max(countPowerLeft, countPowerRight));

        if (countPower == maxPower && damage == 0) {
            return DO_NOTHING;
        }

        if (countPowerLeft == maxPower && leftDamage == 0) {
            return TURN_LEFT;
        }

        if (countPowerRight == maxPower && rightDamage == 0) {
            return TURN_RIGHT;
        }

        return DO_NOTHING;
    }

    private int countPowerUpInBlocks(List<Lane> blocks) {
        // Menghitung power up dalam suatu list of Lane
        int ret = 0;
        for (Lane block: blocks) {
            if (block.terrain.equals(Terrain.BOOST) || block.terrain.equals(Terrain.OIL_POWER) || block.terrain.equals(Terrain.LIZARD) || block.terrain.equals(Terrain.TWEET) || block.terrain.equals(Terrain.EMP)) {
                ret++;
            }
        }
        return ret;
    }

    private int countPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        // Menghitung power up tertentu yang dimiliki
        int ret = 0;
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                ret += 1;
            }
        }
        return ret;
    }

    private String checkImmediateDanger(List<Lane> blocks, GameState gameState, int laneIndicator) {
        // Mengecek bahaya terdekat yang akan ditemui langsung oleh myCar (player)
        List<Terrain> terrains = blocks.stream().map(element -> element.terrain).collect(Collectors.toList());
        Car player = gameState.player;
        Car opponent = gameState.opponent;
        String nearestTerrain = null;
        int nearestTerrainX = player.position.block + 1;
        int nearestCyberTruckX = player.position.block + 1;
        int speed = laneIndicator == 0 ? player.speed : player.speed - 1;

        boolean opponentExist = blocks.stream().map(element -> element.occupiedByPlayerId).collect(Collectors.toList()).contains(opponent.id) && (speed + player.position.block >= faster(gameState.opponent.speed) + opponent.position.block);
        int cyberTruck = blocks.stream().map(element -> element.cyberTruck).collect(Collectors.toList()).indexOf(true);

        if (cyberTruck != -1) {
            nearestCyberTruckX += cyberTruck;
        }

        for (Terrain terrain: terrains) {
            if (terrain.equals(Terrain.MUD)) {
                nearestTerrain = "MUD";
                break;
            } else if (terrain.equals(Terrain.WALL)) {
                nearestTerrain = "WALL";
                break;
            } else if (terrain.equals(Terrain.OIL_SPILL)) {
                nearestTerrain = "OIL";
                break;
            }
            nearestTerrainX++;
        }

        if (nearestCyberTruckX <= nearestTerrainX) {
            nearestTerrain = null;
        } else {
            cyberTruck = -1;
        }


        if (nearestTerrain != null || cyberTruck != -1 || opponentExist) {
            if (nearestTerrain != null) {
                if (opponent.position.block <= nearestTerrainX) {
                    return "OPPONENT";
                } else {
                    return nearestTerrain;
                }
            }

            if (cyberTruck != -1) {
                if (opponent.position.block < nearestTerrainX) {
                    return "OPPONENT";
                } else {
                    return "CYBERTRUCK";
                }
            }
            return "CYBERTRUCK";
        }

        return null;
    }

    private int countImmediateDamage(String immediateDamage) {
        // Menghitung damage yang diberikan berdasarkan jenisny
        if (immediateDamage == "MUD" || immediateDamage == "OIL") {
            return 1;
        }

        if (immediateDamage == "WALL") {
            return 2;
        }

        // Cyber Truck diberi damage 3, walaupun sebenarnya memiiki damage 2,
        // hal ini dilakukan karena Cyber Truck dapat menghentikan mobil sehingga lebih baik dihindari
        if (immediateDamage == "CYBERTRUCK") {
            return 3;
        }

        return 0;
    }

    private boolean canAccelerate(GameState gameState) {
        // Melakukan pengecekan bisa akselerasi atau tidak berdasarkan damage yang dimiliki, bukan boost
        int damage = gameState.player.damage;
        int speed = gameState.player.speed;
        if (damage == 0 && speed <= Bot.SPEED_STATE_3) {
            return true;
        }
        if (damage == 1 && speed <= Bot.SPEED_STATE_3) {
            return true;
        }
        if (damage == 2 && speed <= Bot.SPEED_STATE_2) {
            return true;
        }
        if (damage == 3 && speed <= Bot.SPEED_STATE_1) {
            return true;
        }
        if (damage == 4 && speed <= Bot.MINIMUM_SPEED) {
            return true;
        }
        return false;
    }

    private int faster(int speed) {
        // Mengembalikan kecepatan satu tingkat di atas kecepatan yang dimasukkan
        if (speed == Bot.INITIAL_SPEED ) {
            return Bot.SPEED_STATE_2 ;
        }
        if (speed == Bot.BOOST_SPEED) {
            return speed;
        }
        for (int i = 0; i < 5; i++) {
            if (speed == Bot.SPEED_STATE[i]) {
                return Bot.SPEED_STATE[i + 1];
            }
        }
        return speed; // default return
    }

    private boolean hasCyberTruck(List<Lane> blocks) {
        // Mengecek apakah di dalam suatu list of Lane terdapat Cyber Truck
        return blocks.stream().map(element -> element.cyberTruck).collect(Collectors.toList()).contains(true);
    }

    private boolean canReachBoostSpeed(GameState gameState) {
        // Mengecek jika boost speed dapat dicapai
        if (countPowerUp(PowerUps.BOOST, gameState.player.powerups) > 0) {
            if (gameState.player.damage > 0 || gameState.player.boostCounter > 1) {
                return false;
            }
            return true;
        }
        return false;
    }
    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Lane> getBlocksInFront(int lane, int block, GameState gameState, int laneIndicator) {
        // mengambil blok (list of Lane) yang terdapat di depan
        List<Lane[]> map = gameState.lanes;
        List<Lane> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;
        // Jika belok, speed dikurangi satu
        int speed = laneIndicator == 0 ? gameState.player.speed : gameState.player.speed - 1;
        if (laneIndicator == 0) {
            if (canReachBoostSpeed(gameState)) {
                speed = Bot.BOOST_SPEED ;
            }
            else if (canAccelerate(gameState)) {
                speed = faster(speed);
            }
        }
        else if (laneIndicator == -1) {
            if (lane == 1) {
                return blocks;
            }
            // Dikurangi karena belok
            block--;
        }
        else {
            if (lane == 4) {
                return blocks;
            }
            // Dikurangi karena belok
            block--;
        }
        Lane[] laneList = map.get(lane + laneIndicator - 1);
        for (int i = max(block - startBlock, 0) + 1; i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            blocks.add(laneList[i]);
        }
        return blocks;
    }
}