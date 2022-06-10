package Controller

import Controller.Tower.Tower
import Logger.LogHelper
import Model.Enemy.{Easy, Enemy, WaveImpl}
import Model.Player
import Model.Projectile.Projectile
import Model.Tower.TowerTypes.{BASE_TOWER, CANNON_TOWER, FLAME_TOWER}
import Model.Tower.{TowerType, TowerTypes}
import scalafx.animation.AnimationTimer
import scalafx.print.PrintColor.Color
import scalafx.scene.paint.Color.Red

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

/**
 * This class is the main controller, here is declared all sub-entities controller
 *
 * @param playerName    the player nickname
 * @param mapDifficulty difficulty level of the game
 */
class GameController(playerName: String, mapDifficulty: Int) extends LogHelper {

  private val gridController: GridController = new GridController(mapDifficulty)
  val player: Player = new Player(playerName)
  val towers = new ListBuffer[Tower]
  val enemies = new ListBuffer[Enemy]
  val projectiles = new ListBuffer[Projectile]
  val toRemoveProjectiles = new ListBuffer[Projectile]
  var alive: Boolean = true
  var gameStarted = false
  //Available tower ready to use by player
  val available_towers: Map[TowerTypes.TowerType, Tower] = Map.empty[TowerTypes.TowerType, Tower]
  var selected_tower: Option[Tower] = None
  var selected_cell: Option[Tower] = None
  var wave_counter = 0
  var release_selected_cell_and_tower: Boolean = false
  val frameRate: Double = 1.0 / 30.0 * 1000
  var wave = new WaveImpl(1, this)
  var lastTime = 0L

  /**
   * @param x longitude of selected tile
   * @param y latitude of selected tile
   */
  def onCellClicked(x: Double, y: Double): Unit = {
    if (isTowerSelected &&
      isTileBuildable((x / 64).toInt, (y / 64).toInt)
      && playerHaveEnoughMoneyEnough) {

      val xPos: Int = (x / 64).toInt * 64
      val yPos: Int = (y / 64).toInt * 64

      val tower = selected_tower.get.clone(xPos, yPos)
      this += tower
      selected_tower = Option(tower)
    } else if (isTowerSelected && !playerHaveEnoughMoneyEnough) {
      logger.info("Not enough money! Current money= " + player.money)
    } else if (!isTowerSelected && isAnotherTowerInTile(x.toInt, y.toInt)) {
      selected_cell = Some(towers.filter((_.posX.toInt == x.toInt))
        .filter((_.posY.toInt == y.toInt)).head)
    } else if (!isTowerSelected) {
      logger.info("No tower selected")
    }
  }

  def onPlayButton(): Unit = {
    logger.info("Started wave")
    gameStarted = true
    wave_counter += 1
    wave = this.wave.nextWave()
  }

  def resetSelectedTower(): Unit = {
    selected_tower = None
  }

  def update(delta: Double): Unit = {
    if (alive) {
      DrawingManager.drawGrid(this)

      projectiles.foreach(projectile => {
        projectile.update(delta)
      })
      //Avoid ConcurrentModificationException
      //I can't do gameController -= projectile on foreach
      // more info here: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ConcurrentModificationException.html
      projectiles --= toRemoveProjectiles

      towers.foreach(tower => {
        tower.update(delta)
        DrawingManager.drawTower(tower.posX, tower.posY, tower.graphic())
      })
      enemies.foreach(enemy => {
        enemy.update(delta)
        val x = enemy.enemyCurrentPosition().x
        val y = enemy.enemyCurrentPosition().y
        DrawingManager.enemyDraw(x, y, enemy.getType().color)
      })
      wave.update(delta)
      if (player.health <= 0) {
        alive = false
        logger.info("Player {} lose the game ", player.playerName)
        logger.info("Player {} stats : \n kill counter: {} ", player.killCounter)
      }
    }
  }

  def run(): AnimationTimer = {
    logger.info("Start tower defense game")

    //Animation timer and the time of the game.
    var lastTime = 0L

    val timer = AnimationTimer { t =>
      if (lastTime != 0) {
        val delta = (t - lastTime) / 1e2 //In seconds.
        update(delta)
      }
      lastTime = t
    }
    timer
  }

  def buildTower(tower: Tower): Unit = {
    selected_tower = Option(tower)
  }

  def sellTower(tower: Tower): Unit = {
    tower.player.addMoney(tower.sellCost())
    towers -= tower
  }

  //Enemies
  def +=(enemy: Enemy): Unit = {
    enemies += enemy
  }

  def -=(enemy: Enemy): Unit = {
    enemies -= enemy
  }

  def +=(tower: Tower): Unit = {
    towers += tower
    tower.towerType.amount += 1
  }

  def -=(tower: Tower): Unit = {
    towers -= tower
    tower.towerType.amount -= 1
  }

  def +=(projectile: Projectile): Unit = {
    projectiles += projectile
  }

  def -=(projectile: Projectile): Unit = {
    projectiles -= projectile
  }

  def addProjectileToRemove(projectile: Projectile): Unit = {
    toRemoveProjectiles += projectile
  }

  def setupAvailableTowers(): Unit = {
    available_towers ++= List(
      BASE_TOWER -> new Tower(TowerType(BASE_TOWER), player, 0, 0, this),
      CANNON_TOWER -> new Tower(TowerType(CANNON_TOWER), player, 0, 0, this),
      FLAME_TOWER -> new Tower(TowerType(FLAME_TOWER), player, 0, 0, this)
    )
  }

  def addNewTowerToCache(towerType: TowerTypes.TowerType, tower: Tower): Unit = {
    available_towers.addOne(towerType -> tower)
  }

  def getGridController: GridController = this.gridController

  private def isTowerSelected: Boolean = if (selected_tower.isEmpty) false else true

  private def playerHaveEnoughMoneyEnough: Boolean = player.removeMoney(selected_tower.get.price())

  private def isTileBuildable(x: Int, y: Int): Boolean = gridController.isTileBuildable(x, y)

  private def isAnotherTowerInTile(x: Int, y: Int): Boolean = {
    var tower_in_tile = false
    towers.foreach(tower => {
      if (tower.posX == x && tower.posY == y)
        tower_in_tile = false
      else
        tower_in_tile = true
    })
    tower_in_tile
  }

}

object GameController {

  private var _game_controller: Option[GameController] = None

  def game_controller: Option[GameController] = _game_controller

  private def game_controller_=(gameController: Option[GameController]): Unit = {
    _game_controller = gameController
  }

  def apply(playerName: String, mapDifficulty: Int): GameController = {
    val gameController: GameController = new GameController(playerName, mapDifficulty)
    gameController.setupAvailableTowers()
    game_controller = Option(gameController)
    gameController
  }
}
