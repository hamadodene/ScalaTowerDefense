package Model.Projectile

import Controller.GameController
import Logger.LogHelper
import Model.Enemy.Enemy
import Model.Tower.TowerType
import Utility.WayPoint

class Projectile(_target_pos: WayPoint,
                 origin: WayPoint,
                 firing_tower: TowerType,
                 gameController: GameController
                ) extends ProjectileType with LogHelper {


  var speed: Double = 1.0
  var damage: Double = 1.0
  var hit = false
  val projectile = firing_tower.projectile_graphic
  val direction = (_target_pos - origin).normalize()
  val target_pos = _target_pos + direction * 2
  var pos = origin

  override def move(delta: Double): Unit = {
    val next_pos = pos + direction * (speed * delta)
    hit = (((target_pos - pos)) & (target_pos - next_pos)) < 0.0
    pos = next_pos
  }

  override def on_hit(enemy: Option[Enemy]): Unit = {
    enemy match {
      case None => ()
      case Some(enemy) => enemy.takeDamage(damage.toInt)
    }
    gameController.addProjectileToRemove(this)
  }

  override def update(delta: Double) = {
    move(delta)
    gameController.enemies.find(enemy => {
      val x = enemy.enemyCurrentPosition().x
      val y = enemy.enemyCurrentPosition().y
      val enemyPost = new WayPoint(x, y)
      enemyPost.distance_to(pos) < hitradius * 64
    }) match {
      case None => ()
      case Some(enemy) => {
        on_hit(Some(enemy))
      }
    }
    if (!hit) {
      on_hit(None)
    }
  }

}
