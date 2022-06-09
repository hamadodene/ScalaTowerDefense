package Tower

import Controller.GameController
import Controller.Tower.Tower
import Model.Tower.TowerTypes.{BASE_TOWER, CANNON_TOWER, FLAME_TOWER}
import org.scalatest.funsuite.AnyFunSuite

class TowerGenerationTest extends AnyFunSuite {

  import Model.Tower._

  //Initialize controller
  val controller: GameController = new GameController("player-test", mapDifficulty = 1)

  test("Create Base tower") {
    val tower: Tower = new Tower(TowerType(BASE_TOWER), controller.player, 0.0, 0.0, controller)
    assert(tower.name().equals(BaseTower.name))
    assert(tower.player.playerName.equals(controller.player.playerName))
  }

  test("Create Flame tower") {
    val tower: Tower = new Tower(TowerType(FLAME_TOWER), controller.player, 0.0, 0.0, controller)
    assert(tower.name().equals(FlameTower.name))
    assert(tower.player.playerName.equals(controller.player.playerName))
  }

  test("Create Cannon tower") {
    val tower: Tower = new Tower(TowerType(CANNON_TOWER), controller.player, 0.0, 0.0, controller)
    assert(tower.name().equals(CannonTower.name))
    assert(tower.player.playerName.equals(controller.player.playerName))
  }
}
